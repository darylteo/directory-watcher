package com.darylteo.nio;

import java.io.IOException;

/**
 * <p>
 * This implementation of DirectoryWatchService provides a mechanism for polling for changes.
 * </p>
 * 
 * @author Daryl Teo
 * @see AbstractDirectoryWatchService
 * @see DirectoryWatcher
 */
public class PollingDirectoryWatchService extends AbstractDirectoryWatchService {

  public PollingDirectoryWatchService() throws IOException {
    super();
  }

  /**
   * Notifies all subscribers or any file system changes (if any)
   */
  public void poll() {
    super.handleWatchKey(super.getWatchService().poll());
  }
}
