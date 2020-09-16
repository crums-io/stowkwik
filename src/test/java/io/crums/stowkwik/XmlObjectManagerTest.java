/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik;


import java.io.File;

import io.crums.stowkwik.XmlObjectManager;

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
