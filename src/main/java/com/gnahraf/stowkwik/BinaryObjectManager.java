/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.io.File;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import com.gnahraf.io.Channels;
import com.gnahraf.io.CorruptionException;
import com.gnahraf.io.FilepathGenerator;

/**
 * A binary encoded object manager.
 */
public class BinaryObjectManager<T> extends HashedObjectManager<T> {
  
  private final Codec<T> codec;

  /**
   * @param hashedPath
   * @param encoder
   */
  public BinaryObjectManager(FilepathGenerator hashedPath, Codec<T> codec) {
    super(hashedPath, codec);
    this.codec = codec;
  }
  
  
  
  
  

  @Override
  protected T readObjectFile(File file) throws UncheckedIOException {
    ByteBuffer buffer = allocateBuffer(codec.maxBytes());
    Channels.readFully(file, buffer);
    buffer.flip();
    return codec.read(buffer);
  }

  
  @Override
  protected void writeObjectFile(File file, T object, ByteBuffer buffer) throws UncheckedIOException {
    Channels.writeToNewFile(file, buffer);
  }

  
  
  @Override
  protected void validateFile(File file, T object, ByteBuffer buffer) throws CorruptionException {
    boolean fail = buffer.remaining() != file.length();
    if (fail)
      throw new CorruptionException(file.toString());
    
    ByteBuffer contents = allocateBuffer(buffer.remaining());
    Channels.readFully(file, contents);
    contents.flip();
    
    fail = !contents.equals(buffer);
    if (fail)
      throw new CorruptionException(file.toString());
  }
  
  
  

}
