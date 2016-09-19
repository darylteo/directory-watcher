package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
   * @param threadCount
   *          - number of threads to spawn for this factory.
   * @throws IOException
   */
  public ThreadPoolDirectoryWatchService(int threadCount) throws IOException {
    for (int i = 0; i < threadCount; i++) {
      executorService.execute(new WatcherThread());
    }
  }

  /*
   * Thread responsible for the watching logic
   */
  private class WatcherThread implements Runnable {
    @Override
    public void run() {
      while (true) {
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
    executorService.shutdownNow();
    super.close();
  }
}
