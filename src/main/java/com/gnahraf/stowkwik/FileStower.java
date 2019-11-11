/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;

import java.io.File;
import java.io.IOException;

import com.gnahraf.io.DirectoryWatcher;

/**
 * 
 */
public class FileStower extends FileManager implements AutoCloseable {
  
  /**
   * Name of the default stow directory. It's a subdirectory
   * of an instance's {@linkplain #getRootDir() root}.
   */
  public final static String DEFAULT_STOW_DIR = "stow";
  
  
  
  private final DirectoryWatcher watcher;
  

  /**
   * @param dir
   * @param ext
   */
  public FileStower(File dir, String ext) {
    this(dir, ext, DEFAULT_HASH_ALGO);
  }

  /**
   * @param dir
   * @param ext
   */
  public FileStower(File dir, String ext, String hashAlgo) {
    super(dir, ext, hashAlgo, true);
    DirectoryWatcher.Listener mover = new DirectoryWatcher.Listener() {
      
      @Override
      public void fileAdded(File file) {
        write(file);
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
    return new File(getRootDir(), DEFAULT_STOW_DIR);
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
    watcher.joinWatch();
  }

  @Override
  public void close() throws IOException {
    watcher.close();
  }
  

}
