/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik.log;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.gnahraf.io.Files;
import com.gnahraf.stowkwik.BaseHashedObjectManager;

/**
 * 
 */
public class WriteLogs {
  
  private WriteLogs() {  }
  
  
  public final static String LOG_DIR = "log";
  public final static String WLOG_PREFIX = "wlog";
  public final static String WLOG_PLAINTEXT_EXT = ".txt";
  public final static String WLOG_TABLE_EXT = ".tbl";
  
  
  public static File declarePlainTextLogFile(File dir, String ext) {
    return declareLogFile(dir, ext, WLOG_PLAINTEXT_EXT);
  }
  
  
  public static boolean hasPlainTextLogFile(BaseHashedObjectManager<?> manager) {
    return
        declarePlainTextLogFile(manager.getRootDir(), manager.getFileExtension())
        .isFile();
  }
  
  
  
  public static WriteLog newPlainTextWriteLog(BaseHashedObjectManager<?> manager) throws UncheckedIOException {
    try {
      File logFile = declarePlainTextLogFile(manager.getRootDir(), manager.getFileExtension());
      return new PlainTextWriteLog(logFile);
    } catch (IOException iox) {
      throw new UncheckedIOException(iox);
    }
  }
  
  
  
  private static File declareLogFile(File dir, String ext, String logTypeExt) {
    if (ext == null || ext.isEmpty())
      throw new IllegalArgumentException("empty ext: '" + ext + "'");
    File logDir = new File(dir, LOG_DIR);
    Files.ensureDir(logDir);
    return new File(logDir, WLOG_PREFIX + ext + logTypeExt);
  }
  

}
