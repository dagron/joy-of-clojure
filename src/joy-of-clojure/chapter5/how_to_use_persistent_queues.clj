;; How to use Persistent Queues
;; ---------------------------------------------------------------------------------------------------------------------
;; We mentioned earlier that new Clojure developers often attempt to implement simple queues using vectors. Although
;; this is possible, such an implementation leaves much to be desired. Instead, Clojure provides a persistent immutable
;; queue that will serve all your queueing needs. In this section, we'll touch on the usage of the PersistentQueue
;; class, where its first-in-first-out (FIFO) queueing discipline is described by conj adding to the rear, pop removing
;; from the front, and peek returning the front element without removal.

;; Before going further, it's important to point out that Clojure's PersistentQueue is a collection, not a workflow
;; mechanism. Java has classes deriving from the java.util.concurrent.BlockingQueue interface for workflow, which often
;; are useful in Clojure programs, and those aren't these. If you find yourself wanting to repeatedly check a work queue
;; to see if there's an item of work to be popped off, or if you want to use a queue to send a task to another thread,
;; you do not want the PersistentQueue, because it's an immutable structure; thus there's no way to inherently
;; communicate changes from one worker to another.

;; A queue about nothing
;; ---------------------
;; Search all you like, but the current implementation of Clojure doesn't provide special syntax like the vector, set,
;; and map literals for creating persistent queues. That being the case, how would you go about creating a queue? The
;; answer is that there's a readily available empty queue instance to use, clojure.lang.PersistentQueue/EMPTY. The
;; printed representation for Clojure's queues isn't incredibly informative, but you can change that by providing a
;; method on the print-method multimethod:
(defmethod print-method clojure.lang.PersistentQueue [q, w] ; Overload the printer for queues so they look like fish
  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))

clojure.lang.PersistentQueue/EMPTY
;;=> <-nil-<

;; Defining print-method implementations is a convenient mechanism for printing types in logical ways. This fun format,
;; that we call the queue fish, indicates a direction of flow for conj and pop.

;; You might think that popping an empty queue would raise an exception, but the fact is that this action results in
;; another empty queue. Likewise, peeking an empty queue returns nil. Not breathtaking, for sure, but this behavior
;; helps to ensure that queues work in place of other sequences. The functions first, rest, and next also work on queues
;; and give the results you might expect, although rest and next return seqs, not queues. Therefore, if you're using a
;; queue as a queue, it's best to use the functions designed for this purpose: peek, pop, and conj.

;; Putting things on
;; -----------------
;; The mechanism for adding elements to a queue is conj:
(def schedule
  (conj clojure.lang.PersistentQueue/EMPTY
        :wake-up :shower :brush-teeth))

schedule
;;=> <-(:wake-up :shower :brush-teeth)-<

;; Clojure's persistent queue is implemented internally using two separate collections, the front being a seq and the
;; rear being a vector, as shown in the figure below. All insertions occur in the rear vector, and all removals occur in
;; the front seq, taking advantage of each collection's strength. When all the items from the front list have been
;; popped, the back vector is wrapped in a seq to become the new front, and an empty vector is used as the new back.

;;         Front        Back
;; <-- (1  2  3  4) [5  6  7  8] -<                              ; The two collections used internally in a single
;;      |  |               | /|\                                 ; queue. peek returns the front item of the seq, pop
;;     \|/ +-------+-------+  |                                  ; returns a new queue with the front of the seq left
;;   (peek q)      |      (conj q 8)                             ; off, and conj adds a new item to the back of the
;;                \|/                                            ; vector.
;;              (pop q)

;; Typically, an immutable queue such as this is implemented with the rear as a list in reverse order, because insertion
;; to the front of a list is an efficient operation. But using a Clojure vector eliminates the need for a reversed list.

;; Getting things
;; --------------
;; Clojure provides the peek function to get the front element in a queue:
(peek schedule)
;;=> :wake-up

;; The fact that performing peek doesn't modify the contents of a persistent queue should be no surprise by now.

;; Taking things off
;; -----------------
;; To remove elements from the front of a queue, use the pop function and not rest:
(pop schedule)
;;=> <-(:shower :brush-teeth)-<

(rest schedule)
(:shower :brush-teeth)

;; Although rest returns something with the same values and even prints the same thing pop returns, the former is a seq,
;; not a queue. This is potentially the source of subtle bugs, because subsequent attempts to use conj on the returned
;; seq won't preserve the speed guarantees of the queue type, and the queue functions pop, peek, and conj won't behave
;; as expected.

;; We've talked numerous times in this chapter about the sequence abstraction, and although it's an important
;; consideration, it shouldn't always be used. Instead, it's important to know your data structures, their sweet spots,
;; and their operations. By doing so, you can write code that's specialized in ways that use the performance
;; characteristics you need for a given problem space. Clojure's persistent queues illustrate this fact perfectly.

