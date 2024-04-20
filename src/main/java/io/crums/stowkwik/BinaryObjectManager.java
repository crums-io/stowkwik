/*
 * Copyright 2017 Babak Farhang
 */
package io.crums.stowkwik;


import java.io.File;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import io.crums.stowkwik.io.Channels;
import io.crums.stowkwik.io.CorruptionException;

/**
 * A binary encoded object manager.
 */
public class BinaryObjectManager<T> extends HashedObjectManager<T> {
  
  private final Codec<T> codec;

  /**
   * Creates a new instance with given binary codec.
   * 
   * @param dir   store directory
   * @param ext   filename extension (w/o the dot) used to store objects of type {@code <T>}
   * @param codec serialization interface for type {@code <T>}
   */
  public BinaryObjectManager(File dir, String ext, Codec<T> codec) {
    super(dir, ext, codec);
    this.codec = codec;
  }
  
  
  
  
  

  /**
   * Creates a new instance with given binary codec.
   * 
   * @param dir   store directory
   * @param ext   filename extension (w/o the dot) used to store objects of type {@code <T>}
   * @param codec serialization interface for type {@code <T>}
   * @param hashAlgo the hashing algorithm, e.g. SHA-256
   */
  public BinaryObjectManager(File dir, String ext, Codec<T> codec, String hashAlgo) {
    super(dir, ext, codec, hashAlgo);
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
