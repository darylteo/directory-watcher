package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * The DirectoryWatcherFactory is responsible for creating instances of
 * {@link DirectoryWatcher} for you. By default, each factory is configured with
 * an ExecutorService with a single execution thread. You may increase the
 * number of threads with the appropriate contructor.
 * </p>
 * <p>
 * Every DirectoryWatcher that is created by this Factory will use the same
 * WatchService to receive file system events. If this is not desirable,
 * instantiate a separate DirectoryWatcherFactory.
 * </p>
 * <p>
 * All created DirectoryWatcher instances can be easily cleaned up by calling
 * {@link DirectoryWatcherFactory#close} on this Factory as it implements the
 * AutoCloseable interface. You should do this in any cleanup process done by
 * your application.
 * </p>
 * 
 * @author Daryl Teo <i.am@darylteo.com>
 * @see DirectoryWatcher
 */
public class DirectoryWatcherFactory implements AutoCloseable {
  private final WatchService watchService = FileSystems.getDefault().newWatchService();
  private final ExecutorService executorService = Executors.newCachedThreadPool();

  /* Watcher Mapping */
  private List<DirectoryWatcher> watchers = new LinkedList<>();

  /**
   * <p>
   * Instantiates a DirectoryWatcherFactory with a single thread.
   * </p>
   * 
   * @throws IOException
   */
  public DirectoryWatcherFactory() throws IOException {
    this(1);
  }

  /**
   * <p>
   * Instantiates a DirectoryWatcherFactory with a provided thread count.
   * </p>
   * 
   * @param threadCount
   *          - number of threads to spawn for this factory.
   * @throws IOException
   */
  public DirectoryWatcherFactory(int threadCount) throws IOException {
    for (int i = 0; i < threadCount; i++) {
      executorService.execute(new WatcherThread());
    }
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
    DirectoryWatcher watcher = new DirectoryWatcher(watchService, dir);
    watchers.add(watcher);

    return watcher;
  }

  /**
   * <p>
   * Closes and destroys all DirectoryWatchers spawned from this Factory by
   * closing the internal WatchService and ExecutorService.
   * </p>
   */
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
