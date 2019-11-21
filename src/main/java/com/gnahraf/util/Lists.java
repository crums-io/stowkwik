/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Function;

/**
 * Utility methods for lists. These are assume lists are <em>always</em> {@linkplain RandomAccess random access}.
 * To my mind, linked lists (despite have "list" in their name) don't belong to the interface.
 */
public class Lists {

  private Lists() { }
  
  
  public static <U, V> List<V> map(List<U> source, Function<U, V> mapper) {
    return new ListView<U, V>(source, mapper);
  }
  
  
  
  
  public static <T> List<T> asReadOnlyList(T[] array) {
    return new ArrayView<>(array);
  }
  
  
  protected static class ArrayView<T> extends AbstractList<T> implements RandomAccess {
    
    private final T[] array;
    
    protected ArrayView(T[] array) {
      this.array = Objects.requireNonNull(array);
    }

    @Override
    public T get(int index) {
      return array[index];
    }

    @Override
    public int size() {
      return array.length;
    }
  }
  

  
  
  protected static class ListView<U, V> extends AbstractList<V> implements RandomAccess {
    
    private final List<U> source;
    private final Function<U, V> mapper;
    
    protected ListView(List<U> source, Function<U, V> mapper) {
      this.source = Objects.requireNonNull(source, "source");
      this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public V get(int index) {
      return mapper.apply(source.get(index));
    }

    @Override
    public int size() {
      return source.size();
    }
    
  }
  

}
