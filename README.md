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
that subdirectory. (See [`HexPath`](https://github.com/gnahraf/stowkwik/blob/master/src/main/java/com/gnahraf/io/HexPath.java)
for implementation details.)

You can overlay multiple stores on top of the same directory structure (as long as their extensions differ). This is not
recommended unless the cardinality of one extension is of the same order of magnitude as that of the other.

## Build

Build dependencies are automatically managed by maven. However, there's a `com.gnahraf` dependency which for now you first
have to build yourself. (Sorry.)

* Hop over to [io-util project](https://github.com/gnahraf/io-util) and clone it.
* Switch to the `io-util` directory and invoke `$ mvn clean install -DskipTests=true`
* Return to this project's directory and invoke `$ mvn clean package appassembler:assemble`

### Suggested "Installation"

Though this is a *programming library* the last step above creates a couple of command line tools (still in the rough),
`storex` and `stowd`. To use these, you're going need to adjust your `PATH` environment variable. My usual practice with
apps built by maven is to include `$HOME/bin` on the `PATH` and then

> `$ cp -rf target/appassembler/* ~`

which will copy the `bin` and `repo` directories to your home directory.

To use this library with other maven projects do the same you did with `io-utils`..

> `$ mvn clean install`

### Javadoc

My main use for annotating code with javadoc comments is for the context bubbles my IDE pops on hovering the mouse
over code. I comment more than most tho, so generating this might help. Incant the following in the project directory:

> `$ mvn javadoc:javadoc`

If your `JAVA_HOME` environment variable is not set you'll need to set it for this to work. On MacOS, you do this with

> `$ export JAVA_HOME="$(/usr/libexec/java_home -v 10.0)"`

which is a pain in the arse. (Note the funky version number--not a typo :/ )

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
in order to unamibigously compute hash state (we don't want an object's hash to change if we switch XML libraries, for instance. Dev note: should be able to automate). If on the other hand, you want to marshal objects elsewhere and just want
to throw blobs of bytes into a store, you can use a `BytesManager` which doesn't require any codec. `FileManager` is like
`BytesManager` but is more efficient in many respects and is better at managing larger files (blobs).

All these stores can be traversed in lexicographic order of object hashes. And they can be traversed in *parallel* as
the API takes advantage of the java streaming API. An optional, human readable *write-log* can be specified
in order to keep track of the order in which hashes (files) were added.

Since `FileManager` is so simple, a command line interface for it seemed feasible: `stowd` is a background process that
watches for new files in any number of user-defined "stow directories" as input and which are then *moved* to a store.
It also sports a human readable write-log which allows items in the store to be retrieved in the order they were written.
These stow directories, in turn, can be used for language-agnostic, cross-process input.

`storex` is a command line tool for exploring an existing store. Both tools have a `-help` feature that hopefully makes them self-explanatory.

### Unit Tests

Unit tests are always a good place to see how code works.

The [unit tests](https://github.com/gnahraf/stowkwik/tree/master/src/test/java/com/gnahraf/stowkwik) contain a mock example. See

* `Mock` - the mock object
* `MockEncoder` - encoder used to write mock objects to memory (also used to compute a mock's hash when written as XML)
* `MockCodec` - read/write codec used by the `BinaryObjectManager<Mock>`

## Limits

Practically the limits of your storage medium. This uses a pretty scalable, deeper as you grow, directory structure. If maintaining a write log (so as to keep track of order), you need to swap out log files when you hit around 2B files written.

## Milestones

Oct. 6 2019: Pushing to make `HexPath` a drop-in replacement for `FilepathGenerator` so that it'll scale to whatever the file system can handle.

Oct. 12 2019: Working on a `java.util.Spliterator` that supports streaming files mananged under a `HexPath` instance. This is complicated by the fact there are multiple possible directory paths for a given hash value since `HexPath` grows the directory structure as more objects are dropped in.

Oct. 20 2019: Streaming support added in `HexPathTree`, a subclass of `HexPath` and dropped in as a replacement for the flat directory used by the `ObjectManager`s. Tested with 64k files. Next steps:

* `BytesManager`: a straight file-contents based object manager (no marshalling)
* Command line tool

Nov. 3 2019: Created `storex` a command line tool for exploring an existing store.

Nov. 10 2019: Created `stowd` a command line background process that stows files away by monitoring one or more directories for new files and moving them to the store. Next steps:

* Add configurable output/logging to `stowd`
* Command line tool (`stow` ?) for stowing files that can be piped in the shell for input filepaths and output mappings

Nov. 20 2019: Added a random-access list interface to the write-log. Because the write-log is chronologically ordered, you
can binary search this list.
