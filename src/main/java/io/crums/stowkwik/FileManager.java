/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik;


import static io.crums.util.IntegralStrings.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

import io.crums.stowkwik.io.CorruptionException;

/**
 * 
 */
public class FileManager extends BaseHashedObjectManager<File> {
  
  private final boolean moveOnWrite;

  /**
   * Creates a <em>move-on-write</em>, MD5 instance; {@code this(dir, ext, DEFAULT_HASH_ALGO, true)}
   * 
   * @see FileManager#FileManager(File, String, String, boolean)
   */
  public FileManager(File dir, String ext) {
    this(dir, ext, DEFAULT_HASH_ALGO, true);
  }

  /**
   * Creates an MD5 instance; {@code this(dir, ext, DEFAULT_HASH_ALGO, moveOnWrite)}
   * 
   * @see FileManager#FileManager(File, String, String, boolean)
   */
  public FileManager(File dir, String ext, boolean moveOnWrite) {
    this(dir, ext, DEFAULT_HASH_ALGO, moveOnWrite);
  }

  /**
   * Full param contructor.
   * 
   * @param dir         the root directory. If doesn't exist it's created.
   * @param ext         the file name extension (multiple stores with different
   *                    extensions on the same are possible on the same root
   *                    directory
   * @param hashAlgo    the name of the cryptographic hashing algorithm
   *                    (suitable for {@linkplain MessageDigest#getInstance(String)}).
   *                    E.g. {@code MD5}, {@code SHA-1}, {@code SHA-256}, ..
   * @param moveOnWrite if {@code true} then input files are <em>moved</em> on writes;
   *                    o.w. input files are <em>copied</em> on writes.
   * 
   * @see BaseHashedObjectManager#DEFAULT_HASH_ALGO
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

  @SuppressWarnings("resource")
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
        buffer = ByteBuffer.allocate(Math.min((int) bytes, 4 * 1024));
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
