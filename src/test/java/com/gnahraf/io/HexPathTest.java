/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.io;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.gnahraf.test.IoTestCase;

/**
 * Note we expect randomized inputs to HexPath. The inputs here are highly contrived.
 */
public class HexPathTest extends IoTestCase {
  
  public final static String EXT = ".ext";
  
  public final static List<String> HEXSPACE;
  
  static {
    ArrayList<String> hexspace = new ArrayList<>(256);
    for (int i = 0; i < 16; ++i)
      hexspace.add("0" + Integer.toHexString(i));
    for (int i = 16; i < 256; ++i)
      hexspace.add(Integer.toHexString(i));
    
    HashSet<String> validator = new HashSet<>();
    for (String hex : hexspace) {
      assertEquals(2, hex.length());
      validator.add(hex);
    }
    assertEquals(256, validator.size());
    
    
    HEXSPACE = Collections.unmodifiableList(hexspace);
  }
  
  
  
  
  @Test
  public void test0Empty() {
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
  public void test1One() throws IOException {
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
  public void test258Depth1() throws IOException {
    Object label = new Object() { };
    File dir = getMethodOutputFilepath(label);
    
    String anomalous = "0100";
    HexPath hexPath = new HexPath(dir, EXT, 256);
    
    // (Not testing the default behavior, so we set the boolean param explicitly)
    File file = hexPath.suggest(anomalous, true);
    assertEquals(dir, file.getParentFile());
    assertTrue(file.createNewFile());
    
    File firstFile = file;
    
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
    
    assertEquals(firstFile, hexPath.find(anomalous));
  }
  
  
  @Test
  public void test514Depth2() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    String anomalous = "00100";
    HexPath hexPath = new HexPath(dir, EXT, 256);
    
    // (Not testing the default behavior, so we set the boolean param
    // explicitly--altho here it doesn't matter)
    File file = hexPath.suggest(anomalous, true);
    assertEquals(dir, file.getParentFile());
    assertTrue(file.createNewFile());

    
    final File firstFile = file;
    
    for (int i = 0; i < 255; ++i) {
      file = hexPath.suggest("000" + HEXSPACE.get(i), false);
      assertEquals(dir, file.getParentFile());
      assertTrue(file.createNewFile());
    }
    
    file = hexPath.suggest("000" + HEXSPACE.get(255), false);
//    System.out.println("=======" + method(label) + "=======");
//    System.out.println(file);
    assertEquals(dir, file.getParentFile().getParentFile());
    assertEquals("00", file.getParentFile().getName());
    
    assertTrue(file.getParentFile().mkdir());
    
    assertTrue(file.createNewFile());
    
    assertEquals(file, hexPath.find("000ff"));
    
    final File first00 = file;
    
    file = hexPath.suggest("00101", true);
    
    assertEquals(dir, file.getParentFile().getParentFile());
    assertEquals("00", file.getParentFile().getName());
    assertTrue(file.getParentFile().isDirectory());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find("00101"));
    
    final String outOfSeq = "00200";
    
    // fill the subdirectory
    File subdir = file.getParentFile();

    file = hexPath.suggest(outOfSeq, true);
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find(outOfSeq));
    
    final File oosFile = file;
    
    
    for (int i = 2; i < 255; ++i) {
      String hex = "001" + HEXSPACE.get(i);
      file = hexPath.suggest(hex, false);
      assertEquals(subdir, file.getParentFile());
      assertTrue(file.createNewFile());
      assertEquals(file, hexPath.find(hex));
    }

    // subdir "00" is filled to the brim.. make it overflow
    file = hexPath.suggest("001ff", true);
    assertEquals("1f", file.getParentFile().getName());
    assertTrue(file.createNewFile());
    

    assertEquals(firstFile, hexPath.find(anomalous));
    assertEquals(first00, hexPath.find("000ff"));
    assertEquals(oosFile, hexPath.find(outOfSeq));
  }
  
  @Test
  public void testToHex() {
    Object label = new Object() { };
    // (we're not touching anything, so all runs can use the same dir)
    File root = getMethodOutputDir(label);
    HexPath hexPath = new HexPath(root, EXT, 256);
    
    File path;
    String expected = "00abc7e88";
    {
      path = new File(root, "00");
      path = new File(path, "ab");
      path = new File(path, "c7");
      path = new File(path, "e88" + EXT);
    }
    
    assertEquals(expected, hexPath.toHex(path));
    
    try {
      path = new File(root.getParentFile(), "00");
      path = new File(path, "ab");
      path = new File(path, "c7");
      path = new File(path, "e88" + EXT);
      hexPath.toHex(path);
      fail();
    } catch (IllegalArgumentException success) {

//      System.out.println("======= " + method(label) + " =======");
//      System.out.println("expected error: " + success);
    }
  }
  
  
  @Test
  public void testOptimize() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    final String prefix = "0011";
    
    HexPath hexPath = new HexPath(dir, EXT, 256);
    
    for (String hex : HEXSPACE) {
      File file = hexPath.suggest(prefix + hex, false);
      assertTrue(file.createNewFile());
    }
    
    // The directory is filled the brim.. tip it over with *anomalous
    final File dir00;
    String anomalous = "002100";
    {
      File file = hexPath.suggest(anomalous, true);
      assertEquals("00", file.getParentFile().getName());
      assertEquals(dir, file.getParentFile().getParentFile());
      
      assertTrue(file.createNewFile());
      
      dir00 = file.getParentFile();
    }
    
    for (int i = 0; i < 255; ++i) {
      String hex = prefix + HEXSPACE.get(i);
      File file = hexPath.optimize(hex);
      assertEquals(dir00, file.getParentFile());
    }
    
    // *dir00 is now filled to the brim
    assertEquals(256, dir00.list().length);
    
    // ..if we optimize one more, it should push down it into a '11' subdirectory
    File file = hexPath.optimize(prefix + "ff");
    assertEquals("11", file.getParentFile().getName());
    assertEquals(dir00, file.getParentFile().getParentFile());
    
  }
  
  
  @Test
  public void testFileRenameAssumption() throws IOException {
    File dir = getMethodOutputFilepath(new Object() { });
    assertTrue(dir.mkdir());
    File subdir = new File(dir, "subdir");
    assertTrue(subdir.mkdir());
    File source = new File(dir, "source");
    assertTrue(source.createNewFile());
    assertTrue(source.exists());
    File target = new File(subdir, "target");
    String sName = source.getName();
    source.renameTo(target);
    assertTrue(target.exists());
    assertFalse(source.exists());
    assertEquals(sName, source.getName());
    assertEquals(dir, source.getParentFile());
  }

}
