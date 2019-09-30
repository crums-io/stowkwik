/*
 * Copyright 2017 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;


/**
 * Marshals back an instance of an object encoded by this contract.
 */
public interface Codec<T> extends Encoder<T> {
  
  T read(ByteBuffer src) throws BufferUnderflowException;

}
