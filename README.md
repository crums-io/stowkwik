# stowkwik

Quick and dirty file per object store in Java.

## Motivation

Lotta times you have a lotta machine generated things, whether they be downloaded files, intermediate
results of computations, whatever.. that you'd like to just store on the file system. The goal here is
to streamline the setup (e.g. just specify a root directory) and object marshalling scheme.

Objects and their associated files are identified by their crypotographic hashes. This obviates the need
for naming things when you're storing a lot of things of the same type.

## How to Use

This [API](https://github.com/gnahraf/stowkwik/tree/master/src/main/java/com/gnahraf/stowkwik) calls an object store an `ObjectManager`. Right now there are 2 types of these:

* `BinaryObjectManager`, and
* `XmlObjectManager`

Like the names suggest, the first is machine readable, the second is human readable as well.
BinaryObjectManager uses a type-specific `Codec`; XmlObjectManager still needs a type-specific `Encoder`
in order unamibigously compute hash state.

The [unit tests](https://github.com/gnahraf/stowkwik/tree/master/src/test/java/com/gnahraf/stowkwik) contain a mock example. See

* Mock - the mock object
* MockEncoder - encoder used to write mock objects to memory (also used to compute an object's hash)
* MockCodec - read/write codec used by the BinaryObjectManager

## Limits

~~A few thousand objects per store. Basically, the max number of files per directory in the file system. (Working on fixing this as I write.)~~
Done.

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
