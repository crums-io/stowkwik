/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.io;


import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * File name generator anchored to a directory.
 */
public class FilepathGenerator extends FilenameGenerator {
  
  private final File dir;

  
  /**
   * Creates a new instance. If <tt>dir</tt> does not exist, the constructor
   * creates the directory.
   */
  public FilepathGenerator(File dir, String prefix, String extension) {
    this(dir, prefix, extension, true);
  }
  
  /**
   * Creates a new instance in the specified parent directory.
   * 
   * @param ensureDirectory  if <tt>true</tt>, then <tt>dir</tt> is guaranteed to exist
   *                         on return
   */
  public FilepathGenerator(File dir, String prefix, String extension, boolean ensureDirectory) {
    super(prefix, extension);
    this.dir = dir;
    if (dir == null)
      throw new IllegalArgumentException("dir " + dir);
    if (ensureDirectory && !dir.isDirectory()) {
      if (dir.exists())
        throw new IllegalArgumentException("not a directory: " + dir);
      if (!dir.mkdirs() && !dir.isDirectory())
        throw new IllegalArgumentException("failed to create directory " + dir);
    }
  }
  
  
  
  
  
  
  public File toHexFilepath(byte[] identifier) {
    return new File(dir, toHexFilename(identifier));
  }
  
  
  public File toFilepath(String identifier) {
    return new File(dir, toFilename(identifier));
  }
  
  
  
  public final File getDirectory() {
    return dir;
  }
  
  
  
  public List<String> listIdentifiers() {
    
    final String[] filenames = dir.list(getFilenameFilter());
    if (filenames == null || filenames.length == 0)
      return Collections.emptyList();
    
    return
        new AbstractList<String>() {
      
          @Override
          public String get(int index) {
            return toIdentifier(filenames[index]);
          }
    
          @Override
          public int size() {
            return filenames.length;
          }
        };
  }
  
  
  private String toIdentifier(String filename) {
    int start = getPrefix().length();
    int end = filename.length() - getExtension().length();
    return filename.substring(start, end);
  }
  
  
  
  public Stream<String> streamIdentifiers() {
    try {
      return
          Files.list(dir.toPath())
            .filter(path -> accept(path.getFileName().toString()))
            .map(path -> toIdentifier(path.getFileName().toString()));
    } catch (IOException iox) {
      throw new UncheckedIOException(iox);
    }
    
  }

}
