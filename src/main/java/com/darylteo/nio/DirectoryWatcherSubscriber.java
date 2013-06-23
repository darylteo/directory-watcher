package com.darylteo.nio;

import java.nio.file.Path;

public abstract class DirectoryWatcherSubscriber {
  public void entryCreated(DirectoryWatcher watcher, Path dir) {
  }

  public void entryDeleted(DirectoryWatcher watcher, Path file) {
  }

  public void entryModified(DirectoryWatcher watcher, Path file) {
  }
}
