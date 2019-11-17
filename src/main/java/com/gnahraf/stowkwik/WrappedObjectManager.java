/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.stowkwik;

import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.stream.Stream;

import com.gnahraf.xcept.NotFoundException;

/**
 * Wrapper for aspect-oriented overrides.
 */
public class WrappedObjectManager<T> extends ObjectManager<T> {
  
  protected final ObjectManager<T> base;


  /**
   * 
   */
  protected WrappedObjectManager(ObjectManager<T> base) {
    this.base = Objects.requireNonNull(base, "base");
  }

  @Override
  public String write(T object) throws UncheckedIOException {
    return base.write(object);
  }


  @Override
  public String getId(T object) {
    return base.getId(object);
  }


  @Override
  public boolean containsId(String id) {
    return base.containsId(id);
  }


  @Override
  public T read(String id) throws NotFoundException, UncheckedIOException {
    return base.read(id);
  }


  @Override
  public boolean hasReader() {
    return base.hasReader();
  }


  @Override
  public Reader getReader(String id) throws NotFoundException, UnsupportedOperationException {
    return base.getReader(id);
  }

  
  @Override
  public Stream<String> streamIds() {
    return base.streamIds();
  }

  @Override
  public Stream<String> streamIds(String idPrefix) {
    return base.streamIds(idPrefix);
  }


  
  @Override
  public T readUsingPrefix(String idPrefix) throws NotFoundException, IllegalArgumentException, UncheckedIOException {
    return base.readUsingPrefix(idPrefix);
  }


  @Override
  public Stream<T> streamObjects() {
    return base.streamObjects();
  }


  @Override
  public Stream<T> streamObjects(String idPrefix) {
    return base.streamObjects(idPrefix);
  }

}
