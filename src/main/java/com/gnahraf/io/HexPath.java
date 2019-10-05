/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.io;


import static com.gnahraf.util.IntStrings.*;

import java.io.File;
import java.util.Locale;

/**
 * 
 */
public class HexPath {
  
  
  private final File dir;
  private final FilenameScheme convention;
  private final int maxFilesPerDir;
  
  
  
  public HexPath(File dir, String ext, int maxFilesPerDir) {
    this.dir = dir;
    this.convention = new FilenameScheme(null, ext);
    this.maxFilesPerDir = maxFilesPerDir;
    
    if (maxFilesPerDir < 256) {
      throw new IllegalArgumentException("maxFilesPerDir: " + maxFilesPerDir);
    }
    
    if (dir == null)
      throw new IllegalArgumentException("dir " + dir);
    if (!dir.isDirectory()) {
      if (dir.exists())
        throw new IllegalArgumentException("not a directory: " + dir);
      makeDirectory(dir);
    }
  }
  
  
  public File find(String hex) {
    hex = canonicalizeHex(hex);
    
    File file = new File(dir, convention.toFilename(hex));
    if (file.exists())
      return ensureFile(file);
    
    // loop invariant: hdir is an existing directory
    File hdir = dir;
    while (hex.length() > 2) {
      
      File subdir = subdirOrNull(hdir, hex);
      if (subdir == null)
        break;
      
      // udpate
      hdir = subdir;
      hex = hex.substring(2);
      
      file = new File(hdir, convention.toFilename(hex));
      
      if (file.exists())
        return ensureFile(file);
      
    }
    return null;
  }
  
  
  public File suggest(String hex) {
    return suggest(hex, false);
  }
  
  public File suggest(String hex, boolean makeParentDir) {
    hex = canonicalizeHex(hex);
    
    // find the deepest existing subdir matching *hex

    // loop invariant: hdir is an existing directory
    File hdir = dir;
    while (hex.length() > 2) {
      
      File subdir = subdirOrNull(hdir, hex);
      if (subdir == null)
        break;
      
      // udpate
      hdir = subdir;
      hex = hex.substring(2);
      
    }
    
    if (hdir.list().length >= maxFilesPerDir && hex.length() > 2) {
      hdir = new File(hdir, hex.substring(0, 2));
      hex = hex.substring(2);
      
      if (makeParentDir)
        makeDirectory(hdir);
    }
    
    return new File(hdir, convention.toFilename(hex));
  }
  
  
  
  
  
  
  
  private File ensureFile(File file) {
    if (!file.isFile())
      throw new IllegalStateException("not a file: " + file);
    return file;
  }
  
  private File subdirOrNull(File hdir, String hex) {
    File subdir = new File(hdir, hex.substring(0, 2));
    if (subdir.isDirectory())
      return subdir;
    
    if (subdir.exists())
      throw new IllegalStateException("not a subdir: " + subdir);
    
    return null;
  }
  
  private void makeDirectory(File subdir) {
      if (!subdir.mkdirs() && !subdir.isDirectory())
        throw new IllegalArgumentException("failed to create directory " + subdir);
  }

}
