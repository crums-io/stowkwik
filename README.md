# stowkwik

Quick and dirty file per object store in Java.

## Motivation

Lotta times you have a lotta machine generated things, whether they be downloaded files, intermediate
results of computations, whatever.. that you'd like to just store on the file system. The goal here is
to streamline the setup (e.g. just specify a root directory) and object marshalling scheme.

Objects and their associated files are identified by their crypotographic hashes. This obviates the need
for naming things when you're storing a lot of things of the same type.

## How to Use

This [API](https://github.com/gnahraf/stowkwik/tree/master/src/main/java/com/gnahraf/stowkwik) calls an object store an `ObjectManager`. Right now there are 4 types of these:

* `BinaryObjectManager`
* `XmlObjectManager`
* `BytesManager`
* `FileManager`

Like the names suggest, the first is machine readable, the second is human readable. Computing hash state from
machine readable byte sequences is usually straight forward since in most applications if two objects' byte
sequences are different then their states are different; when state is human readable however, there are usually
multiple valid representations of the same state (white space, comments, item order, etc.), so there's a need to cannonicalize
object state. This is one role of an `Encoder`: it writes an object's state unambiguously to a byte sequence and
is used to compute the object's hash. A `Codec` is an `Encoder` that can read back what it writes.

`BinaryObjectManager` uses a type-specific `Codec`; `XmlObjectManager` still needs a type-specific `Encoder`
in order to unamibigously compute hash state (we don't want an object's hash to change if we switch XML libraries, for instance.
Dev note: should be able to automate). If on the other hand, you want to marshal objects elsewhere and just want
to throw blobs of bytes into a store, you can use a `BytesManager` which doesn't require any codec. `FileManager` is like
`BytesManager` but is more efficient in many respects and is better at managing larger files (blobs).


The [unit tests](https://github.com/gnahraf/stowkwik/tree/master/src/test/java/com/gnahraf/stowkwik) contain a mock example. See

* Mock - the mock object
* MockEncoder - encoder used to write mock objects to memory (also used to compute an object's hash)
* MockCodec - read/write codec used by the BinaryObjectManager

## Limits

Practically the limits of your storage medium. This uses a pretty scalable, deeper as you grow, directory structure.

## Roadmap

Started developing this inside another project; making it stand alone. I aim to make it scalable in an
easy-to-understand way. (I had to add a lot of comments as I revisited this in order to remember what
I had already done.)

Oct. 6 2019: Pushing to make `HexPath` a drop-in replacement for `FilepathGenerator` so that it'll scale to whatever the file system can handle.

Oct. 12 2019: Working on a `java.util.Splitarator` that supports streaming files mananged under a `HexPath` instance. This is complicated by the fact there are multiple possible directory paths for a given hash value since `HexPath` grows the directory structure as more objects are dropped in.

Oct. 20 2019: Streaming support added in `HexPathTree`, a subclass of `HexPath` and dropped in as a replacement for the flat directory used by the `ObjectManager`s. Tested with 64k files. Next steps:

* `BytesManager`: a straight file-contents based object manager (no marshalling)
* Command line tool

Nov. 3 2019: Created `storex` a command line tool for exploring an existing store.

Nov. 10 2019: Created `stowd` a command line background process that stows files away by monitoring one or more directories for new files and moving them to the store. Next steps:

* Add configurable output/logging to `stowd`
* Command line tool (`stow` ?) for stowing files that can be piped in the shell for input filepaths and output mappings
