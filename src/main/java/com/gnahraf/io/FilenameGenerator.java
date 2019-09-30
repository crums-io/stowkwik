/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.io;


import java.io.File;
import java.io.FilenameFilter;

import javax.xml.bind.DatatypeConverter;

/**
 * Represents a prefix / extension file naming scheme.
 */
public class FilenameGenerator {
  
  private final static int MAX_FILENAME_LENGTH = 255;
  
  private final String prefix;
  private final String extension;
  private final int decorationLength;
  private final int maxByteIdentifierLength;

  /**
   * 
   */
  public FilenameGenerator(String prefix, String extension) {
    this.prefix = prefix == null ? "" : prefix;
    this.extension = extension == null ? "" : extension;
    
    this.decorationLength = this.prefix.length() + this.extension.length();
    this.maxByteIdentifierLength = (MAX_FILENAME_LENGTH - decorationLength) / 2;
    
    if (maxByteIdentifierLength < 8)
    	throw new IllegalArgumentException("too long: " + getPrefix() + "/" + getExtension() + "<");
    if (decorationLength == 0)
    	throw new IllegalArgumentException("why bother: no prefix, nor extension");
  }
  
  
  
  
  
  
  public String toFilename(String identifier) {
    
    if (identifier == null || identifier.isEmpty())
      throw new IllegalArgumentException("identifier " + identifier + "<");
    
    return prefix + identifier + extension;
  }
  
  
  
  public String toHexFilename(byte[] identifier) {
    
    if (identifier == null || identifier.length == 0)
      throw new IllegalArgumentException("identifier " + identifier);
    if (identifier.length + maxByteIdentifierLength > 128)
      throw new IllegalArgumentException("byte array length too long for a file name: " + identifier.length);
    return prefix + DatatypeConverter.printHexBinary(identifier) + extension;
  }
  
  
  
  
  

  public final String getPrefix() {
    return prefix;
  }

  public final String getExtension() {
    return extension;
  }
  
  
  
  public boolean accept(String filename) {
    return
    	filename.length() > decorationLength &&
    	  filename.startsWith(prefix) && filename.endsWith(extension);
  }
  
  
  
  public FilenameFilter getFilenameFilter() {
    return new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return FilenameGenerator.this.accept(name);
      }
    };
  }
  
  

}
