/*
 * Copyright 2017 Babak Farhang
 */
package io.crums.stowkwik;


import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Base abstraction for a simple object store. It doesn't yet have a {@code remove()} method.
 * 
 * @param <T> the type of data this instance manages. At minimum, this
 *            requires that {@linkplain Object#equals(Object) Object.equals}
 *            be properly overriden with a value-based implementation
 *            (the base implementation is pointer-based). You might want to also override
 *            {@linkplain Object#hashCode() Object.hashCode} (consistent with {@code equals()})
 *            but it's not required here for the proper functioning of an implementation.
 */
public abstract class ObjectManager<T> {


  /**
   * Writes the given {@code object} and returns its ID. This is an idempotent
   * operation: if there's already another object in the store that equals the
   * given {@code object}, then the existing object's ID is returned.
   * 
   * @throws UncheckedIOException in the event of an I/O error
   */
  public abstract String write(T object) throws UncheckedIOException;
  
  
  
  /**
   * Computes and returns the ID of the given {@code object}. The object
   * does not have to exist in the store. You can use this to determine what
   * the return value of {@linkplain #write(Object)} will be.
   */
  public abstract String getId(T object);
  
  
  public abstract boolean containsId(String id);
  
  
  /**
   * Reads and returns a previously written object.
   * 
   * @param id  the object's ID as returned on write
   * 
   * @throws NotFoundException  if no known (stored) object with the given {@code id} exists
   * @throws UncheckedIOException in the event of an I/O error
   */
  public abstract T read(String id) throws NotFoundException, UncheckedIOException;
  
  
  public boolean hasReader() {
    return false;
  }
  
  
  public Reader getReader(String id) throws NotFoundException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  
  /**
   * Returns a stream of object IDs in lexicographc order.
   */
  public abstract Stream<String> streamIds();
  
  
  /**
   * Returns a stream of object IDs in lexicographc order starting with or greater than
   * {@code idPrefix}.
   */
  public abstract Stream<String> streamIds(String idPrefix);
  
  
  /**
   * Returns the object whose ID starts with the given prefix.
   * <p>
   * Note: the base class originally provided an implementation but it was inefficient
   * (traversed the whole store) and lest I forget again to override it, it's marked abstract.
   * The expected behavior is as follows..
   * </p>
   * <pre>{@code
      public T readUsingPrefix(String idPrefix) throws NotFoundException, IllegalArgumentException, UncheckedIOException {
        if (idPrefix == null || idPrefix.isEmpty())
          throw new IllegalArgumentException("empty idPrefix " + idPrefix);
        
        List<String> id =
            streamIds().filter(hash -> hash.startsWith(idPrefix)).collect(Collectors.toList());
        
        if (id.isEmpty())
          throw new NotFoundException(idPrefix + "..");
        
        if (id.size() != 1)
          throw new IllegalArgumentException("ambiguous prefix " + idPrefix + " (" + id.size() + " matches)");
        
        return read(id.get(0));
      }
   * }</pre>
   * 
   * @param idPrefix prefix of the object's ID.
   * 
   * @return the marshalled object, never {@code null}
   * 
   * @throws NotFoundException
   *         if no object with ID starting with that prefix could be found
   * @throws IllegalArgumentException
   *         if more than one object with ID starting with that prefix is found
   */
  public abstract T readUsingPrefix(String idPrefix)
      throws NotFoundException, IllegalArgumentException, UncheckedIOException;
  

  
  /**
   * Streams objects in the store in order of their IDs. Logically equivalent to
   * this 2-pass implementation.
   * <pre>{@code 
      public Stream<T> streamObjects() {
        return streamIds().map(hash -> read(hash));
      }
   * } </pre>
   */
  public abstract Stream<T> streamObjects();
  
  
  /**
   * Returns objects in the store in order of their IDs starting with or greater than
   * {@code idPrefix}. Logically equivalent to this 2-pass implementation.
   * <pre>{@code
      public Stream<T> streamObjects(String idPrefix) {
        return streamIds(idPrefix).map(hash -> read(hash));
      }
   * }</pre>
   */
  public abstract Stream<T> streamObjects(String idPrefix);
  
  
  
  
  
  
  
  
  
  // ---S-T-A-T-I-C---
  
  
  
  
  /**
   * Maps an instance of type {@code U} to an instance of type {@code V}.
   * This is a workaround for working with a mutable type ({@code U}) at the persistence
   * layer (typically because its easier) when we'd prefer to be working with an
   * immutable exposed type ({@code V}).
   * 
   * @param manager     the base manager
   * @param readMapper  the {@code U -> V} converter
   * @param writeMapper the {@code V -> U} converter
   * 
   * @param <U>         the type under the hood (at the persistence layer)
   * @param <V>         the exposed type (for sanity, do override {@code Object.equals}
   *                    for this type, also)
   */
  public static <U, V> ObjectManager<V> map(
      final ObjectManager<U> manager,
      final Function<U, V> readMapper, final Function<V, U> writeMapper) {
    
    if (manager == null)
      throw new IllegalArgumentException("null manager");
    if (readMapper == null)
      throw new IllegalArgumentException("null readMapper");
    if (writeMapper == null)
      throw new IllegalArgumentException("null writeMapper");
    
    return
        new ObjectManager<V>() {

          @Override
          public String write(V object) {
            U u = writeMapper.apply(object);
            return manager.write(u);
          }
          
          
          @Override
          public String getId(V object) {
            U u = writeMapper.apply(object);
            return manager.getId(u);
          }

          @Override
          public V read(String id) {
            U u = manager.read(id);
            return readMapper.apply(u);
          }
          
          @Override
          public V readUsingPrefix(String idPrefix) {
            U u = manager.readUsingPrefix(idPrefix);
            return readMapper.apply(u);
          }
          
          @Override
          public Stream<String> streamIds() {
            return manager.streamIds();
          }
          
          @Override
          public Stream<String> streamIds(String idPrefix) {
            return manager.streamIds(idPrefix);
          }


          @Override
          public boolean hasReader() {
            return manager.hasReader();
          }


          @Override
          public Reader getReader(String id) throws NotFoundException, UnsupportedOperationException {
            return manager.getReader(id);
          }
          
          
          @Override
          public boolean containsId(String id) {
            return manager.containsId(id);
          }
          

          @Override
          public Stream<V> streamObjects() {
            return manager.streamObjects().map(u -> readMapper.apply(u));
          }
          

          @Override
          public Stream<V> streamObjects(String idPrefix) {
            return manager.streamObjects(idPrefix).map(u -> readMapper.apply(u));
          }
        };
  }
  
}