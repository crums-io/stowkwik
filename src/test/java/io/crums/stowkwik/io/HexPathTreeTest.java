/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik.io;


import static io.crums.stowkwik.io.HexPathTest.EXT;
import static io.crums.stowkwik.io.HexPathTest.HEXSPACE;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;

import org.junit.Test;

import io.crums.testing.IoTestCase;

/**
 * 
 */
public class HexPathTreeTest extends IoTestCase {
  
  
  @Test
  public void testCursor00_Empty() {
    Object label = new Object() { };
    // (we're not touching anything, so all runs can use the same dir)
    File root = getMethodOutputDir(label);
    HexPathTree hexPath = new HexPathTree(root, EXT, 256);
    assertFalse(hexPath.newCursor().hasRemaining());
    assertNull(hexPath.newCursor().trySplit());
  }
  
  
  @Test
  public void testCursor01_OneDepth0() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    final String hex = "0c10f5";
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);
    final File file = hexPath.suggest(hex, true);
    assertEquals(hex.substring(0, 2), file.getParentFile().getName());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find(hex));
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    HexPathTree.Entry entry = cursor.getHeadEntry();
    assertEquals(hex, entry.hex);
    assertEquals(file, entry.file);
    
    assertEquals(hex, cursor.getHeadHex());
    assertEquals(file, cursor.getHeadFile());
    
    assertFalse(cursor.consumeNext());
    assertFalse(cursor.hasRemaining());
    
    cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    
    assertEquals(1, cursor.estimateSize());
    assertNull(cursor.trySplit());
    
    boolean advanced = cursor.tryAdvance(
        e -> {
          assertEquals(hex, e.hex);
          assertEquals(file, e.file);
        });
    
    assertTrue(advanced);
    
    assertFalse(cursor.hasRemaining());
    
    advanced = cursor.tryAdvance(e -> fail());
    assertFalse(advanced);
  }
  

  @Test
  public void testCursor02_OneDepth1() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    final String hex = "0c10f5";
    final File subdir0c = new File(dir, "0c");
    assertTrue(subdir0c.mkdirs());
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);

    final File file = hexPath.suggest(hex, true);
    assertEquals(subdir0c, file.getParentFile());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find(hex));
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    HexPathTree.Entry entry = cursor.getHeadEntry();
    assertEquals(hex, entry.hex);
    assertEquals(file, entry.file);
    
    assertEquals(hex, cursor.getHeadHex());
    assertEquals(file, cursor.getHeadFile());
    
    assertFalse(cursor.consumeNext());
    assertFalse(cursor.hasRemaining());
    
    cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    
    assertEquals(1, cursor.estimateSize());
    assertNull(cursor.trySplit());
    
    boolean advanced = cursor.tryAdvance(
        e -> {
          assertEquals(hex, e.hex);
          assertEquals(file, e.file);
        });
    
    assertTrue(advanced);
    
    assertFalse(cursor.hasRemaining());
    
    advanced = cursor.tryAdvance(e -> fail());
    assertFalse(advanced);
  }
  

  @Test
  public void testCursor03_OneDepth1EmptyPreBranch() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    final String hex = "0c10f4";
    final File subdir0c = new File(dir, "0c");
    assertTrue(subdir0c.mkdirs());
    assertTrue(new File(dir, "00").mkdir());
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);

    final File file = hexPath.suggest(hex, true);
    assertEquals(subdir0c, file.getParentFile());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find(hex));
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    HexPathTree.Entry entry = cursor.getHeadEntry();
    assertEquals(hex, entry.hex);
    assertEquals(file, entry.file);
    
    assertEquals(hex, cursor.getHeadHex());
    assertEquals(file, cursor.getHeadFile());
    
    assertFalse(cursor.consumeNext());
    assertFalse(cursor.hasRemaining());
    
    cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    
    assertEquals(1, cursor.estimateSize());
    assertNull(cursor.trySplit());
    
    boolean advanced = cursor.tryAdvance(
        e -> {
          assertEquals(hex, e.hex);
          assertEquals(file, e.file);
        });
    
    assertTrue(advanced);
    
    assertFalse(cursor.hasRemaining());
    
    advanced = cursor.tryAdvance(e -> fail());
    assertFalse(advanced);
  }
  

  @Test
  public void testCursor04_OneDepth1EmptyPreBranch2() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    final String hex = "0c10f3";
    final File subdir0c = new File(dir, "0c");
    assertTrue(subdir0c.mkdirs());
    assertTrue(new File(dir, "00").mkdir());
    assertTrue(new File(dir, "01").mkdir());
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);

    final File file = hexPath.suggest(hex, true);
    assertEquals(subdir0c, file.getParentFile());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find(hex));
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    HexPathTree.Entry entry = cursor.getHeadEntry();
    assertEquals(hex, entry.hex);
    assertEquals(file, entry.file);
    
    assertEquals(hex, cursor.getHeadHex());
    assertEquals(file, cursor.getHeadFile());
    
    assertFalse(cursor.consumeNext());
    assertFalse(cursor.hasRemaining());
    
    cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    
    assertEquals(1, cursor.estimateSize());
    assertNull(cursor.trySplit());
    
    boolean advanced = cursor.tryAdvance(
        e -> {
          assertEquals(hex, e.hex);
          assertEquals(file, e.file);
        });
    
    assertTrue(advanced);
    
    assertFalse(cursor.hasRemaining());
    
    advanced = cursor.tryAdvance(e -> fail());
    assertFalse(advanced);
  }
  

  @Test
  public void testCursor05_OneDepth1EmptyPreBranch3() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    final String hex = "0c10f4";
    final File subdir0c = new File(dir, "0c");
    assertTrue(subdir0c.mkdirs());
    
    assertTrue(new File(new File(dir, "00"), "10").mkdirs());
    assertTrue(new File(dir, "01").mkdir());
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);

    final File file = hexPath.suggest(hex, true);
    assertEquals(subdir0c, file.getParentFile());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find(hex));
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    HexPathTree.Entry entry = cursor.getHeadEntry();
    assertEquals(hex, entry.hex);
    assertEquals(file, entry.file);
    
    assertEquals(hex, cursor.getHeadHex());
    assertEquals(file, cursor.getHeadFile());
    
    assertFalse(cursor.consumeNext());
    assertFalse(cursor.hasRemaining());
    
    cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    
    assertEquals(1, cursor.estimateSize());
    assertNull(cursor.trySplit());
    
    boolean advanced = cursor.tryAdvance(
        e -> {
          assertEquals(hex, e.hex);
          assertEquals(file, e.file);
        });
    
    assertTrue(advanced);
    
    assertFalse(cursor.hasRemaining());
    
    advanced = cursor.tryAdvance(e -> fail());
    assertFalse(advanced);
  }
  

  @Test
  public void testCursor06_OneDepth1EmptyPostBranch() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    final String hex = "0c10f2";
    final File subdir0c = new File(dir, "0c");
    assertTrue(subdir0c.mkdirs());
    assertTrue(new File(dir, "10").mkdir());
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);

    final File file = hexPath.suggest(hex, true);
    assertEquals(subdir0c, file.getParentFile());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find(hex));
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    HexPathTree.Entry entry = cursor.getHeadEntry();
    assertEquals(hex, entry.hex);
    assertEquals(file, entry.file);
    
    assertEquals(hex, cursor.getHeadHex());
    assertEquals(file, cursor.getHeadFile());
    
    assertFalse(cursor.consumeNext());
    assertFalse(cursor.hasRemaining());
    
    cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    
    // note expected error in estimate (we don't read ahead)
    assertEquals(2, cursor.estimateSize());
//    assertEquals(0, cursor.trySplit().estimateSize());  // the split succeeds but is empty
    // post split, the size estimate disregards the empty branch
//    assertEquals(1, cursor.estimateSize());
    
    boolean advanced = cursor.tryAdvance(
        e -> {
          assertEquals(hex, e.hex);
          assertEquals(file, e.file);
        });
    
    assertTrue(advanced);
    
    assertFalse(cursor.hasRemaining());
    
    advanced = cursor.tryAdvance(e -> fail());
    assertFalse(advanced);
  }
  

  @Test
  public void testCursor07_OneDepth1EmptyPostBranch2() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    final String hex = "0c10f1";
    final File subdir0c = new File(dir, "0c");
    assertTrue(subdir0c.mkdirs());
    assertTrue(new File(new File(dir, "10"), "b9").mkdirs());
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);

    final File file = hexPath.suggest(hex, true);
    assertEquals(subdir0c, file.getParentFile());
    assertTrue(file.createNewFile());
    assertEquals(file, hexPath.find(hex));
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    HexPathTree.Entry entry = cursor.getHeadEntry();
    assertEquals(hex, entry.hex);
    assertEquals(file, entry.file);
    
    assertEquals(hex, cursor.getHeadHex());
    assertEquals(file, cursor.getHeadFile());
    
    assertFalse(cursor.consumeNext());
    assertFalse(cursor.hasRemaining());
    
    cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    
    // note expected error in estimate (we don't read ahead)
    assertEquals(2, cursor.estimateSize());
//    assertEquals(0, cursor.trySplit().estimateSize());  // the split succeeds but is empty
    // post split, the size estimate disregards the empty branch
//    assertEquals(1, cursor.estimateSize());
    
    boolean advanced = cursor.tryAdvance(
        e -> {
          assertEquals(hex, e.hex);
          assertEquals(file, e.file);
        });
    
    assertTrue(advanced);
    
    assertFalse(cursor.hasRemaining());
    
    advanced = cursor.tryAdvance(e -> fail());
    assertFalse(advanced);
  }
  

  

  @Test
  public void testCursor08_2Depth0() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    final String[] hexes = { "0c10e0", "0c10ff" } ;
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);
    for (String hex : hexes) {
      File file = hexPath.suggest(hex);
      assertTrue(file.createNewFile());
    }
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    assertEquals(2, cursor.estimateSize());
    
    final int[] index = {0};  // as a pointer to int
    while(cursor.tryAdvance(e -> {
      assertEquals(hexes[index[0]++], e.hex);
      assertEquals(dir, e.file.getParentFile().getParentFile());
    }));
    assertEquals(hexes.length, index[0]);
  }
  

  

  @Test
  public void testCursor09_2Depth1() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    
    final String[] hexes = { "0c1000", "0c10ff" } ;
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);
    // create first entry at depth 0
    {
      File file = hexPath.suggest(hexes[0]);
      assertTrue(file.createNewFile());
    }
    // create an "0c" subdir
    File subdir0c = new File(dir, "0c");
    assertTrue(subdir0c.isDirectory());
    // push next entry at depth 1
    {
      File file = hexPath.suggest(hexes[1], false);
      assertEquals(subdir0c, file.getParentFile());
      assertTrue(file.createNewFile());
    }
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    assertEquals(2, cursor.estimateSize());
    
    final int[] index = {0};  // as a pointer to int
    while(cursor.tryAdvance(e -> assertEquals(hexes[index[0]++], e.hex)));
    assertEquals(hexes.length, index[0]);
  }
  

  

  @Test
  public void testCursor10_2Depth1EmptyPreBranch() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    
    final String[] hexes = { "0c10e0", "0c90f0" } ;
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);
    // create first entry at depth 0
    {
      File file = hexPath.suggest(hexes[0]);
      assertTrue(file.createNewFile());
    }
    // create an "0c" subdir
    File subdir0c = new File(dir, "0c");
    assertTrue(subdir0c.isDirectory());
    // push next entry at depth 1
    {
      File file = hexPath.suggest(hexes[1], false);
      assertEquals(subdir0c, file.getParentFile());
      assertTrue(file.createNewFile());
    }
    // create empty pre-branch
    File subdir00 = new File(dir, "00");
    assertTrue(new File(subdir00 , "11").mkdirs());
    assertTrue(new File(subdir00 , "22").mkdirs());
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    assertEquals(2, cursor.estimateSize());
    
    final int[] index = {0};  // as a pointer to int
    while(cursor.tryAdvance(e -> assertEquals(hexes[index[0]++], e.hex)));
    assertEquals(hexes.length, index[0]);
  }
  

  

  @Test
  public void testCursor11_2Depth1EmptyMidBranch() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    
    final String[] hexes = { "0c10e0", "0f90f0" } ;
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);
    // create first entry at depth 0
    {
      File file = hexPath.suggest(hexes[0]);
      assertTrue(file.createNewFile());
    }
    // create an "0f" subdir
    File subdir0f = new File(dir, "0f");
    assertTrue(subdir0f.mkdir());
    // push next entry at depth 1
    {
      File file = hexPath.suggest(hexes[1], false);
      assertEquals(subdir0f, file.getParentFile());
      assertTrue(file.createNewFile());
    }
    // create empty mid-branch
    File midSubdir = new File(dir, "0d");
    assertTrue(new File(midSubdir , "11").mkdirs());
    assertTrue(new File(midSubdir , "22").mkdirs());
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
//    assertEquals(2, cursor.estimateSize());
    
    final int[] index = {0};  // as a pointer to int
    while(cursor.tryAdvance(e -> assertEquals(hexes[index[0]++], e.hex)));
    assertEquals(hexes.length, index[0]);
  }
  

  

  @Test
  public void testCursor12_2Depth1EmptyBranchInterleaved() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    // set up
    
    final String[] hexes = { "0c10e1", "0f90f1" } ;
    
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);
    // create first entry at depth 0
    {
      File file = hexPath.suggest(hexes[0]);
      assertTrue(file.createNewFile());
    }
    // create an "0f" subdir
    File subdir0f = new File(dir, "0f");
    assertTrue(subdir0f.mkdir());
    // push next entry at depth 1
    {
      File file = hexPath.suggest(hexes[1], false);
      assertEquals(subdir0f, file.getParentFile());
      assertTrue(file.createNewFile());
    }
    // create empty pre-branch
    File subdir00 = new File(dir, "00");
    assertTrue(new File(subdir00 , "11").mkdirs());
    assertTrue(new File(subdir00 , "22").mkdirs());
    // create another empty pre-branch (so splitting works with this test)
    File subdir01 = new File(dir, "01");
    assertTrue(new File(subdir01 , "11").mkdirs());
    assertTrue(new File(subdir01 , "22").mkdirs());
    // create empty mid-branch
    File midSubdir = new File(dir, "0d");
    assertTrue(new File(midSubdir , "11").mkdirs());
    assertTrue(new File(midSubdir , "22").mkdirs());
    midSubdir = new File(dir, "0e");
    assertTrue(new File(midSubdir , "11").mkdirs());
    assertTrue(new File(midSubdir , "22").mkdirs());
    // create empty post-branch
    File postSubdir = new File(dir, "10");
    assertTrue(new File(postSubdir , "11").mkdirs());
    assertTrue(new File(postSubdir , "22").mkdirs());
    
    // test the cursor
    HexPathTree.Cursor cursor = hexPath.newCursor();
    assertTrue(cursor.hasRemaining());
    System.out.println(method(label) + ": Cursor size estimate is " + cursor.estimateSize());
//    assertEquals(2, cursor.estimateSize());
    
    final int[] index = {0};  // as a pointer to int
    while(cursor.tryAdvance(e -> assertEquals(hexes[index[0]++], e.hex)));
    assertEquals(hexes.length, index[0]);
    
    cursor = hexPath.newCursor();
    
//    Spliterator<HexPathTree.Entry> splitCursor = cursor.trySplit();
//    assertNotNull(splitCursor);
    
    index[0] = 0;
    while(cursor.tryAdvance(e -> assertEquals(hexes[index[0]++], e.hex)));
//    assertEquals(1, index[0]);
//    assertTrue(index[0] <= 2);
//    while(splitCursor.tryAdvance(e -> assertEquals(hexes[index[0]++], e.hex)));
    assertEquals(2, index[0]);
  }
  
  
  
  @Test
  public void testCursor13_3Depth1() throws IOException {
    
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);

//    System.out.println("========= " + method(label) + " =========");

    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);

    final String[] hexes = { "058a9a", "0c10ea", "0f90fa" } ;
    
    for (String hex : hexes) {
      assertTrue(new File(dir, hex.substring(0, 2)).mkdir());
      File file = hexPath.suggest(hex, false);
      assertTrue(file.createNewFile());
    }
    
    Spliterator<HexPathTree.Entry> cursor = hexPath.newCursor();
    Spliterator<HexPathTree.Entry> splitCursor = cursor.trySplit();
    
    assertNotNull(splitCursor);
    int[] index = { 0 };
    while(cursor.tryAdvance(e -> assertEquals(hexes[index[0]++], e.hex)));
//    assertEquals(1, index[0]);
    assertTrue(index[0] <= 2);
//    System.out.println("first Spliterator consumed " + index[0] + " entries");
    while(splitCursor.tryAdvance(e -> assertEquals(hexes[index[0]++], e.hex)));
    assertEquals(3, index[0]);
    
  }
  
  
  @Test
  public void testStreaming() throws IOException {

    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    HexPathTree hexPath = new HexPathTree(dir, EXT);
    
    
    // set up some subdirectories in order to coerce a tree structure
    // as we populate hexPath with entries
    {
      String[] subdirs = { "03", "1a", "d2", "dd" };
      String[] subdirs03 = { "52", "b6" };
      String[] subdirs1a = { "26", "f2" };
      String[] subdirsd2 = { "9e", "c4" };
      String[] subdirsdd = { };
      
      String[][] subs = { subdirs03, subdirs1a, subdirsd2, subdirsdd };
      File[] dirs = makeSubdirs(dir, subdirs);
      for (int k = 0; k < dirs.length; ++k)
        makeSubdirs(dirs[k], subs[k]);
    }

    ArrayList<String> hexes = new ArrayList<>();
    // we're going to use a 4 byte scheme for our hexes (8 characters)
    
    String threeBytePrefix = "332211";
    for (String hex : HEXSPACE)
      addHex(hexPath, hexes, threeBytePrefix + hex);
    
    addHex(hexPath, hexes, "03778899");
    
    threeBytePrefix = "0352ea";
    for (int k = 4; k < 34; ++k)
      addHex(hexPath, hexes, threeBytePrefix + HEXSPACE.get(k));
    

    
    threeBytePrefix = "034455";
    for (int k = 79; k < 127; ++k)
      addHex(hexPath, hexes, threeBytePrefix + HEXSPACE.get(k));

    threeBytePrefix = "d2c455";
    for (int k = 0; k < 200; ++k)
      addHex(hexPath, hexes, threeBytePrefix + HEXSPACE.get(k));
    
    threeBytePrefix = "d2c466";
    for (int k = 0; k < 256; ++k)
      addHex(hexPath, hexes, threeBytePrefix + HEXSPACE.get(k));
    
    Collections.sort(hexes);
    
    System.out.println(method(label) + ": " + hexes.size() + " entries added");
    
    final int[] count = { 0 };
    
    hexPath.stream().forEachOrdered(e -> assertEquals(hexes.get(count[0]++), e.hex));
    assertEquals(hexes.size(), count[0]);
  }
  
  
  private void addHex(HexPathTree hexPath, List<String> hexes, String hex) throws IOException {
    hexes.add(hex);
    assertTrue(hexPath.suggest(hex).createNewFile());
  }
  
  
  private File[] makeSubdirs(File dir, String[] subdirs) {
    File[] dirs = new File[subdirs.length];
    for (int i = 0; i < subdirs.length; ++i) {
      dirs[i] = new File(dir, subdirs[i]);
      assertTrue(dirs[i].mkdir());
    }
    return dirs;
  }
  
  
  
  
  
  
  
//  @Test
  public void testCursor_514Depth2() throws IOException {
    Object label = new Object() { };
    final File dir = getMethodOutputFilepath(label);
    
    String anomalous = "00100";
    HexPathTree hexPath = new HexPathTree(dir, EXT, 256);
    
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

}
