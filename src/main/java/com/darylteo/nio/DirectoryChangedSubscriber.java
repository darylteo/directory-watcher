package com.darylteo.nio;

import java.nio.file.Path;

/**
 * A convenience subclass of {@link DirectoryWatcherSubscriber} for responding
 * to all events.
 * 
 * @author Daryl Teo <i.am@darylteo.com>
 * 
 */
public abstract class DirectoryChangedSubscriber extends DirectoryWatcherSubscriber {
  /**
   * Called by the DirectoryWatcher when it detects any changes.
   * 
   * @param watcher
   *          the source of the event
   * @param entry
   *          the path of the entry
   */
  public abstract void directoryChanged(DirectoryWatcher watcher, Path entry);

  @Override
  public void entryCreated(DirectoryWatcher watcher, Path entry) {
    directoryChanged(watcher, entry);
  }

  @Override
  public void entryDeleted(DirectoryWatcher watcher, Path entry) {
    directoryChanged(watcher, entry);
  }

  @Override
  public void entryModified(DirectoryWatcher watcher, Path entry) {
    directoryChanged(watcher, entry);
  }
}