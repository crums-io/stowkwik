/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

class MockEncoder implements Encoder<Mock> {

  @Override
  public void write(Mock item, ByteBuffer dtn) throws BufferOverflowException {
    dtn.putDouble(item.a).putDouble(item.b).putInt(item.c);
  }

  @Override
  public int maxBytes() {
    return 20;
  }
}