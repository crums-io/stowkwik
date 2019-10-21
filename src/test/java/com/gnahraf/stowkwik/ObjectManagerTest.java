/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;


import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

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
  public void testStreaming01_256() {
    Object label = new Object() { };

//    System.out.println("========= " + method(label) + " =========");
    
    ObjectManager<Mock> store = makeStore(label);
    
    TreeMap<String, Mock> book = new TreeMap<>();
    
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
    
//    System.out.println("book size: " + book.size());
    
//    final Set<String> ids = book.keySet();
    store.streamIds().forEach(
        id -> {
//          System.out.println("strm>" + id + "<");
//          System.out.println("book>" + book.firstKey() + "<");
            assertNotNull(book.remove(id));
          });
    
//    System.out.println("book size: " + book.size());
    assertTrue(book.isEmpty());
  }
  
  
  @Test
  public void testStreaming02_64k() {
    Object label = new Object() { };
    String bigtestFlag = "bigtest";
    String perfFlag = "perftest";
    boolean bigtest = isFlagged(bigtestFlag);
    boolean perf = isFlagged(perfFlag);
    if (!bigtest && !perf) {
      System.out.println("Skipping " + method(label) + "; to run set -D" + bigtestFlag + "=true or -D" + perfFlag + "=true");
      System.out.println("If you do so, make sure to *clean right away* since this will create a lot of test files");
      return;
    };
    System.out.println("Running " + method(label) + " (-D" + (perf ? perfFlag : bigtestFlag) + "=true)");
    System.out.println("***Warning***: Clean build directory often! This creates a lot of files");
    testStreaming(new Object() { }, 64*1024, perf);
  }
  
  
  
  private boolean isFlagged(String flag) {
    return "true".equalsIgnoreCase(System.getProperty(flag));
  }
  
  
  private void testStreaming(Object label, int limit, final boolean perf) {

    //  System.out.println("========= " + method(label) + " =========");
    
    ObjectManager<Mock> store = makeStore(label);
    
    TreeMap<String, Mock> book = new TreeMap<>();
    
    long startMillis = System.currentTimeMillis();
    for (int i = 0; i < limit; ++i) {
      Mock item = new Mock();
      item.c = i;
      String id = store.write(item);
      if (!perf)
        book.put(id, item);
    }
    
    for (String id : book.keySet()) {
      Mock obj = store.read(id);
      assertEquals(book.get(id), obj);
    }
    
    //  System.out.println("book size: " + book.size());
    int[] count = { 0 };
    store.streamIds().forEach(id ->
          {
            count[0]++;
            if (!perf)
              assertNotNull(book.remove(id));
          });
    
    double lapMillis = System.currentTimeMillis() - startMillis;
    System.out.println("lap time: " + (lapMillis / 1000) + " sec");
    assertTrue(book.isEmpty());
    
  }
  
  
  
  
  protected ObjectManager<Mock> makeStore(Object methObj) {
    File dir = getMethodOutputFilepath(methObj);
    return makeStore(dir);
  }
  
  
  protected abstract ObjectManager<Mock> makeStore(File dir);

}
