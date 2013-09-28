package com.darylteo.nio.tests;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import com.darylteo.nio.DirectoryWatcher;
import com.darylteo.nio.DirectoryWatcherFactory;
import com.darylteo.nio.DirectoryWatcherSubscriber;

public class TestRunner {
  public static void main(String[] args) throws Exception {
    DirectoryWatcherFactory factory = new DirectoryWatcherFactory();
    DirectoryWatcher watcher = factory.newWatcher("watcher_test");

    watcher.exclude("dir/");

    CountDownLatch latch = new CountDownLatch(10);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryCreated(DirectoryWatcher watcher, Path entry) {
        System.out.println("Created: " + entry);
      }

      @Override
      public void entryModified(DirectoryWatcher watcher, Path entry) {
        System.out.println("Modified: " + entry);
      }

      @Override
      public void entryDeleted(DirectoryWatcher watcher, Path entry) {
        System.out.println("Deleted: " + entry);
      }
    });

    latch.await();
    factory.close();
  }
}
