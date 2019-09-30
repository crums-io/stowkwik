/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;


import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.junit.Test;

import com.gnahraf.io.FilepathGenerator;
import com.gnahraf.test.IoTestCase;


/**
 * Base test suite for <tt>XxxObjectManagerTest</tt>s.
 * <p/>
 * Sorry, don't believe adding cognitive load for understanding test cases. But
 * duplication sucks blood
 * 
 * @see #makeStore(FilepathGenerator)
 */
public abstract class ObjectManagerTest extends IoTestCase {
  
  protected final String ext;
  
  
  
  public ObjectManagerTest(String ext) {
    this.ext = ext;
    if (ext == null || ext.length() == 0)
      throw new IllegalArgumentException("null ext");
  }
  
  @Test
  public void testOne() {
    ObjectManager<Mock> store = makeStore(new Object() { });
    
    // make sure the mock implementation is meaningful
    Mock expected = new Mock();
    expected.a = 0;
    assertNotEquals(new Mock(), expected);
    
    expected = new Mock();
    expected.b = 0;
    assertNotEquals(new Mock(), expected);
    
    expected = new Mock();
    expected.c = 0;
    assertNotEquals(new Mock(), expected);
    
    // okay, the actual test..
    String id = store.write(expected);
    Mock read = store.read(id);
    
    assertEquals(expected, read);
  }
  
  
  @Test
  public void testIdempotence() {
    ObjectManager<Mock> store = makeStore(new Object() { });
    Mock expected = new Mock();
    expected.b = 99;
    expected.c = -124;
    

    String id = store.write(expected);
    Mock read = store.read(id);
    
    assertEquals(expected, read);
    
    assertEquals(id, store.write(read));
  }
  
  
  @Test
  public void test100() {
    ObjectManager<Mock> store = makeStore(new Object() { });
    
    HashMap<String, Mock> book = new HashMap<>();
    
    for (int i = 0; i < 100; ++i) {
      Mock item = new Mock();
      item.c = i;
      String id = store.write(item);
      book.put(id, item);
    }
    
    for (String id : book.keySet()) {
      Mock obj = store.read(id);
      assertEquals(book.get(id), obj);
    }
  }
  
  
  @Test
  public void testStreaming256() {
    ObjectManager<Mock> store = makeStore(new Object() { });
    
    HashMap<String, Mock> book = new HashMap<>();
    
    for (int i = 0; i < 256; ++i) {
      Mock item = new Mock();
      item.c = i;
      String id = store.write(item);
      book.put(id, item);
    }
    
    for (String id : book.keySet()) {
      Mock obj = store.read(id);
      assertEquals(book.get(id), obj);
    }
    
    Set<String> ids = book.keySet();
    store.streamIds().forEach(id -> ids.remove(id));
    assertTrue(book.isEmpty());
  }
  
  
  
  
  protected ObjectManager<Mock> makeStore(Object methObj) {
    File dir = getMethodOutputFilepath(methObj);
    FilepathGenerator convention = new FilepathGenerator(dir, null, ext);
    return makeStore(convention);
  }
  
  
  protected abstract ObjectManager<Mock> makeStore(FilepathGenerator convention);

}
