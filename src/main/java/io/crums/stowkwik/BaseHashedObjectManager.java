/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.stowkwik;


import static io.crums.util.IntegralStrings.toHex;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.crums.stowkwik.io.Channels;
import io.crums.stowkwik.io.CorruptionException;
import io.crums.stowkwik.io.HexPathTree;
import io.crums.util.IntegralStrings;

/**
 * Base implementation for a file-per-object storage manager. The ID of each object is
 * determined by a cryptographic hash (MD5 usually suffices) of its contents.
 *
 * @param <T> the type of object managed.
 */
public abstract class BaseHashedObjectManager<T> extends ObjectManager<T> {
  
  /**
   * Default hashing algorithm used by subclasses.
   */
  public final static String DEFAULT_HASH_ALGO = "MD5";
  
  protected final HexPathTree hexPath;
  protected final String hashAlgo;

  /**
   * 
   * @param dir the root directory. If doesn't exist it's created.
   * @param ext the file name extension (multiple stores with different
   *            extensions on the same are possible on the same root
   *            directory
   * @param hashAlgo
   *            the name of the cryptographic hashing algorithm
   *            (suitable for {@linkplain MessageDigest#getInstance(String)})
   *            E.g. {@code MD5}, {@code SHA-1}, {@code SHA-256}, ..
   * 
   * @see BaseHashedObjectManager#DEFAULT_HASH_ALGO
   */
  protected BaseHashedObjectManager(File dir, String ext, String hashAlgo) {
    this.hexPath = new HexPathTree(dir, ext);
    this.hashAlgo = hashAlgo;
    if (hashAlgo == null)
      throw new IllegalArgumentException("null hashAlgo");
    sanityCheckAlgo();
    hexPath.primeRoot();
  }
  
  
  protected void sanityCheckAlgo() {
    
    String sampleId;
    {
      List<String> sampleIds = streamIds().limit(1).collect(Collectors.toList());
      if (sampleIds.isEmpty()) {
        newDigest(); // make sure the digest algo is supported (method was not exercised)
        return;
      }
      sampleId = sampleIds.get(0);
    }
    

    T object = read(sampleId);
    if (!sampleId.equals(getId(object)))
      throw new IllegalArgumentException(
          "hash algo " + hashAlgo + " appears not to match that used for existing data");
  }
  
  
  public File getRootDir() {
    return hexPath.getRoot();
  }
  

  public String getFileExtension() {
    return hexPath.getFileExtension();
  }
  

  
  
  @Override
  public String write(T object) throws UncheckedIOException {
    ByteBuffer buffer = toByteBuffer(object);
    
    String hash = signature(buffer);
    File file = hexPath.suggest(hash, true);
    
    if (file.exists())
      validateFile(file, object, buffer);
    else
      writeObjectFile(file, object, buffer);
    
    return hash;
  }
  
  
  
  @Override
  public String getId(T object) {
    ByteBuffer buffer = toByteBuffer(object);
    
    return signature(buffer);
  }



  @Override
  public boolean containsId(String id) {
    return hexPath.find(id) != null;
  }



  @Override
  public T read(String hash) throws UncheckedIOException {
    File file = hexPath.find(hash);
    if (file == null)
      throw new NotFoundException(hash);
    
    return readObjectFile(file);
  }
  
  
  
  @Override
  public Stream<String> streamIds() {
    return hexPath.stream().map(e -> e.hex);
  }
  
  
  
  @Override
  public Stream<String> streamIds(String idPrefix) {
    return hexPath.streamStartingFrom(idPrefix).map(e -> e.hex);
  }
  
  
  @Override
  public Stream<T> streamObjects() {
    return hexPath.stream().map(e -> readObjectFile(e.file));
  }
  
  
  @Override
  public Stream<T> streamObjects(String idPrefix) {
    return hexPath.streamStartingFrom(idPrefix).map(e -> readObjectFile(e.file));
  }
  
  
  public T readUsingPrefix(String idPrefix) throws NotFoundException, IllegalArgumentException, UncheckedIOException {
    if (idPrefix == null || idPrefix.isEmpty())
      throw new IllegalArgumentException("empty idPrefix " + idPrefix);
    
    idPrefix = IntegralStrings.canonicalizeHex(idPrefix);
    
    // create a distinct cursor
    HexPathTree.Cursor cursor = hexPath.newCursor(true);
    
    if (!cursor.advanceToPrefix(idPrefix))
      throw new NotFoundException(idPrefix + "..");
    
    HexPathTree.Entry head = cursor.getHeadEntry();
    
    if (!head.hex.startsWith(idPrefix))
      throw new NotFoundException(idPrefix + "..");
    
    if (cursor.consumeNext() && cursor.getHeadHex().startsWith(idPrefix))
      throw new IllegalArgumentException("ambiguous (more than 1 result) for prefix " + idPrefix );
    
    return readObjectFile(head.file);
  }
  
  
  
  protected final File getFilepath(String hash) {
    return hexPath.find(hash);
  }
  
  
  protected final ByteBuffer loadByteBuffer(File file) throws UncheckedIOException {
    if (file.length() > maxBytes())
      throw new CorruptionException(
          "file length " + file.length() + " > maxBytes (" + maxBytes() + "): " + file);
    ByteBuffer buffer = allocateBuffer((int) file.length());
    Channels.readFully(file, buffer);
    return buffer.flip();
  }
  
  
  protected final void validateFileAgainstBuffer(File file, ByteBuffer buffer) throws CorruptionException {
    boolean fail = buffer.remaining() != file.length();
    if (fail)
      throw new CorruptionException(file.toString());
    
    ByteBuffer contents = loadByteBuffer(file);
    
    fail = !contents.equals(buffer);
    if (fail)
      throw new CorruptionException(file.toString());
  }
  
  
  protected abstract int maxBytes();
  
  

  /**
   * @param file      an existing file
   */
  protected abstract T readObjectFile(File file) throws UncheckedIOException;
  
  
  protected abstract ByteBuffer toByteBuffer(T object);
  
  /**
   * Validates the contents of the given existing {@code file} against the {@code object}
   * it's supposed to represent. The given {@code buffer} is an alternate representation
   * of the same object. Depending on implementations details (e.g. is {@code object}/{@code buffer}
   * relationship 1:1) one or the other may be used.
   */
  protected abstract void validateFile(File file, T object, ByteBuffer buffer) throws CorruptionException;
  
  /**
   * @param file      (does not yet exist)
   * @param object    the thing to be written
   * @param buffer    the binary contents used to generate the object's signature
   *                  (may be suitable for writing to file?)
   * @throws UncheckedIOException
   */
  protected abstract void writeObjectFile(File file, T object, ByteBuffer buffer) throws UncheckedIOException;



  protected ByteBuffer allocateBuffer(int bytes) {
    return ByteBuffer.allocate(bytes);
  }
  
  /**
   * Computes and returns the signature of the given {@code buffer}.
   * Excepting its mark, the state of the {@code buffer} is not modified.
   */
  protected String signature(ByteBuffer buffer) {
    MessageDigest digest = threadLocalDigest();
    buffer.mark();
    digest.update(buffer);
    buffer.reset();
    return toHex(digest.digest());
  }
  
  /**
   * Testing shows about a 10% improvement in speed when reusing digests (SSD
   */
  protected MessageDigest threadLocalDigest() {
    Map<String, MessageDigest> map = digestMap.get();
    MessageDigest digest = map.get(hashAlgo);
    if (digest == null) {
      digest = newDigest();
      map.put(hashAlgo, digest);
    } else
      digest.reset();
    return digest;
  }
  
  protected final static ThreadLocal<Map<String, MessageDigest>> digestMap = new ThreadLocal<>() {
    @Override
    protected Map<String, MessageDigest> initialValue() {
      return new HashMap<>(2);
    }
  };
  
  
  protected MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance(hashAlgo);
    } catch (NoSuchAlgorithmException nsax) {
      IllegalArgumentException iax = new IllegalArgumentException("hash algo: " + hashAlgo);
      iax.initCause(nsax);
      throw iax;
    }
  }

}
