package com.darylteo.nio.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.darylteo.nio.DirectoryWatcher;
import com.darylteo.nio.DirectoryWatcherFactory;

public class DirectoryWatcherFilterTest {

  private DirectoryWatcherFactory factory;
  private DirectoryWatcher watcher;

  @Before
  public void before() throws IOException {
    System.out.println("\nRunning Test");

    factory = new DirectoryWatcherFactory();
    watcher = factory.newWatcher(Paths.get(""));
  }

  @After
  public void after() throws Exception {
    factory.close();
  }

  @Test
  public void testEverything1() throws InterruptedException, IOException {
    /* No Filters */
    assertTrue(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes1() throws InterruptedException, IOException {
    watcher.include("file");

    assertTrue(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes2() throws InterruptedException, IOException {
    watcher.include("file.json");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes3() throws InterruptedException, IOException {
    watcher.include("*.json");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes4() throws InterruptedException, IOException {
    watcher.include("*");

    assertTrue(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes5() throws InterruptedException, IOException {
    watcher.include("**");

    assertTrue(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes6() throws InterruptedException, IOException {
    watcher.include("foo/*");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes7() throws InterruptedException, IOException {
    watcher.include("foo/file.json");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes8() throws InterruptedException, IOException {
    watcher.include("**/file.json");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes9() throws InterruptedException, IOException {
    watcher.include("**/*.json");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes10() throws InterruptedException, IOException {
    watcher.include("foo/**");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes11() throws InterruptedException, IOException {
    watcher.include("foo/**/*.json");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes12() throws InterruptedException, IOException {
    watcher.include("**/bar/*.json");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testIncludes13() throws InterruptedException, IOException {
    watcher.include("**/foo/**/bar/*.json");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testExcludes1() throws InterruptedException, IOException {
    watcher.exclude("**");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testExcludes2() throws InterruptedException, IOException {
    watcher.exclude("*");

    assertFalse(watcher.shouldTrack(Paths.get("file")));
    assertFalse(watcher.shouldTrack(Paths.get("file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testExcludes3() throws InterruptedException, IOException {
    watcher.exclude("foo/*");

    assertTrue(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertTrue(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }

  @Test
  public void testExcludes4() throws InterruptedException, IOException {
    watcher.exclude("foo/**");

    assertTrue(watcher.shouldTrack(Paths.get("file")));
    assertTrue(watcher.shouldTrack(Paths.get("file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/file.json")));
    assertFalse(watcher.shouldTrack(Paths.get("foo/bar/file.json")));
  }
}
