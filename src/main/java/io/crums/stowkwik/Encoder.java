/*
 * Copyright 2017 Babak Farhang
 */
package io.crums.stowkwik;


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
   * Encodes the state of the given {@code item} in the specified destination buffer {@code dtn}.
   */
  void write(T item, ByteBuffer dtn) throws BufferOverflowException;
  
  /**
   * Returns the maximum number of bytes written to the destination buffer.
   */
  int maxBytes();
  
  
  /**
   * Determines whether items are written in a self-delimited way. This condition is
   * typically satisfied.
   * 
   * @return {@code true}
   */
  default boolean isSelfDelimiting() {
    return true;
  }
}
