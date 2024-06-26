/*
 * Copyright 2017 Babak Farhang
 */
package io.crums.stowkwik;


import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;


/**
 * Marshals back an instance of an object encoded by this contract.
 */
public interface Codec<T> extends Encoder<T> {
  
  /**
   * Reads and returns an object marshalled from the given {@code src} buffer.
   * On return the position of the buffer is advanced.
   *
   * @param src
   * @return
   * @throws BufferUnderflowException
   */
  T read(ByteBuffer src) throws BufferUnderflowException;
  

}
