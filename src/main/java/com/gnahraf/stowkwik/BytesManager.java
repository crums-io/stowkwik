/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import com.gnahraf.io.Channels;
import com.gnahraf.io.CorruptionException;

/**
 * 
 */
public class BytesManager extends BaseHashedObjectManager<ByteBuffer> {
  
  private final int maxBytes;
  
  
  
  public BytesManager(File dir, String ext) {
    this(dir, ext, DEFAULT_HASH_ALGO);
  }

  public BytesManager(File dir, String ext, String hashAlgo) {
    this(dir, ext, hashAlgo, 1024*1024);
  }

  public BytesManager(File dir, String ext, String hashAlgo, int maxBytes) {
    super(dir, ext, hashAlgo);
    this.maxBytes = maxBytes;
    if (maxBytes < 16)
      throw new IllegalArgumentException("maxBytes: " + maxBytes);
  }

  @Override
  protected ByteBuffer readObjectFile(File file) throws UncheckedIOException {
    return loadByteBuffer(file);
  }

  @Override
  protected void writeObjectFile(File file, ByteBuffer object, ByteBuffer buffer) throws UncheckedIOException {
    Channels.writeToNewFile(file, buffer);
  }

  @Override
  protected ByteBuffer toByteBuffer(ByteBuffer object) {
    return object;
  }

  
  @Override
  protected int maxBytes() {
    return maxBytes;
  }

  @Override
  protected void validateFile(File file, ByteBuffer object, ByteBuffer buffer) throws CorruptionException {
    validateFileAgainstBuffer(file, buffer);
  }

}