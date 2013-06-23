package com.darylteo.nio;

import java.nio.file.Path;

public abstract class DirectoryChangedSubscriber extends DirectoryWatcherSubscriber {
  public abstract void directoryChanged(DirectoryWatcher watcher, Path path);

  @Override
  public void entryCreated(DirectoryWatcher watcher, Path dir) {
    directoryChanged(watcher, dir);
  }

  @Override
  public void entryDeleted(DirectoryWatcher watcher, Path file) {
    directoryChanged(watcher, file);
  }

  @Override
  public void entryModified(DirectoryWatcher watcher, Path file) {
    directoryChanged(watcher, file);
  }
}