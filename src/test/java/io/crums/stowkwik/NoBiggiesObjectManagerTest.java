/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik;

/**
 * Short circuits big boiler plate tests.
 */
public abstract class NoBiggiesObjectManagerTest extends ObjectManagerTest {

  /**
   * @param ext
   */
  public NoBiggiesObjectManagerTest(String ext) {
    super(ext);
  }




  /**
   * No op.
   */
  @Override
  public void testStreaming02_64k() {
    System.out.println(method(new Object() { }) + " is always skipped for " + getClass().getSimpleName());
  }

}
