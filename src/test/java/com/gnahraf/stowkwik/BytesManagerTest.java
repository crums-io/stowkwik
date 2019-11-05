/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.io.File;
import java.nio.ByteBuffer;

/**
 * <tt>BytesManager</tt> test. Also, a test <tt>ObjectManager.map</tt> method.
 */
public class BytesManagerTest extends NoBiggiesObjectManagerTest {

  /**
   * @param ext
   */
  public BytesManagerTest() {
    super(".byt");
  }

  @Override
  protected ObjectManager<Mock> makeStore(File dir) {
    
    BytesManager manager = new BytesManager(dir, ext);
    MockCodec codec = new MockCodec();
    
    return ObjectManager.map(
        manager,
        
        b -> {
          b.mark();
          Mock m = codec.read(b);
          b.reset();
          return m;
        },
        
        m -> {
          ByteBuffer b = ByteBuffer.allocate(codec.maxBytes());
          codec.write(m, b);
          b.flip();
          return b;
        });
  }
  
}
