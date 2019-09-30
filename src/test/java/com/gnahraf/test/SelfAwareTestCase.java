/**
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.test;

import java.lang.reflect.Method;



/**
 * Provides a programmatic way (a hack) to return a method's name from code inside it.
 * Use this also to determine test output directories.
 * <p/>
 * (No, it's not sentient.)
 */
public class SelfAwareTestCase {
  

  /**
   * Returns the method name via a hack. Example usage:
   * <pre>
   *   <tt>System.out.println("I'm in " + method(new Object() { }));</tt>
   * </pre>
   * 
   * @param innerMethodObject  an instance of a class defined in the method you're
   *                           calling from, simplest of which is just <tt>new Object(){ }</tt>
   */
  public String method(Object innerMethodObject) {
    Class<?> clazz = innerMethodObject.getClass();
    Method testMethod = clazz.getEnclosingMethod();
    if (testMethod == null)
      throw new IllegalArgumentException(
          "argument type not defined inside test case method: " + innerMethodObject);
    
    // the following check proved unnecessarily restrictive..
    
//    if (!testMethod.getDeclaringClass().equals(getClass()))
//      throw new IllegalArgumentException(
//          "argument type not defined in test case class: " + innerMethodObject);
    
    return testMethod.getName();
  }

}