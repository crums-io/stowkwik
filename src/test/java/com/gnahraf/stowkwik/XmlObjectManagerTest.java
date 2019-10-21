/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;


import java.io.File;

/**
 * 
 */
public class XmlObjectManagerTest extends ObjectManagerTest {
  
  public XmlObjectManagerTest() {
    super(".xml");
  }
  
  
  @Override
  protected XmlObjectManager<Mock> makeStore(File dir) {
    return new XmlObjectManager<>(dir, ext, new MockEncoder(), Mock.class);
  }

}
