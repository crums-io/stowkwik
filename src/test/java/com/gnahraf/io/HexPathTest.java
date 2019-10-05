/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.io;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.gnahraf.test.IoTestCase;

/**
 * 
 */
public class HexPathTest extends IoTestCase {
  
  private final static String EXT = ".ext";
  
  private final static List<String> HEXSPACE;
  
  static {
    ArrayList<String> hexspace = new ArrayList<>(256);
    for (int i = 0; i < 16; ++i)
      hexspace.add("0" + Integer.toHexString(i));
    for (int i = 16; i < 256; ++i)
      hexspace.add(Integer.toHexString(i));
    
    for (String hex : hexspace)
      assertEquals(2, hex.length());
    
    HEXSPACE = Collections.unmodifiableList(hexspace);
  }
  
  
  
  
  @Test
  public void testEmpty() {
    Object label = new Object() { };
    // (we're not touching anything, so all runs can use the same dir)
    HexPath hexPath = new HexPath(getMethodOutputDir(label), EXT, 256);
    try {
      hexPath.find("");
      fail();
    } catch (IllegalArgumentException expected) {   }
    try {
      hexPath.find("0x0");
      fail();
    } catch (IllegalArgumentException expected) {   }
    
    assertFalse(hexPath.suggest("00").exists());
  }
  
  @Test
  public void testOne() throws IOException {
    Object label = new Object() { };
    File dir = getMethodOutputFilepath(label);
    assertFalse(dir.exists());
    
    HexPath hexPath = new HexPath(dir, EXT, 256);
    assertTrue(dir.isDirectory());
    
    int hval = 29;
    String hex = HEXSPACE.get(hval);
    
    File file = hexPath.find(hex);
    assertNull(file);
    
    file = hexPath.suggest(hex);
    assertEquals(dir, file.getParentFile());
    
    assertTrue(file.createNewFile());
    
    File roundtrip = hexPath.find(hex);
    assertEquals(file, roundtrip);
  }
  
  
  @Test
  public void test258() throws IOException {
    Object label = new Object() { };
    File dir = getMethodOutputFilepath(label);
    
    String anomalous = "0100";
    HexPath hexPath = new HexPath(dir, EXT, 256);
    
    // (Not testing the default behavior, so we set the boolean param explicitly)
    File file = hexPath.suggest(anomalous, true);
    assertEquals(dir, file.getParentFile());
    assertTrue(file.createNewFile());
    
    for (int i = 0; i < 255; ++i) {
      file = hexPath.suggest("00" + HEXSPACE.get(i), false);
      assertEquals(dir, file.getParentFile());
      assertTrue(file.createNewFile());
    }
    
    file = hexPath.suggest("00" + HEXSPACE.get(255), false);
//    System.out.println("=======" + method(label) + "=======");
//    System.out.println(file);
    assertEquals(dir, file.getParentFile().getParentFile());
    assertEquals("00", file.getParentFile().getName());
    
    assertTrue(file.getParentFile().mkdir());
    
    assertTrue(file.createNewFile());
    
    assertEquals(file, hexPath.find("00ff"));
    
    file = hexPath.suggest("0101", true);
    assertEquals(dir, file.getParentFile().getParentFile());
    assertEquals("01", file.getParentFile().getName());
    assertTrue(file.getParentFile().isDirectory());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find("0101"));
  }

}
