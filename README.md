# Directory Watcher for Java 7

Watches for changes in files and folders recursively. Features Ant style filtering patterns.

## Distribution

### Maven
```xml
<dependency>
  <groupId>com.darylteo</groupId>
  <artifactId>directory-watcher</artifactId>
  <version>1.1.1</version>
</dependency>
````

### Gradle
```groovy
dependencies {
  compile 'com.darylteo~directory-watcher~1.1.1'
}
````

## Example

```java
public class Example {
  public static void main(String[] args) {
    // Get Watcher
    DirectoryWatcherFactory factory = new DirectoryWatcherFactory();
    DirectoryWatcher watcher = factory.newWatcher("");
    
    // Configure 
    watcher.include("src/**");
    watcher.exclude("bin");
    watcher.exclude("build");
    watcher.exclude("bin/**");
    watcher.exclude("build/**");
    
    // Subscribe
    watcher.subscribe(new DirectoryChangedSubscriber() {
      public void directoryChanged(DirectoryWatcher watcher, Path path) {
        System.out.println("Something changed! " + path.toString());
      }
    });
    
    // Cleanup
    factory.close();
  }
}

````

## Documentation
[Javadoc](http://darylteo.github.io/directory-watcher/javadoc/current/)

