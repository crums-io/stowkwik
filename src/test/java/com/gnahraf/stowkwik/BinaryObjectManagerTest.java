/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;


import com.gnahraf.io.FilepathGenerator;

/**
 * 
 */
public class BinaryObjectManagerTest extends ObjectManagerTest {
  
  public BinaryObjectManagerTest() {
    super(".bino");
  }
  

  
  protected BinaryObjectManager<Mock> makeStore(FilepathGenerator convention) {
    return new BinaryObjectManager<>(convention, new MockCodec());
  }

}
