/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.test;


import java.io.File;

/**
 *
 */
public class TestOutputFiles {
  
  public final static String TARGET = "target";
  public final static String TEST_OUTPUTS = "test-outputs";
  

  private final File root;
  
  
  public TestOutputFiles() {
    this(new File("."));
  }
  
  
  
  /**
   * 
   */
  public TestOutputFiles(File context) {
    if (!context.isDirectory())
      throw new IllegalArgumentException("context must be an existing directory: " + context);
    this.root = new File(context, TARGET + File.separator + TEST_OUTPUTS);
    if (!root.mkdirs() && !root.isDirectory())
      throw new IllegalArgumentException("failed to create test output directory " + root);
  }
  
  
  
  public File getOutputPath(Class<?> clazz) {
    if (clazz.isArray())
      throw new IllegalArgumentException(clazz.getName());
    return new File(root, clazz.getName());
  }

}
