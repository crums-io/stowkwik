# stowkwik

Quick and dirty file per object store in Java. It's not terribly fast, but it does scale.

## Motivation

On occasion I needed to store a lot of intermediate results but couldn't be
bothered to setup a database schema and instead dumped state in files. If I first numbered
their filenames, I quickly preferred deriving their filenames from a cryptographic hash instead
of a straight numbering scheme for the following reasons..

* **Fewer race conditions.** A number based file naming scheme is hard to coordinate across multiple threads/processes. With a
hash based naming scheme, the only contention on the write-path is at the same hash--i.e. an attempt by 2 writers to
write the same object at the same time (which under most scenarios should be highly unlikely, if not impossible).
* **Easier to merge.** Merging 2 independent sets of numbered file names would usually necessitate some renumbering. With a
hash based naming scheme, file names never collide (neglibibly unlikely to); if they do, they are (almost certainly) duplicates.
* **URI.** A (sufficiently strong) hash based ID can be thought of as a URI. Such IDs are immutable and independent of the
container they live in. This is doubly important for objects that reference other objects thru their IDs.
* **Set behavior.** No dups and fast membership test.

So with these obvious advantages to a hash based file naming scheme, I'd drop these files in a same directory.
This works fine until hitting files in the tens of thousands. After that things slow down noticeably, particularly
if your view of the directory needs to remain somewhat up-to-date. Maybe this limit is not a bad thing: it forces a
programmer to find the data a more permanent home. Still, I had always planned a fix to that limit, and knowing I could
I thought I might roll a library around it.

## File Naming Scheme

Instead of a flat directory structure, we use a nested one. It's a *nest-more-as-you-grow* scheme.
Besides a root directory, each store is specified using a hashing algo (default is MD5) and aribitrary file extension.
Suppose the MD5 signature of an object in the store is `078940eef616da36acd0d8c80c99cf8f` and the store's extension is `.ext`. Then the file path to this object under the store's root is one of the following

`/078940eef616da36acd0d8c80c99cf8f.ext`  
`/07/8940eef616da36acd0d8c80c99cf8f.ext`  
`/07/89/40eef616da36acd0d8c80c99cf8f.ext`  
`/07/89/40/eef616da36acd0d8c80c99cf8f.ext`  
` .`  
` .`  
`/07/89/40/ee/f6/16/da/36/ac/d0/d8/c8/0c/99/cf/8f.ext`  

On insertion, the deepest possible existing path is preferred; if the deepest existing directory has more files (with the given
extension) than a preset threshold (default 256, the minimum), then a new subdirectory is created and the file is stored in
that subdirectory. (See [`HexPath`](https://github.com/crums-io/stowkwik/blob/master/src/main/java/io/crums/stowkwik/io/HexPath.java)
for implementation details.)

You can overlay multiple stores on top of the same directory structure (as long as their extensions differ). This is not
recommended unless the cardinality of one extension is of the same order of magnitude as that of the other. The reason why
this is mentioned is that there are a good number of cases when indeed 2 extensions have very nearly the same number of objects.
In that case, overlaying saves directory structure overhead.

## Build

Builds are managed by maven.

* `$ mvn clean install` - builds, tests, and install's the library in the local .m2 repo.
* `$ mvn clean package appassembler:assemble` - builds 2 somewhat crude CLI tools. (See next.)


### Suggested "Installation"

Though this is a *programming library* the last step above creates a couple of command line tools (still in the rough),
`stowex` and `stowd`. To use these, you'll need to adjust your `PATH` environment variable. My usual practice with
apps built by maven is to include `$HOME/bin` on my `PATH` and then

> `$ cp -rf target/appassembler/* ~`

which will copy the `bin` and `repo` directories to your home directory.

### Javadoc

My main use for annotating code with javadoc comments is for the context bubbles my IDE pops on hovering the mouse
over code. I comment more than most tho, so generating this might help. Incant the following in the project directory:

> `$ mvn javadoc:javadoc`


## How to Use

This [API](https://github.com/crums-io/stowkwik/tree/master/src/main/java/io/crums/stowkwik)
calls an object store an `ObjectManager`. Right now there are 3 types of these:

* `FileManager`
* `BytesManager`
* `BinaryObjectManager`

All these are views on a managed [`HexPath`](https://github.com/crums-io/stowkwik/blob/master/src/main/java/io/crums/stowkwik/io/HexPath.java)
directory structure.

`FileManager` is the simplest and requires virtually no setup. It uses a file's raw contents to compute its hash. It supports 2
*write* modes: file moving (renaming), and file copying. The former is preferred (better failure behavior). Because it's rules are
so simple, you can create one with the `stowd` tool provided, and explore its contents either with conventional shell tools or
the `stowex` command line tool. See below. So you don't have to code Java to use this; for the higher level object managers that
follow you do.

`BytesManager` is much like a `FileManager`--e.g. require's no user defined codec, except from the programmer's perspective,
you're dealing with `java.nio.ByteBuffer`s instead of files. Now if you're reading and writing raw bytes in Java, you'll need to
marshall/unmarshall these to values and objects somehow. Or you might consider the next abstraction.

`BinaryObjectManager` is an object manager that uses a user defined `Codec` to marshall/unmarshall objects in the store. These
are quick to code, but their down side is that the data in the file is no longer human readable. The next (and currently last)
abstraction is a compromise to readability.


> Usually computing hash state from machine readable byte sequences is straight forward since in most applications
if two objects' byte sequences are different then their states are different; when state is human readable however, there are
usually multiple valid representations of the same state (white space, comments, item order, etc.), so there's a need to
cannonicalize object state. This is one role of an `Encoder`: it writes an object's state unambiguously to a byte sequence and
is used to compute the object's hash. A `Codec` is an `Encoder` that can read back what it writes.


All these stores can be traversed in lexicographic order of object hashes. And they can be traversed in *parallel* as
the API takes advantage of the java streaming API. An optional, human readable *write-log* can be specified
in order to keep track of the order in which hashes (files) were added.

### Command Line Tools

`stowd` is a background process that watches for new files in any number of user-defined "stow directories" as input and which
are then *moved* to a store (`FileManager`). It requires no programming or setup since it uses a file's raw contents to compute hash.
It also sports a human readable write-log which allows items in the store to be retrieved in the order they were written.
These stow directories, in turn, can be used for language-agnostic, cross-process input.

`stowex` is a command line tool for reading and exploring an existing store. Both tools have a `-help` feature that hopefully makes them self-explanatory.

## Limits

Practically the limits of your storage medium. This uses a pretty scalable, deeper as you grow, directory structure. If maintaining a write log (so as to keep track of order), you need to swap out log files when you hit around 2B files written.

---
## Milestones

Oct. 6 2019: Pushing to make `HexPath` a drop-in replacement for `FilepathGenerator` so that it'll scale to whatever the file system can handle.

Oct. 12 2019: Working on a `java.util.Spliterator` that supports streaming files mananged under a `HexPath` instance. This is complicated by the fact there are multiple possible directory paths for a given hash value since `HexPath` grows the directory structure as more objects are dropped in.

Oct. 20 2019: Streaming support added in `HexPathTree`, a subclass of `HexPath` and dropped in as a replacement for the flat directory used by the `ObjectManager`s. Tested with 64k files. Next steps:

* `BytesManager`: a straight file-contents based object manager (no marshalling)
* Command line tool

Nov. 3 2019: Created `stowex` a command line tool for exploring an existing store.

Nov. 10 2019: Created `stowd` a command line background process that stows files away by monitoring one or more directories for new files and moving them to the store. Next steps:

* Add configurable output/logging to `stowd`
* Command line tool (`stow` ?) for stowing files that can be piped in the shell for input filepaths and output mappings

Nov. 20 2019: Added a random-access list interface to the write-log. Because the write-log is chronologically ordered, you
can binary search this list.

