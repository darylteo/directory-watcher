package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
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
 * {@link ThreadPoolDirectoryWatchService#close} on this Factory as it
 * implements the AutoCloseable interface. You should do this in any cleanup
 * process done by your application.
 * </p>
 * 
 * @author Daryl Teo <i.am@darylteo.com>
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
}
