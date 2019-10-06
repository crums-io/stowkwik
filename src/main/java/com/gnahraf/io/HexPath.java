/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.io;


import static com.gnahraf.util.IntStrings.*;

import java.io.File;

/**
 * 
 */
public class HexPath {
  
  
  private final File root;
  private final FilenameScheme convention;
  private final int maxFilesPerDir;
  
  
  
  public HexPath(File dir, String ext, int maxFilesPerDir) {
    this.root = dir;
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
    String subhex = hex;
    
    // find the deepest matching subdir.. call it *hdir
    
    // loop invariant: hdir is an existing directory
    File hdir = root;
    while (subhex.length() > 2) {
      
      File subdir = subdirOrNull(hdir, subhex);
      if (subdir == null)
        break;
      
      // udpate
      hdir = subdir;
      subhex = subhex.substring(2);
      
    }
    
    while (true) {
      File file = new File(hdir, convention.toFilename(subhex));
      if (file.exists())
        return ensureFile(file);
      if (subhex.equals(hex))
        break;
      
      subhex = hdir.getName() + subhex;
      hdir = hdir.getParentFile();
    }
    
    return null;
  }
  
  
  
  public File findAndOptimize(String hex) {
    File file = find(hex);
    if (file == null)
      return null;
    return optimizeImpl(file, hex);
  }
  
  
  public File suggest(String hex) {
    return suggest(hex, false);
  }
  
  public File suggest(String hex, boolean makeParentDir) {
    hex = canonicalizeHex(hex);
    
    // find the deepest existing subdir matching *hex

    // loop invariant: hdir is an existing directory
    File hdir = root;
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
  
  
  
  public File optimize(String hex) {
    File file = find(hex);
    if (file == null)
      throw new IllegalStateException("no found: " + hex);
    
    return optimizeImpl(file, hex);
  }
  
  
  public File optimize(File file) {
    if (!file.isFile())
      throw new IllegalArgumentException("file does not exist: " + file);
    String hex = toHex(file);
    return optimizeImpl(file, hex);
  }
  
  
  private File optimizeImpl(File file, String hex) {
    File suggestedPath = suggest(hex, true);
    if (!suggestedPath.equals(file)) {
      if (!file.renameTo(suggestedPath))
        throw new IllegalStateException("rename " + file + " --> " + suggestedPath + " failed");
      file = suggestedPath;
    }
    return file;
    
  }
  
  
  
  public String toHex(File file) {
    StringBuilder buffer = new StringBuilder();
    String tail = convention.toIdentifer(file.getName());
    if (!isHex(tail))
      throw new IllegalArgumentException(file.toString());
    
    File dir = file.getParentFile();
    toHexRecurse(dir, buffer);
    return buffer.append(tail).toString();
  }
  
  
  private void toHexRecurse(File dir, StringBuilder buffer) {
    String hex = dir.getName();
    if (!isHex(hex))
      throw new IllegalArgumentException(dir + "/...");
    
    File parent = dir.getParentFile();
    if (parent == null)
      throw new IllegalArgumentException("unmanaged (and unlikely) path " + dir + "/..");
      
    if (!parent.equals(root))
      toHexRecurse(dir.getParentFile(), buffer);
    
    buffer.append(hex);
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
