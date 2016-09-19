package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * This implementation of DirectoryWatchService automatically waits for changes
 * using a Thread Pool.
 * </p>
 *
 * @author Daryl Teo
 * @see AbstractDirectoryWatchService
 * @see DirectoryWatcher
 */
public class ThreadPoolDirectoryWatchService extends AbstractDirectoryWatchService {

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final List<WatcherThread> watcherThreads = new ArrayList<>();

  /**
   * <p>
   * Instantiates a DirectoryWatcherFactory with a single thread.
   * </p>
   *
   * @throws IOException
   */
  public ThreadPoolDirectoryWatchService() throws IOException {
    this(1);
  }

  /**
   * <p>
   * Instantiates a DirectoryWatcherFactory with a provided thread count.
   * </p>
   *
   * @param threadCount - number of threads to spawn for this factory.
   * @throws IOException
   */
  public ThreadPoolDirectoryWatchService(int threadCount) throws IOException {
    for (int i = 0; i < threadCount; i++) {
      WatcherThread thread = new WatcherThread();
      watcherThreads.add(thread);
      executorService.execute(thread);
    }
  }

  /*
   * Thread responsible for the watching logic
   */
  private class WatcherThread implements Runnable {

    private AtomicBoolean stop = new AtomicBoolean(false);

    public WatcherThread() {
    }

    public void setStop(boolean stop) {
      this.stop.set(stop);
    }

    @Override
    public void run() {
      while (!stop.get()) {
        try {
          ThreadPoolDirectoryWatchService.super.handleWatchKey(ThreadPoolDirectoryWatchService.super.getWatchService().take());
        } catch (InterruptedException | ClosedWatchServiceException e) {
          return;
        }
      }
    }
  }

  @Override
  public void close() throws Exception {
    super.close();
    executorService.shutdownNow();
    for (WatcherThread thread : watcherThreads) {
      thread.setStop(true);
    }
  }
}