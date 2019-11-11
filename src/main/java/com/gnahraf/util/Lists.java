/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 
 */
public class Lists {

  private Lists() { }
  
  
  public static <U, V> List<V> map(List<U> source, Function<U, V> mapper) {
    return new ListView<U, V>(source, mapper);
  }
  
  
  protected static class ListView<U, V> extends AbstractList<V> {
    
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
  
  
  public static <T> List<T> asReadOnlyList(T[] array) {
    Objects.requireNonNull(array);
    
    return new AbstractList<T>() {

      @Override
      public T get(int index) {
        return array[index];
      }

      @Override
      public int size() {
        return array.length;
      }
    };
  }

}
