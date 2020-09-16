/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik;


/**
 * Mock object for item serialization.
 */
class Mock {
  
  // don't mess with these default values; assumed non zero in tests
  public double a = -1.5;
  public double b = 2.1e5;
  public int c = 5;
  
  public boolean equals(Object o) {
    if (o == this)
      return true;
    
    else if (o instanceof Mock) {
      Mock m = (Mock) o;
      return a == m.a && b == m.b && c == m.c;
    } else
      return false;
  }
  
  // sorry for being pedantic; this code is never hit :/
  public int hashCode() {
    return c ^ Double.hashCode(a) ^ Double.hashCode(b);
  }
}