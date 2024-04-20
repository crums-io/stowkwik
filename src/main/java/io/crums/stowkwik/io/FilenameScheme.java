/*
 * Copyright 2017 Babak Farhang
 */
package io.crums.stowkwik.io;


import static io.crums.util.IntegralStrings.isLowercaseHex;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Represents a prefix / extension file naming scheme.
 */
public class FilenameScheme {
  
  private final static int MAX_FILENAME_LENGTH = 255;
  
  private final String prefix;
  private final String extension;
  private final int decorationLength;

  /**
   * 
   */
  public FilenameScheme(String prefix, String extension) {
    this.prefix = prefix == null ? "" : prefix;
    this.extension = extension == null ? "" : extension;
    
    this.decorationLength = this.prefix.length() + this.extension.length();
    
    if (MAX_FILENAME_LENGTH - decorationLength < 8)
    	throw new IllegalArgumentException("too long: " + getPrefix() + "/" + getExtension() + "<");
    if (decorationLength == 0)
    	throw new IllegalArgumentException("why bother: no prefix, nor extension");
  }
  
  
  
  
  
  
  public String toFilename(String identifier) {
    
    if (identifier == null || identifier.isEmpty())
      throw new IllegalArgumentException("identifier " + identifier + "<");
    
    return prefix + identifier + extension;
  }
  
  
  
  public String toIdentifer(String filename) {
    if (!acceptForm(filename))
      throw new IllegalArgumentException(filename);
    return toIdentifierUnchecked(filename);
  }
  
  
  public String toIdentifierUnchecked(String filename) {
    return filename.substring(prefix.length(), filename.length() - extension.length());
  }
  
  
  /**
   * Determines whether the {@linkplain FilenameScheme#toIdentifer(String) identifier}
   * in the given <tt>filename</tt> is a lowercase hexadecimal string.
   */
  public boolean isHexFilename(String filename) {
    return acceptForm(filename) && isLowercaseHex(toIdentifierUnchecked(filename));
  }
  
  

  public final String getPrefix() {
    return prefix;
  }

  public final String getExtension() {
    return extension;
  }
  
  
  /**
   * Determines whether the given <tt>filename</tt> conforms to this naming scheme.
   * 
   * @see #getFilenameFilter()
   */
  public boolean accept(String filename) {
    return acceptForm(filename);
  }
  
  
  protected final boolean acceptForm(String filename) {
    return
        filename.length() > decorationLength &&
          filename.startsWith(prefix) && filename.endsWith(extension);
  }
  
  
  /**
   * Returns a new stateless filter governed by {@linkplain #accept(String)}.
   */
  public FilenameFilter getFilenameFilter() {
    return new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return FilenameScheme.this.accept(name);
      }
    };
  }
  
  

}
