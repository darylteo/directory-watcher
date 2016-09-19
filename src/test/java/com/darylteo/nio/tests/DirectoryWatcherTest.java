package com.darylteo.nio.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.darylteo.nio.DirectoryChangedSubscriber;
import com.darylteo.nio.DirectoryWatcher;
import com.darylteo.nio.ThreadPoolDirectoryWatchService;
import com.darylteo.nio.DirectoryWatcherSubscriber;

public class DirectoryWatcherTest {

  private ThreadPoolDirectoryWatchService factory;
  private DirectoryWatcher watcher;
  private Path root = Paths.get("watcher_test");

  private static final int LATCH_TIMEOUT = 10;
  private CountDownLatch latch;

  @Before
  public void before() throws IOException {
    System.out.println("\nRunning Test");

    resetTestFolder(root);

    factory = new ThreadPoolDirectoryWatchService();
    watcher = factory.newWatcher(root);
  }

  @After
  public void after() throws Exception {
    factory.close();
    System.out.println("\nTest Complete");
  }

  private void initLatch(int count) {
    latch = new CountDownLatch(count);
  }

  private void countdown() {
    synchronized (this) {
      latch.countDown();
      this.notifyAll();
    }
  }

  private void awaitLatch() {
    synchronized (this) {
      while (true) {
        try {
          System.out.println(latch.getCount());
          if (latch.getCount() == 0) {
            System.out.println("Finish");
            return;
          }

          this.wait(5000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  public void resetTestFolder(Path root) throws IOException {
    if (Files.exists(root)) {
      Files.walkFileTree(root, new FileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }

    Files.createDirectories(root.resolve("empty1/empty2"));
    Files.createDirectories(root.resolve("level1/level2"));

    Files.createFile(root.resolve("file"));
    Files.createFile(root.resolve("level1/file"));
    Files.createFile(root.resolve("level1/level2/file"));

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void testCreateDirectory1() throws InterruptedException, IOException {
    final Path newPath = Paths.get("newdir");
    initLatch(1);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryCreated(DirectoryWatcher watcher, Path dir) {
        assertEquals("Two absolute paths are not equal", newPath, dir);
        countdown();
      }
    });

    Files.createDirectories(root.resolve(newPath));
    awaitLatch();
  }

  @Test
  public void testCreateDirectory2() throws InterruptedException, IOException {
    final Set<Path> paths = new HashSet<>();
    paths.add(Paths.get("newdir1"));
    paths.add(Paths.get("newdir1/newdir2"));

    initLatch(2);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryCreated(DirectoryWatcher watcher, Path dir) {
        assertTrue("Watcher did not return a correct path", paths.remove(dir));
        countdown();
      }
    });

    Files.createDirectories(root.resolve("newdir1/newdir2"));
    awaitLatch();
  }

  @Test
  public void testDeleteDirectory1() throws InterruptedException, IOException {
    /* Basic Deletion Test */
    final Path deletePath = Paths.get("empty1/empty2");
    initLatch(1);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryDeleted(DirectoryWatcher watcher, Path file) {
        System.out.println("Entry Deleted: " + file);
        assertEquals("Watcher did not return a correct path", deletePath, file);
        countdown();
      }
    });

    deleteFile(root.resolve(deletePath));
    awaitLatch();
  }

  @Test
  public void testDeleteDirectory2() throws InterruptedException, IOException {
    /* Nested Deletion Test */
    final Set<Path> paths = new HashSet<>();
    paths.add(Paths.get("empty1"));

    initLatch(1);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryDeleted(DirectoryWatcher watcher, Path file) {
        System.out.println("Entry Deleted: " + file);
        assertTrue("Watcher did not return a correct path", paths.remove(file));
        countdown();
      }
    });

    deleteFileTree(root.resolve("empty1"));
    awaitLatch();
  }

  @Test
  public void testDeleteDirectory3() throws InterruptedException, IOException {
    /* Nested Deletion Test */
    final Set<Path> paths = new HashSet<>();
    paths.add(Paths.get("level1"));

    initLatch(1);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryDeleted(DirectoryWatcher watcher, Path file) {
        System.out.println("Entry Deleted: " + file);
        assertTrue("Watcher did not return a correct path", paths.remove(file));
        countdown();
      }
    });

    deleteFileTree(root.resolve("level1"));
    awaitLatch();
  }

  @Test
  public void testDeleteFile1() throws IOException, InterruptedException {
    /* Delete a file in root */
    final Path deletePath = Paths.get("file");

    initLatch(1);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryDeleted(DirectoryWatcher watcher, Path file) {
        assertEquals("Watcher did not return a correct path", deletePath, file);
        countdown();
      }
    });

    deleteFile(root.resolve(deletePath));
    awaitLatch();
  }

  @Test
  public void testDeleteFile2() throws IOException, InterruptedException {
    /* Delete a file deep in hierarchy */
    final Path deletePath = Paths.get("level1/level2/file");

    initLatch(1);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryDeleted(DirectoryWatcher watcher, Path file) {
        assertEquals("Watcher did not return a correct path", deletePath, file);
        countdown();
      }
    });

    deleteFile(root.resolve(deletePath));
    awaitLatch();
  }

  @Test
  public void testDeleteFile3() throws IOException, InterruptedException {
    /* Delete multiple files */
    final Set<Path> paths = new HashSet<>();
    paths.add(Paths.get("file"));
    paths.add(Paths.get("level1/file"));
    paths.add(Paths.get("level1/level2/file"));

    initLatch(3);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryDeleted(DirectoryWatcher watcher, Path file) {
        assertTrue("Watcher did not return a correct path", paths.remove(file));
        countdown();
      }
    });

    for (Path p : paths) {
      deleteFile(root.resolve(p));
    }

    awaitLatch();
  }

  @Test
  public void testDirectoryChangedSubscriber() throws IOException, InterruptedException {
    /* Modify a single file in root */
    final Path modifyPath = Paths.get("file");

    initLatch(1);

    watcher.subscribe(new DirectoryChangedSubscriber() {
      @Override
      public void directoryChanged(DirectoryWatcher watcher, Path path) {
        assertEquals("Watcher did not return a correct path", modifyPath, path);
        countdown();
      }
    });

    writeToFile(root.resolve(modifyPath));
    awaitLatch();
  }

  @Test
  public void testFileModified1() throws IOException, InterruptedException {
    /* Modify a single file in root */
    final Path modifyPath = Paths.get("file");

    initLatch(1);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryModified(DirectoryWatcher watcher, Path file) {
        assertEquals("Watcher did not return a correct path", modifyPath, file);
        countdown();
      }
    });

    writeToFile(root.resolve(modifyPath));
    awaitLatch();
  }

  @Test
  public void testFileModified2() throws IOException, InterruptedException {
    /* Modify multiple files */
    final Set<Path> paths = new HashSet<>();
    paths.add(Paths.get("file"));
    paths.add(Paths.get("level1/file"));
    paths.add(Paths.get("level1/level2/file"));

    initLatch(3);

    watcher.subscribe(new DirectoryWatcherSubscriber() {
      @Override
      public void entryModified(DirectoryWatcher watcher, Path file) {
        assertTrue("Watcher did not return a correct path", paths.remove(file));
        countdown();
      }
    });

    // Need this to give the polling mechanism time to hook into events
    Thread.sleep(1000);
    for (Path p : paths) {
      writeToFile(root.resolve(p));
    }

    awaitLatch();
  }

  private void deleteFile(final Path path) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Files.delete(path);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void deleteFileTree(final Path path) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              Files.delete(file);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            }

          });
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void writeToFile(Path path) throws IOException {
    Files.write(path, "Hello World!".getBytes());
  }
}