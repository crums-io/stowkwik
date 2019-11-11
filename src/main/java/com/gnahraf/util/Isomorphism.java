/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

/**
 * A one-to-one mapping and its inverse. An instance of course is only a
 * <em>declaration</em>; it's not a guarantee the relationship is true.
 */
public class Isomorphism<U, V> {
  
  
  public final static Isomorphism<File, Path> FILE_TO_PATH =
      declare(f -> f.toPath(), p -> p.toFile(), File.class, Path.class);
  
  
  public final static Isomorphism<Path, File> PATH_TO_FILE = FILE_TO_PATH.reverse();
  
  
  
  public static <U, V> Isomorphism<U, V> declare(
      Function<U, V> mapping, Function<V, U> inverse, Class<U> sourceType, Class<V> targetType) {
    
    return new Isomorphism<>(mapping, inverse, sourceType, targetType);
  }
  
  
  private final Function<U, V> mapping;
  private final Function<V, U> inverse;
  
  private final Class<U> sourceType;
  private final Class<V> targetType;

  /**
   * 
   */
  public Isomorphism(Function<U, V> mapping, Function<V, U> inverse, Class<U> sourceType, Class<V> targetType) {
    this.mapping = Objects.requireNonNull(mapping, "mapping");
    this.inverse = Objects.requireNonNull(inverse, "inverse");
    this.sourceType = Objects.requireNonNull(sourceType, "sourceType");
    this.targetType = Objects.requireNonNull(targetType, "targetType");
  }
  
  
  public Function<U, V> mapping() {
    return mapping;
  }
  
  
  public Function<V, U> inverse() {
    return inverse;
  }
  
  
  public Class<U> sourceType() {
    return sourceType;
  }
  
  public Class<V> targetType() {
    return targetType;
  }
  
  
  public boolean isTargetType(Object o) {
    return o != null && targetType.isInstance(o);
  }
  
  
  public boolean isSourceType(Object o) {
    return o != null && sourceType.isInstance(o);
  }
  
  
  
  public Isomorphism<V, U> reverse() {
    return new Isomorphism<>(inverse, mapping, targetType, sourceType);
  }
  
  
  
  /**
   * Overriden so that the reverse of an instance's reverse is equal to itself.
   */
  @Override
  public boolean equals(Object o) {
    return o instanceof Isomorphism<?, ?> && equals((Isomorphism<?, ?>) o);
  }
  
  public boolean equals(Isomorphism<?, ?> o) {
    return
        o != null &&
        sourceType.equals(o.sourceType) &&
        targetType.equals(o.targetType) &&
        mapping.equals(o.mapping) &&
        inverse.equals(o.inverse);
  }
  

  @Override
  public int hashCode() {
    return sourceType.hashCode() ^ targetType.hashCode() ^ mapping.hashCode() ^ inverse.hashCode();
  }

}
