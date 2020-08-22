/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.io.File;
import java.io.IOException;

import com.gnahraf.io.DirectoryWatcher;
import com.gnahraf.stowkwik.log.WriteLog;
import com.gnahraf.stowkwik.log.WriteLoggedObjectManager;
import com.gnahraf.stowkwik.log.WriteLogs;
import com.gnahraf.util.cc.ThreadUtils;

/**
 * A {@linkplain FileManager} with declarable watch directories (called "stow" directories)
 * from which files can be input. Lo key cross process input mechanism.
 * <p/>
 */
public class FileStower implements AutoCloseable {
  
  /**
   * Name of the default stow directory. It's a subdirectory
   * of an instance's {@linkplain #getRootDir() root}.
   */
  public final static String DEFAULT_STOW_DIR = "stow";

  private final Object closeLock = new Object();

  private final FileManager fileManager;
  private final WriteLoggedObjectManager<File> managerView;
  private final DirectoryWatcher watcher;
  
  

  /**
   * @param dir
   * @param ext
   */
  public FileStower(File dir, String ext) {
    this(dir, ext, FileManager.DEFAULT_HASH_ALGO);
  }

  /**
   * @param dir
   * @param ext
   */
  public FileStower(File dir, String ext, String hashAlgo) {
    this.fileManager= new FileManager(dir, ext, hashAlgo, true);
    WriteLog wlog = WriteLogs.newPlainTextWriteLog(fileManager);
    
    this.managerView = new WriteLoggedObjectManager<>(fileManager, wlog);
    
    
    
    DirectoryWatcher.Listener mover = new DirectoryWatcher.Listener() {
      
      @Override
      public void fileAdded(File file) {
        managerView.write(file);
        // TODO: configurable handler for pre-existing input
        if (file.exists())
          file.delete();
      }
    };
    this.watcher = new DirectoryWatcher(mover);
  }
  
  /**
   * Returns the default stow directory, which is a subdirectory of root.
   * The returned "abstract path name" may not yet exist.
   */
  public File getDefaultStowDirectory() {
    return new File(fileManager.getRootDir(), DEFAULT_STOW_DIR);
  }
  
  
  public boolean addDefaultStowDirectory() {
    return addStowDirectory(defaultStowDir());
  }
  
  
  public boolean addStowDirectory(File stowDirectory) {
    return watcher.addDirectory(stowDirectory);
  }
  
  
  public boolean removeStowDirectory(File stowDirectory) {
    return watcher.removeDirectory(stowDirectory);
  }
  
  
  private File defaultStowDir() {
    File stowDir = getDefaultStowDirectory();
    if (!stowDir.isDirectory() && !stowDir.mkdirs())
      throw new IllegalStateException("failed to mkdir " + stowDir);
    return stowDir;
  }
  
  public void joinClose() throws InterruptedException {
    synchronized (closeLock) {
      while (managerView.isOpen())
        closeLock.wait();
    }
  }

  @Override
  public void close() throws IOException {
    synchronized (closeLock) {
      watcher.close();
      // leave some time for the watcher thread to complete
      try {
        ThreadUtils.ensureSleepMillis(100);
      } catch (InterruptedException ix) {
        // noop
      }
      // close the write-log
      managerView.close();
      closeLock.notifyAll();
    }
  }

}
