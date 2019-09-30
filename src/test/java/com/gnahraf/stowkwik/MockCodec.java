/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;


class MockCodec extends MockEncoder implements Codec<Mock> {
  
  public Mock read(ByteBuffer src) throws BufferUnderflowException {
    Mock obj = new Mock();
    obj.a = src.getDouble();
    obj.b = src.getDouble();
    obj.c = src.getInt();
    return obj;
  }

}
