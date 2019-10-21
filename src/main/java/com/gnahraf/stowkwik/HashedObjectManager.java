/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.io.File;
import java.nio.ByteBuffer;

/**
 * A file-per-object storage manager. The ID of each object is
 * determined by a cryptographic hash (MD5 suffices) of its contents.
 *
 * @param T the type of object managed.
 */
public abstract class HashedObjectManager<T> extends BaseHashedObjectManager<T> {
  

  private final Encoder<T> encoder;
  
  protected HashedObjectManager(File dir, String ext, Encoder<T> encoder) {
    this(dir, ext, encoder, DEFAULT_HASH_ALGO);
  }

  /**
   * @param dir         the root directory
   * @param ext         filename extension used by entries
   * @param encoder     binary encoder for computing the hash of the object's contents
   */
  protected HashedObjectManager(File dir, String ext, Encoder<T> encoder, String hashAlgo) {
    super(dir, ext, hashAlgo);
    this.encoder = encoder;
    
    if (encoder == null)
      throw new IllegalArgumentException("null encoder");
  }
  

  @Override
  protected ByteBuffer toByteBuffer(T object) {
    ByteBuffer buffer = allocateBuffer(encoder.maxBytes());
    encoder.write(object, buffer);
    buffer.flip();
    return buffer;
  }

  
  
  @Override
  protected int maxBytes() {
    return encoder.maxBytes();
  }

  
}
