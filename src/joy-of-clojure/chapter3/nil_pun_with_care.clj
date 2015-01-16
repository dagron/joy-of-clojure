;; Nil pun with care
;; ---------------------------------------------------------------------------------------------------------------------
;; Because empty collections act like true in Boolean contexts, you need an idiom for testing whether there's anything
;; in a collection to process. Thankfully, Clojure provides such a technique:
(seq [1 2 3])
;;=> (1 2 3)

(seq [])
;;=> nil

;; The seq function returns a sequence view of a collection, or nil if the collection is empty. In a language like
;; Common Lisp, an empty list acts as a false value and can be used as a pun (a term with the same behavior) for such in
;; determining a looping termination. As you saw earlier, Clojure's empty sequences are instead truthy, and therefore to
;; use one as a pun for falsity will lead to heartache and despair. One solution that might come to mind is to use
;; empty? in the test, leading to the awkward phrase (when-not (empty? s) ...). A better solution is to use seq as a
;; termination condition, as in the following function print-seq:
(defn print-seq [s]
  (when (seq s)         ;; Check for empty
    (prn (first s))
    (recur (rest s))))  ;; Recur

;; There are a number of points to take away from this example. First, the use of seq as a terminating condition is the
;; preferred way to test whether a sequence is empty. If you tested just s instead of (seq s), then the terminating
;; condition wouldn't occur even for empty collections, leading to an infinite loop. Thankfully, the use of seq allows
;; you to properly check for an empty collection:
(print-seq [])
;;=> nil

;; Second, rest is used instead of next to consume the sequence on the recursive call. Although they’re nearly identical
;; in behavior, rest can return a sequence that's either empty or not empty (has elements), but it never returns nil. On
;; the other hand, next returns a seq of the rest, or (seq (rest s)), and thus never returns an empty sequence,
;; returning nil in its place. It's appropriate to use rest here because you're using seq explicitly in each subsequent
;; iteration. Observe:
(print-seq [1 2])
;; 1
;; 2
;;=> nil

;; As shown, the print-seq function uses seq and recursion (via recur) to "consume" a collection, printing its elements
;; along the way using prn. In fact, print-seq is a template for most functions in Clojure: it shows that you generally
;; shouldn't assume seq has been called on your collection arguments, but instead call seq in the function itself and
;; process based on its result. Using this approach fosters a more generic handling of collections, a topic that we
;; explore in great detail later on. In the meantime, it's important to keep in mind the difference between empty
;; collections and false values; otherwise your attempts at nil punning may cause groans all around.

;; NOTE An important point to mention is that it would be best to use doseq to iterate over the collection rather than
;; an explicit recursion, but that wouldn't allow us to illustrate the point at hand: the Clojure forms named with do at
;; the start (doseq, dotimes, do, and so on) are intended for side effects in their bodies and generally return nil as
;; their results.
