package com.darylteo.nio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DirectoryWatcher {
  private final List<DirectoryWatcherSubscriber> subscribers = new ArrayList<>();

  private Path path;

  /* Used to filter files */
  private final List<Pattern> includes = new LinkedList<>();
  private final List<Pattern> excludes = new LinkedList<>();

  /* Used to determine watch status */
  private final Set<WatchKey> dirs = new HashSet<>();

  /* Constructors */
  DirectoryWatcher(Path path) throws IOException {
    this.path = path;
  }

  /* Properties */
  public Path getPath() {
    return this.path;
  }

  List<DirectoryWatcherSubscriber> getSubscribers() {
    return this.subscribers;
  }

  /* Subscriptions */
  public void subscribe(DirectoryWatcherSubscriber subscriber) {
    subscribers.add(subscriber);
  }

  public void unsubscribe(DirectoryWatcherSubscriber subscriber) {
    subscribers.remove(subscriber);
  }

  /* WatchKeys */
  boolean watch(WatchKey key) {
    return this.dirs.add(key);
  }

  boolean unwatch(WatchKey key) {
    return this.dirs.remove(key);
  }

  boolean isWatching(WatchKey key) {
    return dirs.contains(key);
  }

  /* Filters */
  public void include(String filter) {
    includes.add(compileFilter(filter));
  }

  public void exclude(String filter) {
    excludes.add(compileFilter(filter));
  }

  private Pattern compileFilter(String filter) {
    System.out.println("\nFilter: " + filter);

    if (filter.endsWith("/") || filter.endsWith("\\")) {
      filter = filter + "**";
    }

    String[] subs = filter.split("[/|\\\\]");
    StringBuilder pattern = new StringBuilder("^");
    boolean appendDelimiter = false;

    for (String sub : subs) {
      if (appendDelimiter) {
        pattern.append(File.separator);
      } else {
        appendDelimiter = true;
      }

      if (sub.equals("**")) {
        pattern.append(".*?");
        appendDelimiter = false;
      } else {
        pattern.append(sub
          .replace(".", "\\.")
          .replace("?", ".")
          .replace("*", "[^" + File.separator + "]*?")
          );
      }
    }

    pattern.append("$");

    Pattern result = Pattern.compile(pattern.toString());
    System.out.println(result);

    return result;
  }

  /* Filter Checking */
  public boolean shouldTrack(Path path) {
    return isTracking(path.toString());
  }

  public boolean isTracking(String path) {
    return shouldInclude(path) && !shouldExclude(path);
  }

  private boolean shouldInclude(String path) {
    if (includes.isEmpty()) {
      return true;
    }

    for (Pattern pattern : includes) {
      if (pattern.matcher(path).matches()) {
        return true;
      }
    }

    return false;
  }

  private boolean shouldExclude(String path) {
    if (excludes.isEmpty()) {
      return false;
    }

    for (Pattern pattern : excludes) {
      if (pattern.matcher(path).matches()) {
        return true;
      }
    }

    return false;
  }

}