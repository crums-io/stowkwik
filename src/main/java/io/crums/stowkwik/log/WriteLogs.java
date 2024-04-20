/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik.log;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import io.crums.stowkwik.BaseHashedObjectManager;
import io.crums.io.FileUtils;
import io.crums.stowkwik.io.HexPath;

/**
 * 
 */
public class WriteLogs {
  
  private WriteLogs() {  }
  
  /**
   * Name of the log subdirectory under root.
   */
  public final static String LOG_DIR = "log";
  public final static String WLOG_PREFIX = "wlog";
  public final static String WLOG_PLAINTEXT_EXT = ".txt";
  public final static String WLOG_TABLE_EXT = ".tbl";
  
  
  /**
   * Returns a path to the log file for the for the given root {@code dir} and extension {@code ext}.
   * On return the log subdirectory is guaranteed to exist; the log file itself is not created, so it
   * may or may not exist.
   * 
   * @see #LOG_DIR
   */
  public static File declarePlainTextLogFile(File dir, String ext) {
    return declareLogFile(dir, ext, WLOG_PLAINTEXT_EXT);
  }
  
  
  /**
   * Determines whether there's an existing plain text log file.
   * <p>
   * FIXME: Note this has a side effect I didn't bother to fix: creates the log subdirectory.
   * </p>
   */
  public static boolean hasPlainTextLogFile(BaseHashedObjectManager<?> manager) {
    return
        declarePlainTextLogFile(manager.getRootDir(), manager.getFileExtension())
        .isFile();
  }
  

  /**
   * Determines whether there's an existing plain text log file.
   * <p>
   * FIXME: Note this has a side effect I didn't bother to fix: creates the log subdirectory.
   * </p>
   */
  public static boolean hasPlainTextLogFile(HexPath hexPath) {
    return
        declarePlainTextLogFile(hexPath.getRoot(), hexPath.getFileExtension())
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
  
  
  public static PlainTextWriteLogReader newPlainTextWriteLogReader(File dir, String ext) throws UncheckedIOException {
    try {
      File logFile = declarePlainTextLogFile(dir, ext);
      return new PlainTextWriteLogReader(logFile);
    } catch (IOException iox) {
      throw new UncheckedIOException(iox);
    }
  }
  
  
  
  private static File declareLogFile(File dir, String ext, String logTypeExt) {
    if (ext == null || ext.isEmpty())
      throw new IllegalArgumentException("empty ext: '" + ext + "'");
    File logDir = new File(dir, LOG_DIR);
    FileUtils.ensureDir(logDir);
    return new File(logDir, WLOG_PREFIX + ext + logTypeExt);
  }
  

}
