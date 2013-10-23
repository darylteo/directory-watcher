package com.darylteo.nio;

import java.io.IOException;

public class PollingDirectoryWatchService extends AbstractDirectoryWatchService {

  public PollingDirectoryWatchService() throws IOException {
    super();
  }

  public void poll() {
    super.handleWatchKey(super.getWatchService().poll());
  }
}
