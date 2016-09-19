package com.darylteo.nio;

import java.nio.file.Path;

/**
 * Subclass this class in order to respond to file change events from
 * {@link DirectoryWatcher} instances.
 * 
 * @author Daryl Teo
 * 
 */
public abstract class DirectoryWatcherSubscriber {
  /**
   * Called by the DirectoryWatcher when it detects a new entry.
   * 
   * @param watcher
   *          the source of the event
   * @param entry
   *          the path of the entry
   */
  public void entryCreated(DirectoryWatcher watcher, Path entry) {
  }

  /**
   * Called by the DirectoryWatcher when it detects a deleted entry.
   * 
   * @param watcher
   *          the source of the event
   * @param entry
   *          the path of the entry
   */
  public void entryDeleted(DirectoryWatcher watcher, Path entry) {
  }

  /**
   * Called by the DirectoryWatcher when it detects a change in an entry.
   * 
   * @param watcher
   *          the source of the event
   * @param entry
   *          the path of the entry
   */
  public void entryModified(DirectoryWatcher watcher, Path entry) {
  }
}
