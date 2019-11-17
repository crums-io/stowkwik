/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik.log;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;

/**
 * Logs writes in plain text. Example output:
 * <tt><pre>
 *    .
 *    .
 *  2019-11-15T23:04:04 86d3f3a95c324c9479bd8986968f4327
 *  2019-11-15T23:04:04 067b4e4fdb16fd58aa8a009b9fc0aad2
 *    .
 *    .
 * </pre></tt>
 */
public class PlainTextWriteLog implements WriteLog {
  
  private final FileWriter writer;
  

  /**
   * 
   */
  public PlainTextWriteLog(File file) throws IOException {
    this.writer = new FileWriter(file, true);
  }

  
  @Override
  public synchronized void objectWritten(String id) {
    
    String now = Instant.now().toString();
    try {
      
      writer.write(now, 0, now.length() - 8);
      writer.write(' ');
      writer.write(id);
      writer.write('\n');
      writer.flush();
      
    } catch (IOException iox) {
      throw new UncheckedIOException("while writing '" + id + "'", iox);
    }
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
  
  

}
