/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.io;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.gnahraf.test.IoTestCase;

/**
 * 
 */
public class DirectoryWatcherTest extends IoTestCase {
  
  final static int PATIENCE_MILLIS = 500;
  final static int MAX_PATIENCE = 30000;

  
  @Test
  public void test00None() throws IOException {
    DirectoryWatcher.Listener blowupListener = new DirectoryWatcher.Listener() {
      @Override
      public void fileAdded(File file) {
        fail("wa? " + file);
      }
    };
    
    
    DirectoryWatcher watcher = new DirectoryWatcher(blowupListener);
//    sleep(PATIENCE_MILLIS);
    watcher.close();
  }
  
  

  @Test
  public void test01NoActivity() throws IOException {
    DirectoryWatcher.Listener blowupListener = new DirectoryWatcher.Listener() {
      @Override
      public void fileAdded(File file) {
        fail("wa? " + file);
      }
    };
    
    
    DirectoryWatcher watcher = new DirectoryWatcher(blowupListener);
    File dir = getMethodOutputFilepath(blowupListener);
    assertTrue(dir.mkdir());
    watcher.addDirectory(dir);
//    sleep(PATIENCE_MILLIS);
    watcher.close();
  }
  
  

  @Test
  public void test02CreateOneInPlace() throws IOException {
    List<File> files = new ArrayList<>();
    DirectoryWatcher.Listener listAdapter = new DirectoryWatcher.Listener() {
      @Override
      public void fileAdded(File file) {
        synchronized (files) {
          files.add(file);
          files.notify();
        }
      }
    };
    
    
    try (DirectoryWatcher watcher = new DirectoryWatcher(listAdapter)) {
      File dir = getMethodOutputFilepath(listAdapter);
      assertTrue(dir.mkdir());
      watcher.addDirectory(dir);
      File moo = new File(dir, "moo");
      moo.createNewFile();
      synchronized (files) {
        long startTime = System.currentTimeMillis();
        while (files.isEmpty() && System.currentTimeMillis() - startTime < MAX_PATIENCE) {
          try {
            files.wait(MAX_PATIENCE / 2);
          } catch (InterruptedException signal) {
            continue;
          }
        }
      }
      assertEquals(1, files.size());
      assertEquals(moo, files.get(0));
    }
  }
  
  

  @Test
  public void test03MoveOne() throws IOException {
    
    List<File> files = new ArrayList<>();
    
    DirectoryWatcher.Listener listAdapter = new DirectoryWatcher.Listener() {
      @Override
      public void fileAdded(File file) {
        synchronized (files) {
          files.add(file);
          files.notify();
        }
      }
    };
    
    
    try (DirectoryWatcher watcher = new DirectoryWatcher(listAdapter)) {
      File dir = getMethodOutputFilepath(listAdapter);
      assertTrue(dir.mkdir());
      File watchDir = new File(dir, "watch");
      assertTrue(watchDir.mkdir());
      watcher.addDirectory(watchDir);
      File moo = new File(dir, "moo");
      moo.createNewFile();
      File wamoo = new File(watchDir, moo.getName());
      moo.renameTo(wamoo);
      synchronized (files) {
        long startTime = System.currentTimeMillis();
        while (files.isEmpty() && System.currentTimeMillis() - startTime < MAX_PATIENCE) {
          try {
            files.wait(MAX_PATIENCE);
          } catch (InterruptedException signal) {
            continue;
          }
        }
      }
      assertEquals(1, files.size());
      assertEquals(wamoo, files.get(0));
    }
  }
  
  

  @Test
  public void test04Watch2DirectoriesMove2() throws IOException {
    
    Set<File> files = new HashSet<>();
    
    DirectoryWatcher.Listener listAdapter = new DirectoryWatcher.Listener() {
      @Override
      public void fileAdded(File file) {
        synchronized (files) {
          files.add(file);
          files.notify();
        }
      }
    };
    
    
    try (DirectoryWatcher watcher = new DirectoryWatcher(listAdapter)) {
      File dir = getMethodOutputFilepath(listAdapter);
      
      assertTrue(dir.mkdir());
      File watchDir = new File(dir, "watch");
      assertTrue(watchDir.mkdir());
      
      File watchDir2 = new File(dir, "watch2");
      assertTrue(watchDir2.mkdir());
  
      watcher.addDirectory(watchDir);
      watcher.addDirectory(watchDir2);
      
      Set<File> expected = new HashSet<>();
      {
        File temp = new File(dir, "moo");
        temp.createNewFile();
        File target = new File(watchDir, temp.getName());
        temp.renameTo(target);
        expected.add(target);
      }
  
      {
        File temp = new File(dir, "woof");
        temp.createNewFile();
        File target = new File(watchDir2, temp.getName());
        temp.renameTo(target);
        expected.add(target);
      }
      
      
      
      synchronized (files) {
        long startTime = System.currentTimeMillis();
        while (files.size() < expected.size() && System.currentTimeMillis() - startTime < MAX_PATIENCE) {
          try {
            files.wait(MAX_PATIENCE);
          } catch (InterruptedException signal) {
            continue;
          }
        }
      }
      assertEquals(expected, files);
    }
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ix) {    }
  }

}
