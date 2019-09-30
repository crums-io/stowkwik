/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.test;

import java.io.File;

import com.gnahraf.io.IntPathnameGenerator;

/**
 * A convention for output file paths for unit tests. We don't want running
 * tests twice without cleaning to fail. This works by numbering paths per
 * invocation in ascending order at the leaves of the directory structure.
 * <p/>
 * The other approach would be to assign a <em>global</em> root directory
 * per invocation of tests. That looks cleaner but it has the downside you
 * now have to keep track of what a run of a suite of tests is (which will
 * depend on assumptions about the test harness). Way too brittle, imo.
 */
public class IoTestCase extends SelfAwareTestCase {
  
  
  public final File outputDir;

  /**
   * 
   */
  public IoTestCase() {
    TestOutputFiles testDirs = new TestOutputFiles();
    this.outputDir = testDirs.getOutputPath(getClass());
    outputDir.mkdirs();
    if (!outputDir.isDirectory())
      throw new IllegalStateException("failed to create test output dir " + outputDir);
  }
  
  
  
  public File getMethodOutputDir(Object innerMethodObject) {
    File dir = new File(outputDir, method(innerMethodObject));
    dir.mkdir();
    if (!dir.isDirectory())
      throw new IllegalStateException(
          "failed to create test method output directory " + dir);
    
    return dir;
  }
  
  /**
   * Returns a new file path for this run of the test.
   */
  public File getMethodOutputFilepath(Object innerMethodObject) {
    return getMethodOutputFilepath(innerMethodObject, "RUN-", null);
  }
  
  public File getMethodOutputFilepath(Object innerMethodObject, String prefix) {
    return getMethodOutputFilepath(innerMethodObject, prefix, null);
  }
  
  public File getMethodOutputFilepath(Object innerMethodObject, String prefix, String postfix) {
    File dir = getMethodOutputDir(innerMethodObject);
    return new IntPathnameGenerator(dir, prefix, postfix).newPath();
  }

}
