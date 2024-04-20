/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik.io;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

//import com.sun.nio.file.SensitivityWatchEventModifier;

import io.crums.util.Isomorphism;
import io.crums.util.Sets;

/**
 * This class has been stubbed out while a proper fix is found,
 * for using the non-standard (and hard to package)
 * {@code import com.sun.nio.file.SensitivityWatchEventModifier}
 * type. The packaging problems relate to the module-info.java
 * file.
 */
public class DirectoryWatcher implements AutoCloseable {
  
  
  public interface Listener {
    void fileAdded(File file);
  }
  

  private final Map<Path, WatchKey> watchKeys = new HashMap<>();
  
  private final Listener dispatcher;
  private final WatchService fs;
  private final Thread thread;
  

  /**
   * 
   */
  public DirectoryWatcher(Listener dispatcher) {
    Objects.requireNonNull(dispatcher, "dispatcher");
    
    
    this.dispatcher = dispatcher;
    try {
      fs = FileSystems.getDefault().newWatchService();
    } catch (IOException iox) {
      throw new UncheckedIOException(iox);
    }
    Runnable loop = new Runnable() {
      public void run() {
        loop();
      }
    };
    this.thread = new Thread(loop, getClass().getSimpleName());
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
  }
  
  
  public synchronized boolean addDirectory(File dir) {
    if (!dir.isDirectory())
      throw new IllegalArgumentException("not an existing dir " + dir);
    
    Path path = dir.toPath();
    {
      WatchKey watchKey = watchKeys.get(path);
      if (watchKey != null) {
        if (watchKey.isValid())
          return false; // idempotent
        else
          watchKeys.remove(path);
      }
    }
    try {
      WatchKey watchKey = path.register(
          fs,
          new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE });
//      WatchKey watchKey = path.register(
//          fs,
//          new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE },
//          SensitivityWatchEventModifier.HIGH );
      watchKeys.put(path, watchKey);
      return true;
    } catch (IOException iox) {
      throw new UncheckedIOException(iox);
    }
  }
  
  
  public synchronized Set<File> getDirectories() {
    return Sets.map(watchKeys.keySet(), Isomorphism.PATH_TO_FILE);
  }
  
  
  public synchronized boolean removeDirectory(File dir) {
    WatchKey watchKey = watchKeys.remove(dir.toPath());
    boolean removed = watchKey != null;
    if (removed)
      watchKey.cancel();
    return removed;
  }
  
  
  
  private void loop() {
    while (true) {
      
      WatchKey watchKey;
      try {
        watchKey = fs.take();
      } catch (InterruptedException ix) {
//        ix.printStackTrace();
        // normal
        return;
      } catch (ClosedWatchServiceException cwsx) {
        // normal
        return;
      }
      
      for (WatchEvent<?> event: watchKey.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind == StandardWatchEventKinds.OVERFLOW)
          continue;
        
        Path path = (Path) event.context();
        Path dir = (Path) watchKey.watchable();
        Path child = dir.resolve(path);
        dispatcher.fileAdded(child.toFile());
      }
      watchKey.reset();
    }
  }
  
  
  public void joinWatch() throws InterruptedException {
    thread.join();
  }


  @Override
  public void close() throws IOException {
    fs.close();
  }

}
