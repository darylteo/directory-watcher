package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectoryWatcherFactory implements AutoCloseable {
  private final WatchService watchService = FileSystems.getDefault().newWatchService();
  private final ExecutorService executorService = Executors.newCachedThreadPool();

  /* WatchService Modifiers */
  private Set<Path> dirs = new HashSet<>();

  /* Watcher Mapping */
  private List<DirectoryWatcher> watchers = new LinkedList<>();

  public DirectoryWatcherFactory() throws IOException {
    this(1);
  }

  public DirectoryWatcherFactory(int threadCount) throws IOException {
    for (int i = 0; i < threadCount; i++) {
      executorService.execute(new WatcherThread());
    }
  }

  public DirectoryWatcher newWatcher(Path dir) throws IOException {
    DirectoryWatcher watcher = new DirectoryWatcher(watchService, dir);
    watchers.add(watcher);

    return watcher;
  }

  @Override
  public void close() throws Exception {
    watchService.close();
    executorService.shutdown();
  }

  /*
   * Thread responsible for the watching logic
   */
  private class WatcherThread implements Runnable {
    @Override
    public void run() {
      while (true) {
        WatchKey key = null;
        try {
          key = watchService.take();
        } catch (InterruptedException | ClosedWatchServiceException e) {
          return;
        }

        /* Poll the events and handle */
        for (WatchEvent<?> event : key.pollEvents()) {
          try {
            handleEvent(key, (WatchEvent<Path>) event);
          } catch (IOException e) {
            continue;
          }
        }

        /* Reset the Key to get more events later */
        if (!key.reset()) {
          try {
            handleDirectoryDeleted(key, (Path) key.watchable());
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }

    private void handleEvent(WatchKey key, WatchEvent<Path> event) throws IOException {
      WatchEvent.Kind<?> kind = event.kind();

      if (kind == StandardWatchEventKinds.OVERFLOW) {
        return;
      }

      Path dir = (Path) key.watchable();
      Path path = dir.resolve(event.context());

      /* ENTRY_CREATE */
      if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
        if (Files.isDirectory(path)) {
          handleDirectoryCreated(key, path);
          return;
        }

        handleFileCreated(key, path);
        return;
      }

      /* ENTRY_MODIFY */
      if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
        if (dirs.contains(path)) {
          // ignore
          return;
        }

        handleFileModified(key, path);
        return;
      }

      /* ENTRY_DELETE */
      if (dirs.contains(path)) {
        // the deleted entry was one of the watched directories
        // We'll wait for the key to invalidate to screw around with the
        // watched dirs and keys
        return;
      }

      handleFileDeleted(key, path);
    }

    private void handleDirectoryCreated(final WatchKey key, Path dir) throws IOException {
      dirs.add(dir);
      for (DirectoryWatcher watcher : watchers) {
        watcher.directoryCreated(key, dir);
      }
    }

    private void handleDirectoryDeleted(WatchKey key, Path dir) throws IOException {
      dirs.remove(dir);
      for (DirectoryWatcher watcher : watchers) {
        watcher.directoryDeleted(key);
      }
    }

    private void handleFileCreated(WatchKey key, Path file) throws IOException {
      for (DirectoryWatcher watcher : watchers) {
        watcher.fileCreated(key, file);
      }
    }

    private void handleFileDeleted(WatchKey key, Path file) {
      for (DirectoryWatcher watcher : watchers) {
        watcher.fileDeleted(key, file);
      }
    }

    private void handleFileModified(WatchKey key, Path file) {
      for (DirectoryWatcher watcher : watchers) {
        watcher.fileModified(key, file);
      }
    }
  }
}
