package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectoryWatcherFactory implements AutoCloseable {
  private final WatchService watchService = FileSystems.getDefault().newWatchService();
  private final ExecutorService executorService = Executors.newCachedThreadPool();

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
          WatchEvent.Kind<?> kind = event.kind();

          if (kind == StandardWatchEventKinds.OVERFLOW) {
            continue;
          }

          if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            for (DirectoryWatcher watcher : watchers) {
              watcher.handleModifyEvent(key, (Path) event.context());
            }

            continue;
          }

          if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            for (DirectoryWatcher watcher : watchers) {
              watcher.handleCreateEvent(key, (Path) event.context());
            }

            continue;
          }

          for (DirectoryWatcher watcher : watchers) {
            watcher.handleDeleteEvent(key, (Path) event.context());
          }
        }

        /* Reset the Key to get more events later */
        if (!key.reset()) {
          for (DirectoryWatcher watcher : watchers) {
            watcher.handleKeyInvalid(key);
          }
        }
      }
    }
  }
}
