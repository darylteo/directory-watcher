package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractDirectoryWatchService implements AutoCloseable, DirectoryWatchService {
  private final WatchService watchService = FileSystems.getDefault().newWatchService();
  private List<DirectoryWatcher> watchers = new LinkedList<>();

  public AbstractDirectoryWatchService() throws IOException {
  }

  protected WatchService getWatchService() {
    return this.watchService;
  }

  protected List<DirectoryWatcher> getWatchers() {
    return this.watchers;
  }

  /**
   * <p>
   * Instantiates a new DirectoryWatcher for the path given.
   * </p>
   * 
   * @param dir
   *          the path to watch for events.
   * @return a DirectoryWatcher for this path (and all child paths)
   * @throws IOException
   */
  public DirectoryWatcher newWatcher(String dir) throws IOException {
    return newWatcher(Paths.get(dir));
  }

  /**
   * <p>
   * Instantiates a new DirectoryWatcher for the path given.
   * </p>
   * 
   * @param dir
   *          the path to watch for events.
   * @return a DirectoryWatcher for this path (and all child paths)
   * @throws IOException
   */
  public DirectoryWatcher newWatcher(Path dir) throws IOException {
    DirectoryWatcher watcher = new DirectoryWatcher(this.watchService, dir);
    addWatcher(watcher);

    return watcher;
  }

  private void addWatcher(DirectoryWatcher watcher) {
    this.watchers.add(watcher);
  }

  protected void handleWatchKey(WatchKey key) {
    if (key == null) {
      return;
    }

    /* Poll the events and handle */
    for (WatchEvent<?> event : key.pollEvents()) {
      WatchEvent.Kind<?> kind = event.kind();

      if (kind == StandardWatchEventKinds.OVERFLOW) {
        continue;
      }

      if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
        for (DirectoryWatcher watcher : getWatchers()) {
          watcher.handleModifyEvent(key, (Path) event.context());
        }

        continue;
      }

      if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
        for (DirectoryWatcher watcher : getWatchers()) {
          watcher.handleCreateEvent(key, (Path) event.context());
        }

        continue;
      }

      for (DirectoryWatcher watcher : getWatchers()) {
        watcher.handleDeleteEvent(key, (Path) event.context());
      }
    }

    /* Reset the Key to get more events later */
    if (!key.reset()) {
      for (DirectoryWatcher watcher : getWatchers()) {
        watcher.handleKeyInvalid(key);
      }
    }
  }

  @Override
  public void close() throws Exception {
    this.watchService.close();
    this.watchers.clear();
  }
}
