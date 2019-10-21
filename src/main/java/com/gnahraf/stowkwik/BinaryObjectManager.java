/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.io.File;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import com.gnahraf.io.Channels;
import com.gnahraf.io.CorruptionException;

/**
 * A binary encoded object manager.
 */
public class BinaryObjectManager<T> extends HashedObjectManager<T> {
  
  private final Codec<T> codec;

  /**
   * @param hashedPath
   * @param encoder
   */
  public BinaryObjectManager(File dir, String ext, Codec<T> codec) {
    super(dir, ext, codec);
    this.codec = codec;
  }
  
  
  
  
  

  @Override
  protected T readObjectFile(File file) throws UncheckedIOException {
    ByteBuffer buffer = loadByteBuffer(file);
    return codec.read(buffer);
  }

  
  @Override
  protected void writeObjectFile(File file, T object, ByteBuffer buffer) throws UncheckedIOException {
    Channels.writeToNewFile(file, buffer);
  }

  
  
  @Override
  protected void validateFile(File file, T object, ByteBuffer buffer) throws CorruptionException {
    validateFileAgainstBuffer(file, buffer);
  }
  
  
  

}
