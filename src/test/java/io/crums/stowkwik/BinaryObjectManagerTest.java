/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik;


import java.io.File;

/**
 * 
 */
public class BinaryObjectManagerTest extends NoBiggiesObjectManagerTest {
  
  public BinaryObjectManagerTest() {
    super(".bino");
  }
  

  @Override
  protected BinaryObjectManager<Mock> makeStore(File dir) {
    return new BinaryObjectManager<>(dir, ext, new MockCodec());
  }

}
