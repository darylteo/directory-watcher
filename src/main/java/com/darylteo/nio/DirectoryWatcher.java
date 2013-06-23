package com.darylteo.nio;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.sun.nio.file.SensitivityWatchEventModifier;

public class DirectoryWatcher {
  /* Properties */
  private Path path;
  private WatchService watcher;

  /* Subscriptions */
  private final List<DirectoryWatcherSubscriber> subscribers = new ArrayList<>();

  /* Used to filter files */
  private final List<Pattern> includes = new LinkedList<>();
  private final List<Pattern> excludes = new LinkedList<>();

  /* Used to determine watch status */
  private final Set<WatchKey> keys = new HashSet<>();
  private final Set<Path> dirs = new HashSet<>();

  /* Constructors */
  DirectoryWatcher(final WatchService watcher, final Path path) throws IOException {
    this.path = path.toAbsolutePath();
    this.watcher = watcher;

    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        register(dir, watcher);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /* Properties */
  public Path getPath() {
    return this.path;
  }

  /* WatchService */
  private void register(Path path, final WatchService watcher) throws IOException {
    path = path.toAbsolutePath();

    keys.add(path.register(
      watcher,
      new WatchEvent.Kind<?>[] {
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY
      },
      new WatchEvent.Modifier[] { SensitivityWatchEventModifier.HIGH }
      ));

    dirs.add(path);
  }

  private void deregister(WatchKey key, Path path) {
    keys.remove(key);
    dirs.remove(path);
  }

  /* Subscriptions */
  List<DirectoryWatcherSubscriber> getSubscribers() {
    return this.subscribers;
  }

  public void subscribe(DirectoryWatcherSubscriber subscriber) {
    subscribers.add(subscriber);
  }

  public void unsubscribe(DirectoryWatcherSubscriber subscriber) {
    subscribers.remove(subscriber);
  }

  /* Filters */
  public void include(String filter) {
    includes.add(compileFilter(filter));
  }

  public void exclude(String filter) {
    excludes.add(compileFilter(filter));
  }

  private Pattern compileFilter(String filter) {
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
    return Pattern.compile(pattern.toString());
  }

  /* Filter Checking */
  public boolean shouldTrack(Path path) {
    return shouldTrack(path.toString());
  }

  public boolean shouldTrack(String path) {
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

  /* Handlers */
  void handleCreateEvent(WatchKey key, Path path) {
    if (!isTrackingKey(key)) {
      return;
    }

    path = actualPath(key, path);

    // if a new dir is created we need to register it to our watcher
    // else inner events won't be tracked. In some cases, we may only
    // receive an event for the top level dir: any further nested dir
    // will not have any event as we haven't registered them. We'll
    // need to manually traverse and make sure we got them too.
    if (Files.isDirectory(path)) {
      FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          dir = relativePath(dir);
          directoryCreated(dir);

          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          file = relativePath(file);
          fileCreated(file);

          return FileVisitResult.CONTINUE;
        }
      };

      try {
        Files.walkFileTree(path, visitor);
      } catch (IOException e) {
      }
    }
  }

  void handleModifyEvent(WatchKey key, Path path) {
    if (!isTrackingKey(key)) {
      return;
    }

    path = actualPath(key, path);
    if (Files.isDirectory(path)) {
      return;
    }

    fileModified(relativePath(path));
  }

  void handleDeleteEvent(WatchKey key, Path path) {
    if (!isTrackingKey(key)) {
      return;
    }

    path = actualPath(key, path);

    // ignore directories
    if (dirs.contains(path)) {
      return;
    }

    fileDeleted(relativePath(path));
  }

  void handleKeyInvalid(WatchKey key) {
    if (!isTrackingKey(key)) {
      return;
    }

    Path dir = relativePath(((Path) key.watchable()));
    directoryDeleted(dir);
  }

  void directoryCreated(Path dir) throws IOException {
    for (DirectoryWatcherSubscriber sub : subscribers) {
      sub.directoryCreated(DirectoryWatcher.this, dir);
    }
  }

  void directoryDeleted(Path dir) {
    for (DirectoryWatcherSubscriber sub : subscribers) {
      sub.directoryDeleted(this, dir);
    }
  }

  void fileCreated(Path file) {
    for (DirectoryWatcherSubscriber sub : subscribers) {
      sub.fileCreated(this, file);
    }
  }

  void fileModified(Path file) {
    for (DirectoryWatcherSubscriber sub : subscribers) {
      sub.fileModified(this, file);
    }
  }

  void fileDeleted(Path file) {
    for (DirectoryWatcherSubscriber sub : subscribers) {
      sub.fileDeleted(this, file);
    }
  }

  private boolean isTrackingKey(WatchKey key) {
    return keys.contains(key);
  }

  private Path actualPath(WatchKey key, Path path) {
    return ((Path) key.watchable()).resolve(path);
  }

  private Path relativePath(Path path) {
    return this.path.relativize(path);
  }
}