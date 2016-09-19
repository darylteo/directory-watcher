package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Each implementation of DirectoryWatchService is responsible for creating
 * instances of {@link DirectoryWatcher} for you.
 * </p>
 * <p>
 * Every DirectoryWatcher that is created by this Factory will use the same
 * WatchService to receive file system events. If this is not desirable,
 * instantiate a separate DirectoryWatcherFactory.
 * </p>
 * <p>
 * All created DirectoryWatcher instances can be easily cleaned up by calling
 * {@link ThreadPoolDirectoryWatchService#close} on this Factory as it
 * implements the AutoCloseable interface. You should do this in any cleanup
 * process done by your application.
 * </p>
 *
 * @author Daryl Teo <i.am@darylteo.com>
 * @see DirectoryWatcher
 */
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
   * @param dir the path to watch for events.
   * @return a DirectoryWatcher for this path (and all child paths)
   * @throws IOException
   */
  public DirectoryWatcher newWatcher(String dir) throws IOException {
    return newWatcher(Paths.get(dir), null);
  }

  /**
   * <p>
   * Instantiates a new DirectoryWatcher for the path given.
   * </p>
   *
   * @param dir       the path to watch for events.
   * @param separator the file path separator for this watcher
   * @return a DirectoryWatcher for this path (and all child paths)
   * @throws IOException
   */
  public DirectoryWatcher newWatcher(String dir, String separator) throws IOException {
    return newWatcher(Paths.get(dir), separator);
  }

  /**
   * <p>
   * Instantiates a new DirectoryWatcher for the path given.
   * </p>
   *
   * @param dir the path to watch for events.
   * @return a DirectoryWatcher for this path (and all child paths)
   * @throws IOException
   */
  public DirectoryWatcher newWatcher(Path dir) throws IOException {
    return newWatcher(dir, null);
  }

  /**
   * <p>
   * Instantiates a new DirectoryWatcher for the path given.
   * </p>
   *
   * @param dir       the path to watch for events.
   * @param separator the file path separator for this watcher
   * @return a DirectoryWatcher for this path (and all child paths)
   * @throws IOException
   */
  public DirectoryWatcher newWatcher(Path dir, String separator) throws IOException {
    DirectoryWatcher watcher = new DirectoryWatcher(this.watchService, dir, separator);
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
