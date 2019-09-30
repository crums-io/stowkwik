/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;


import com.gnahraf.io.FilepathGenerator;

/**
 * 
 */
public class XmlObjectManagerTest extends ObjectManagerTest {
  
  public XmlObjectManagerTest() {
    super(".xml");
  }
  
  
  
  protected XmlObjectManager<Mock> makeStore(FilepathGenerator convention) {
    return new XmlObjectManager<>(convention, new MockEncoder(), Mock.class);
  }

}
