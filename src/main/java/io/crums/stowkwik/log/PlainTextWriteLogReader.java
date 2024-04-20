/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik.log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

import io.crums.io.channels.ChannelUtils;
import io.crums.stowkwik.io.CorruptionException;
import io.crums.util.IntegralStrings;

/**
 * 
 */
public class PlainTextWriteLogReader
    extends AbstractList<PlainTextWriteLogReader.LogEntry>
    implements RandomAccess, Channel {
  
  
  /**
   * A plain text log entry. Ordered by timestamp.
   */
  public final static class LogEntry implements Comparable<LogEntry> {
    
    private final static String SEARCH_HEX = "00";
    
    /**
     * String representation of time unless this instance is a search key.
     * Never null.
     */
    public final String timestamp;
    
    /**
     * Hexadecimal id.
     */
    public final String hex;
    
    private LogEntry(String timestamp, String hex) {
      this.timestamp = timestamp;
      this.hex = hex;
    }

    @Override
    public int compareTo(LogEntry o) {
      return timestamp.compareTo(o.timestamp);
    }

    @Override
    public int hashCode() {
      return timestamp.hashCode() ^ hex.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this)
        return true;
      else if (!(obj instanceof LogEntry))
        return false;
      LogEntry other = (LogEntry) obj;
      return hex.equals(other.hex) && timestamp.equals(other.timestamp);
    }
    
    
    public boolean isSearchKey() {
      return SEARCH_HEX.equals(hex);
    }
    
    

    
    public static LogEntry searchKey(String timestamp) {
      Objects.requireNonNull(timestamp);
      return new LogEntry(timestamp, SEARCH_HEX);
    }
  }
  
  
  private final static int MAX_ENTRY_WIDTH = 1024;
  
  
  private final Object lock = new Object();
  private final File path;
  private final FileChannel file;
  private int size;
  private int entryWidth;
  private ByteBuffer rowBuffer;
  

  /**
   * 
   */
  @SuppressWarnings("resource")
  public PlainTextWriteLogReader(File logFile) throws IOException, CorruptionException {
    this.path = Objects.requireNonNull(logFile);
    this.file = new RandomAccessFile(path, "r").getChannel();
    
    long byteSize = file.size();
    if (byteSize != 0) {
      this.entryWidth = readEntryWidth(0);
      if (entryWidth != 0) {
        if (byteSize > maxReadbleFileSize()) {
          file.close();
          throw new IllegalArgumentException(
              path + " size (" + byteSize + ") overflow; entryWidth " + entryWidth);
        }
        updateSize();
      }
    }
  }
  
  
  public boolean update() throws UncheckedIOException {
    try {
      synchronized (lock) {
        return updateSize();
      }
    } catch (IOException iox) {
      throw new UncheckedIOException(iox);
    }
  }
  
  
  private boolean updateSize() throws IOException {
    if (entryWidth == 0 && (entryWidth = readEntryWidth(0)) == 0)
      return false;
    int newSize = (int) (Math.min(maxReadbleFileSize(), file.size()) / entryWidth);
    if (newSize == this.size)
      return false;
    
    // we're gonna update the size..
    
    // check the last entry for alignment
    int w = readEntryWidth(offset(newSize - 1));
    if (w != entryWidth) {
      file.close();
      throw new CorruptionException("Misalignment at offest <= " + offset(newSize - 1) + " in " + path);
    }
    // if this is the first time there's anything to read
    if (size == 0)
      rowBuffer = ByteBuffer.allocate(entryWidth);
    size = newSize;
    
    return true;
  }
  
  
  private long offset(int entry) {
    return entry * entryWidth;
  }
  
  
  private long maxReadbleFileSize() {
    return ((long) entryWidth) * Integer.MAX_VALUE;
  }
  
  
  
  private int readEntryWidth(long offset) throws IOException {
    file.position(offset);
    int w = 0;
    boolean found = false;
    int sepIndex = -1;
    ByteBuffer buffer = ByteBuffer.allocate(128);
    
    while (file.read(buffer) != -1 && w < MAX_ENTRY_WIDTH) {
      buffer.flip();
      
      while (buffer.hasRemaining() && !found) {
        char c = (char) buffer.get();
        found = (PlainTextWriteLog.ENTRY_END == c);
        if (c == PlainTextWriteLog.TIME_HASH_DELIMIT)
          sepIndex = w;
        
        ++w;
      }
      buffer.clear();
      
      if (found) {
        if (sepIndex < 5 || sepIndex > w - 5) {
          file.close();
          throw new CorruptionException("around offset " + offset + " in " + path);
        }
        return w;
      }
    }
    if (w >= MAX_ENTRY_WIDTH)
      throw new CorruptionException("entry overflow beyond offset " + offset + " in " + path);
    
    return 0;
  }

  @Override
  public boolean isOpen() {
    return file.isOpen();
  }

  @Override
  public void close() throws IOException {
    file.close();
  }

  @Override
  public LogEntry get(int index) throws UncheckedIOException {
    
    String timestamp, hex;
    try {
      
      synchronized (lock) {
        
        if (index < 0 || index >= size)
          throw new IndexOutOfBoundsException(index);
        
        file.position(offset(index));
        rowBuffer.clear();
        ChannelUtils.readRemaining(file, rowBuffer);
        rowBuffer.flip();
        StringBuilder work = new StringBuilder(64);
        for (char c = (char) rowBuffer.get(); c != PlainTextWriteLog.TIME_HASH_DELIMIT; ) {
          work.append(c);
          c = (char) rowBuffer.get();
        }
        timestamp = work.toString();
        work.setLength(0);
        while (rowBuffer.hasRemaining())
          work.append((char) rowBuffer.get());
        
        int len = work.length();
        boolean ok = work.charAt(len - 1) == PlainTextWriteLog.ENTRY_END;
        work.setLength(len - 1);
        hex = work.toString();
        ok &= IntegralStrings.isLowercaseHex(hex);
        
        if (!ok) {
          file.close();
          throw new CorruptionException(
              "on reading index " + index + " (" + timestamp + "/" + hex + ")");
        }
      }
      
    } catch (IOException iox) {
      throw new UncheckedIOException("on reading index " + index, iox);
    }
    
    return new LogEntry(timestamp, hex);
  }
  
  
  
  public List<LogEntry> listFrom(String timestamp) throws UncheckedIOException {
    LogEntry key = LogEntry.searchKey(timestamp);
    int index = Collections.binarySearch(this, key);
    if (index < 0)
      index = -1 - index;
    int sz = size();
    if (index == sz)
      return Collections.emptyList();
    else if (index == 0)
      return this;
    else
      return subList(index, sz);
  }
  

  
  
  public static LogEntry searchKey(String timestamp) {
    return LogEntry.searchKey(timestamp);
  }

  @Override
  public int size() {
    synchronized (lock) {
      return size;
    }
  }

}
