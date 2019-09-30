/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * Contract for encoding (writing) the state of an object into a memory buffer. We
 * distinguish this operation from reading back (decoding) the object, because when
 * used only as a way to compute a cryptographic hash of an object's state, there's
 * no need to read it back.
 * 
 * @see Codec
 */
public interface Encoder<T> {
  
  /**
   * Encodes the state of the given <tt>item</tt> in the specified destination buffer <tt>dtn</tt>.
   */
  void write(T item, ByteBuffer dtn) throws BufferOverflowException;
  
  /**
   * Returns the maximum number of bytes written to the destination buffer.
   */
  int maxBytes();
}
