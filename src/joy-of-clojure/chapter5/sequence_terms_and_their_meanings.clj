;; Sequence terms and what they mean
;; ---------------------------------------------------------------------------------------------------------------------
;; The words sequential, sequence, and seq don't sound very different from each other, but they mean specific things in
;; Clojure. We'll start with specific definitions of each term to help you tell them apart, and then go into a bit of
;; detail about how they relate to equality partitions and the sequence abstraction.

;; A sequential collection is one that holds a series of values without reordering them. As such, it's one of three
;; broad categories of collection types along with sets and maps.

;; A sequence is a sequential collection that represents a series of values that may or may not exist yet. They may be
;; values from a concrete collection or values that are computed as necessary. A sequence may also be empty.

;; Clojure has a simple API called seq for navigating collections. It consist of two functions: first and rest. If the
;; collection has anything in it, (first coll) returns the first element; otherwise it returns nil. (rest coll) returns
;; a sequence of the items other than the first. If there are no other items, rest returns an empty sequence and never
;; nil. As you may recall from earlier, the way that Clojure treats nil versus empty collections motivates the iteration
;; patterns used in the language. Clojure functions that promise to return sequences, such as map and filter, work the
;; same way as rest. A seq is any object that implements the seq API, thereby supporting the functions first and rest.
;; You might consider it an immutable variant of an enumerator or iterator where the lack of internal state allows near
;; limitless concurrent and parallel iteration over the same seq.

;; Throughout this repository, we'll use very precise terms for certain aspects and features of Clojure.
;; The table below provides a summary of some of the collection-specific terms.

;; Term                       Brief Description                   Examples
;; ---------------------------------------------------------------------------------------------------------------------
;; Collection                 A composite data type               [1  2], {:a 1}, #{1 2}, and lists and arrays
;; Sequential                 Ordered series of values            [1 2 3 4], (1 2 3 4)
;; Sequence                   A sequential collection that may    The result of (map some-fun some-coll)
;;                            or may not exist yet
;; Seq                        Simple API for navigating           first, rest, nil and ()
;;                            collections
;; clojure.core/seq           A function that returns an object   (seq []) ;;=> nil, and (seq [1 2]) ;;=> (1 2)
;;                            implementing the seq API

;; There's also a function called seq that accepts a wide variety of collection-like objects. Some collections, such as
;; lists, implement the seq API directly, so calling seq on them returns the collection itself. More often, calling seq
;; on a collection returns a new seq object for navigating that collection. In either case, if the collection is empty,
;; seq returns nil and never an empty sequence. Functions that promise to return seqs (not sequences), such as next,
;; work the same way.

;; Clojure's sequence library manipulates collections, strings, arrays, and so on as if they were sequences, using the
;; seq function and seq API.

;; Beware type-based predicates:
;; Clojure includes a few predicates with names like the words just defined. Although they're not frequently used, it
;; seems worth mentioning that they may not mean exactly what the definitions here might suggest. For example, every
;; object for which sequential? returns true is a sequential collection, but it returns false for some that are also
;; sequential. This is because of implementation details that may be improved in a future version of Clojure.

;; Equality Partitions:
;; As we mentioned previously, Clojure classifies each collection data type into one of three logical categories or
;; partitions: sequentials, maps, and sets. These divisions draw clear distinctions between the types and help define
;; equality semantics. Specifically, two objects will never be equal if they belong to different partitions. Few
;; collection types are actually sequences, although several such as vectors are sequential. If two sequentials have the
;; same values in the same order, = returns true for them, even if their concrete types are different, as shown:
(= [1 2 3] '(1 2 3))
;;=> true

;; Conversely, even if two collections have the same exact values, if one is a sequential collection and the other
;; isn't, = returns false:
(= [1 2 3] #{1 2 3})
;;=> false

;; Examples of things that are sequential include Clojure lists and vectors, and Java lists such as java.util.ArrayList.
;; In fact, everything that implements java.util.List is included in the sequential partition. Generally, things that
;; fall into the other partitions include set or map in their name and so are easy to identify.

;; The Sequence Abstraction:
;; Many Lisps build their data types on the cons-cell abstraction, an elegant two-element structure illustrated in the
;; figure below. The cons-cell is used to build a linked list structure akin to the java.util.LinkedList type in the
;; Java core library. In fact, although the cons-cell is the base structure on which traditional Lisps are built, the
;; name Lisp comes from its focus on list processing.

;; Each cons-cell is a simple pair, a car and a cdr. (A) A list with two cells, each of which has a value -- x and y,
;; respectively -- as the head (the car in Lisp terminology) and a list as the tail (the cdr). This is very similar to
;; first and rest in Clojure sequences. (B) A cons-cell with a simple value for both the head and tail. This is called a
;; dotted pair but is not supported by any of Clojure's built-in types.

;;    +---+    +---+            +---+---+
;; (A)|   | -> |   | -> ()   (B)|   |   | -> y
;;    +---+    +---+            +---+---+
;;      |        |                |
;;     \|/      \|/              \|/
;;      x        y                x

;; Clojure also has a couple of cons-cell-like structures that are covered later on, but they're not central to
;; Clojure's design. Instead, the conceptual interface fulfilled by the cons-cell has been lifted off the concrete
;; structure illustrated previously and been named sequence. All an object needs to do to be a sequence is to support
;; the two core functions: first and rest. This isn't much, but it's all that's required for the bulk of Clojure's
;; powerful library of sequence functions and macros to be able to operate on the collection: filter, map, for, doseq,
;; take, partition ... the list goes on.

;; At the same time, a wide variety of objects satisfy this interface. Every Clojure collection provides at least one
;; kind of seq object for walking through its contents, exposed via the seq function. Some collections provide more than
;; one; for example, vectors support rseq, and maps support the functions keys and vals. All of these functions return a
;; seq or, if the collection is empty, nil.

;; You can see examples of this by looking at the types of objects returned by various expressions. Here's the map
;; class:
(class (hash-map :a 1))
;;=> clojure.lang.PersistentHashMap

;; Unsurprisingly, the hash-map function returns an object of type PersistentHashMap. Passing that map object to seq
;; returns an entirely new kind of object:
(seq (hash-map :a 1))
;;=> ([:a 1])

(class (seq (hash-map :a 1)))
;;=> clojure.lang.PersistentHashMap$NodeSeq

;; This class name suggests it's a seq of nodes on a hash map. Similarly, you can get a seq of keys on the same map:
(seq (keys (hash-map :a 1)))
;;=> (:a)

(class (seq (keys (hash-map :a 1))))
;;=> clojure.lang.APersistentMap$KeySeq

;; Note that these specific class names are an implementation detail that may change in the future, but the concepts
;; they embody are central to Clojure and unlikely to change.
