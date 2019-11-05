/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;


import static com.gnahraf.util.IntegralStrings.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

import com.gnahraf.io.CorruptionException;

/**
 * 
 */
public class FileManager extends BaseHashedObjectManager<File> {
  
  private final boolean moveOnWrite;

  /**
   * 
   * @param dir
   * @param ext
   * @param moveOnWrite
   */
  public FileManager(File dir, String ext, boolean moveOnWrite) {
    this(dir, ext, DEFAULT_HASH_ALGO, moveOnWrite);
  }

  /**
   * @param dir
   * @param ext
   * @param hashAlgo
   * @param moveOnWrite
   */
  public FileManager(File dir, String ext, String hashAlgo, boolean moveOnWrite) {
    super(dir, ext, hashAlgo);
    this.moveOnWrite = moveOnWrite;
  }

  /**
   * Doesn't depend on this method.
   */
  @Override
  protected int maxBytes() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected File readObjectFile(File file) {
    return file;
  }

  @Override
  protected void validateFile(File file, File object, ByteBuffer buffer) throws CorruptionException {
    boolean ok = file.length() == object.length() && toByteBuffer(file).equals(buffer);
    if (!ok)
      throw new CorruptionException(file.toString());
  }

  @Override
  protected void writeObjectFile(File file, File object, ByteBuffer buffer) throws UncheckedIOException {
    if (moveOnWrite) {
      if (!object.renameTo(file) && !file.equals(object))
        throw new IllegalArgumentException("failed to move " + object + " to " + file);
    } else try {
      Files.copy(object.toPath(), file.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    } catch (IOException iox) {
      throw new UncheckedIOException("on copying " + object + " to " + file, iox);
    }
  }

  @Override
  protected String signature(ByteBuffer buffer) {
    return toHex(buffer);
  }

  @Override
  protected ByteBuffer toByteBuffer(File object) throws UncheckedIOException {
    
    ByteBuffer buffer;
    {
      long bytes = object.length();
      if (bytes == 0)
        throw new IllegalArgumentException("empty file " + object);
      if (bytes > 1024 * 1024)
        buffer = ByteBuffer.allocateDirect(64 * 1024);
      if (bytes > 128  * 1024)
        buffer = ByteBuffer.allocateDirect(8 * 1024);
      else
        buffer = ByteBuffer.allocate(Math.min((int) bytes, 4096));
    }

    MessageDigest digest = threadLocalDigest();
    
    int nullReads = 0;
    
    try (FileChannel channel = new FileInputStream(object).getChannel()) {
      
      while (channel.read(buffer) != -1) {
        buffer.flip();
        
        if (buffer.hasRemaining())
          digest.update(buffer);
        
        // unhappy path..
        else if (++nullReads > 1024) {
          String offset;
          try {
            offset = Long.toString(channel.position());
          } catch (IOException wow) {
            offset = "unknown";
          }
          throw new IOException("null read sanity check failing. Offset: " + offset);
        }
        
        buffer.clear();
      }
      
    } catch (IOException iox) {
      throw new UncheckedIOException("on reading " + object, iox);
    }
    
    
    return ByteBuffer.wrap(digest.digest());
  }

}
