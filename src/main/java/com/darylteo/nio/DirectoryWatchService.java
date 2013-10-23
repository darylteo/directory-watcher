package com.darylteo.nio;

import java.io.IOException;
import java.nio.file.Path;

public interface DirectoryWatchService {
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
  public DirectoryWatcher newWatcher(String dir) throws IOException;

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
  public DirectoryWatcher newWatcher(Path dir) throws IOException;
}