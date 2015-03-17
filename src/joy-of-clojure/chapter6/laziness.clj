;; Laziness
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure is partially a lazy language. This isn't to say that Clojure vectors lie around the house every day after
;; school playing video games and refusing to get a job. Instead, Clojure is lazy in the way it handles its sequence
;; types -- but what does that mean?

;; First, we'll start by defining what it means for a language to be eager, or, in other words, not lazy. Many
;; programming languages are eager in that arguments to functions are immediately evaluated when passed, and Clojure in
;; most cases follows this pattern as well. Observe the following:
(- 13 (+ 2 2))
;;=> 9

;; The expression (+ 2 2) is eagerly evaluated, in that its result 4 is passed on to the subtraction function during the
;; actual call and not at the point of need. But a lazy programming language such as Haskell will evaluate a function
;; argument only if that argument is needed in an overarching computation.

;; In this section, we'll discuss how you can use laziness to avoid nontermination, unnecessary calculations, and even
;; combinatorially exploding computations. We'll also discuss the matter of utilizing infinite sequences, a surprisingly
;; powerful technique. Finally, you'll use Clojure's delay and force to build a simple lazy structure. First, we'll
;; start with a simple example of laziness that you may be familiar with from Java.

;; Familiar laziness with logical-and
;; ----------------------------------
;; Laziness isn't limited to the case of the evaluation of function arguments; a common example can be found even in
;; eager programming languages. Take the case of Java's logical-and operator &&. Java implementations, by dictate of the
;; specification, optimize this particular operator to avoid performing unnecessary operations should an early
;; subexpression evaluate to false. This lazy evaluation in Java allows the following idiom:

;; if (obj != null && obj.isWhatiz ()) {
;;   ...
;; }

;; For those of you unfamiliar with Java, the preceding code says, "If the object obj isn't null, then call the method
;; isWhatiz." Without a short-circuiting (or lazy, if you will) && operator, the preceding operation would always throw
;; a java.lang.NullPointerException whenever obj was set to null. Although this simple example doesn't qualify Java as a
;; lazy language, it does illustrate the first advantage of lazy evaluation: laziness allows the avoidance of errors in
;; the evaluation of compound structures.

;; Clojure's and operator also works this way, as do a number of other operators, but we won't discuss this type of
;; short-circuiting laziness too deeply. The following listing illustrates what we mean, using the case of a series of
;; nested if expressions.

;; Short-circuiting if expression
;; ------------------------------
(defn if-chain [x y z]
  (if x
    (if y
      (if z
        (do
          (println "Made it!")
          :all-truthy)))))

(if-chain () 42 true)
;; Made it!
;;=> :all-truthy

(if-chain true true false)
;;=> nil

;; The call to println is evaluated only in the case of three truthy arguments. But you can perform the equivalent
;; action given only the and macro:
(defn and-chain [x y z]
  (and x y z (do (println "Made it!") :all-truthy)))

(and-chain () 42 true)
;; Made it!
;;=> :all-truthy

(and-chain true false true)
;;=> false

;; You may see tricks like this from time to time, but they're not often found in Clojure code. Regardless, we've
;; presented them as a launching point for the rest of the discussion in this section. We'll now proceed to discuss how
;; your own Clojure programs can be made more generally lazy by following an important recipe.

;; Understanding the lazy-seq recipe
;; ---------------------------------
;; Here's a seemingly simple function, steps, that takes a sequence and makes a deeply nested structure from it:
(steps [1 2 3 4])
;;=> [1 [2 [3 [4 []]]]]

;; Seems simple enough, no? Your first instinct might be to tackle this problem recursively, as suggested by the form of
;; the desired result:
(defn steps-recursive [[x & xs]]
  (if x
    [x (steps-recursive xs)]
    []))

(steps-recursive [1 2 3 4])
;;=> [1 [2 [3 [4 []]]]]

;; Things look beautiful at this point; you've created a simple solution to a simple problem. But therein bugbears lurk.
;; What would happen if you ran this same functino on a large set?
(steps-recursive (range 2000000))
;;=> CompilerException java.lang.StackOverflowError

;; Observing the example, running the same function over a sequence of 200,000 elements causes a stack overflow. How can
;; you fix this problem? Perhaps it's fine to say that you'll never encounter such a large input set in your own
;; programs; such trade-offs are made all the time. But Clojure provides lazy sequences to help tackle such problems
;; without significantly complicating your source code. Additionally, your code should strive to deal with, and produce,
;; lazy sequences.

;; Stepping back a bit, let's examine the lazy-seq recipe for applying laziness to your own functions:
;; 1. Use the lazy-seq macro at the outermost level of your lazy-sequence-producing expresion(s).
;; 2. If you happen to be consuming another sequence during your operations, then use rest instead of next.
;; 3. Prefer higher-order functions when processing sequences.
;; 4. Don't hold on to your head.

;; These rules of thumb are simple, but they take some practice to utilize to their fullest. For example, #4 is
;; especially subtle in that the trivial case is easy to conceptualize, but it's more complex to implement in large
;; cases. For now we'll gloss over #3, because we'll talk about that approach separately later on.

;; rest vs. next
;; -------------
;; What happens when you create a potentially infinite sequence of integers using iterate, printing a dot each time you
;; generate a new value, and then use either rest or next to return the first three values of the sequence? The
;; difference between rest and next can be seen in the following example:
(def very-lazy (-> (iterate #(do (print \.) (inc %)) 1)
                   rest rest rest))
;;=> ..#'user/very-lazy

(def less-lazy (-> (iterate #(do (print \.) (inc %)) 1)
                   next next next))
;;=> ...#'user/less-lazy

;; As shown, the next version printed three dots, whereas the rest version printed only two. When building a lazy seq
;; from another, rest doesn't cause the calculation of (or realize) any more elements than it needs to; next does. In
;; order to determine whether a seq is empty, next needs to check whether there's at least one thing in it, thus
;; potentially causing one extra realization. Here's an example:
(println (first very-lazy))
;; .4
;;=> nil

(println (first less-lazy))
;; 4
;;=> 4

;; Grabbing the first element in a lazy seq built with rest causes a realization as expected. but the same doesn't
;; happen for a seq built with next because it's already been previously realized. Using next causes a lazy seq to be
;; one element less lazy, which might not be desired if the cost of realization is expensive. In general, we recommend
;; that you use next unless you're specifically trying to write code to be as lazy as possible.

;; Using lazy-seq and rest
;; -----------------------
;; In order to be a proper lazy citizen, you should produce lazy sequences using the lazy-seq macro. Here's a lazy
;; version of steps-recursive that addresses the problems with the previous implementation.

(defn lazy-steps-recursive [s]
  (lazy-seq
    (if (seq s)
      [(first s) (lazy-steps-recursive (rest s))]
      [])))

(lazy-steps-recursive [1 2 3 4])
;;=> (1 (2 (3 (4 ()))))

(class (lazy-steps-recursive [1 2 3 4]))
;;=> clojure.lang.LazySeq

(dorun (lazy-steps-recursive (range 2000000)))
;;=> nil

;; There are a few points of note for this new implementation. As we mentioned in our rules of thumb, when consuming
;; a sequence in the body of a lazy-seq, you want to use rest, as in lazy-steps-recursive. Second, you're no longer
;; producing nested vectors as the output of the function, but instead a lazy sequence LazySeq, which is the byproduct
;; of the lazy-seq macro.

;; With only minor adjustments, you've created a lazy version of the step function while also maintaining simplicity.
;; The first two rules of the lazy-sequence recipe can be used in all cases when producing lazy sequences. Note them in
;; the previous example -- the use of lazy-seq as the outermost form in the function and the use of rest. You'll see
;; this pattern over and over in Clojure code found in the wild.

;; If what's going on here still doesn't quite make sense to you, consider this even simpler example:
(defn simple-range [i limit]
  (lazy-seq
    (when (< i limit)
      (cons i (simple-range (inc i) limit)))))

;; This behaves similarly to Clojure's built-in function range, but it's simpler in that it doesn't accept a step
;; argument and has no support for producing chunked seqs:
(simple-range 0 9)
;;=> (0 1 2 3 4 5 6 7 8)

;; Note that it follows all the lazy-seq recipe rules you've seen so far. The figure below is a representation of what's
;; in memory when the REPL has printed the first two items in a simple-range seq but hasn't yet printed any more
;; than that.

;; Complications may arise if you accidentally hold on to the head of a lazy sequence. This is addressed by the fourth
;; rule of lazy sequences.

;; Each step of a lazy seq may be in one of two states. If the step is unrealized, it contains a function or closure of
;; no arguments that can be called later to realize the step. When this happens, the thunk's return value is cached
;; instead, and the thunk itself is released as pictured in the first two lazy seq boxes, transitioning the step to the
;; realized state. Note that although not shown here, a realized lazy seq may contain nothing at all, called nil,
;; indicating the end of the seq.

;; +--------+        +--------+        +------------------------+
;; |lazy-seq|   +--->|lazy-seq|   +--->|        lazy-seq        |
;; +--------+   |    +--------+   |    +------------------------+
;; |+------+|   |    |+------+|   |    |+----------------------+|
;; || cons ||   |    || cons ||   |    ||        thunk         ||
;; |+------+|   |    |+------+|   |    |+----------------------+|
;; || 0 |* ||---+    || 1 |* ||---+    ||  (simple-range 2 9)  ||
;; |+------+|        |+------+|        |+----------------------+|
;; +--------+        +--------+        +------------------------+

;; Losing your head
;; ----------------
;; The primary advantage of laziness in Clojure is that it prevents the full realization of interim results during a
;; calculation. If you manage to hold on to the head of a sequence somewhere within a function, then that sequence will
;; be prevented from being garbage collected. The simplest way to retain the head of a sequence is to bind it
;; to a local. This condition can occur with any type of value bind, be it to a reference type or through the usage of
;; let or binding:
(let [r (range 1e9)]
  (first r)
  (last r))
;;=> 999999999

(let [r (range 1e9)]
  (last r)
  (first r))
;;=> java.lang.OutOfMemoryError: GC overhead limit exceeded

;; Clojure's compiler can deduce that in the first example, the retention of r is no longer needed when the computation
;; of (last r) occurs, and therefore Clojure aggressively clears it. But in the second example, the head is needed later
;; in the overall computation and can no longer be safely cleared. Of course, the compiler could perform some
;; rearranging with the order of operations for this case, but it doesn't because in order to do so safely it would have
;; to guarantee that all the composite functions were pure. It's OK if you're not clear on what a pure function is right
;; now -- we'll cover it later on. In a nutshell, take to heart that Clojure can't rearrange operations, because there's
;; no way to guarantee that order is unimportant. This is one area where a purely functional lazy language such as
;; Haskell shines by comparison.

;; Employing infinite sequences
;; ----------------------------
;; Because Clojure's sequences are lazy, they have the potential to be infinitely long. Clojure provides a number of
;; functions for generating and working with infinite sequences:

;; Run at your own risk!
(iterate (fn [n] (/ n 2)) 1)
;;=> (1 1/2 1/4 1/8 1/16 1/32 1/64 1/128 1/256 1/512...)

;; It sure is a nice trick (although you might not think so had you chosen to ignore our warning), but what could you
;; possibly use infinite sequences for? Working with infinite sequences often fosters more declarative solutions. Take a
;; simple example as a start. Imagine that you have a function that calculates a triangle number for a given integer:
(defn triangle [n]
  (/ (* n (+ n 1)) 2))

(triangle 10)
;;=> 55

;; The function triangle can then be used to build a sequence of the first 10 triangle numbers:
(map triangle (range 1 11))
;;=> (1 3 6 10 15 21 28 36 45 55)

;; There's nothing wrong with the preceding solution, but it suffers from a lack of flexibility in that it does what it
;; does and that's all. By defining a sequence of all the triangle numbers, as in the following listing, you can perform
;; more interesting "queries" in order to retrieve the desired elements.

;; Infinite sequences fostering declarative solutions
(def triangle-numbers (map triangle (iterate inc 1)))

(take 10 triangle-numbers)                                  ; Get the first 10
;;=> (1 3 6 10 15 21 28 36 45 55)

(take 10 (filter even? triangle-numbers))                   ; Get the first 10 even
;;=> (6 10 28 36 66 78 120 136 190 210)

(nth triangle-numbers 99)                                   ; What Gauss found
;;=> 5050

(double (reduce + (take 1000 (map / triangle-numbers))))    ; Converge on 2
;;=> 1.998001998001998

(take 2 (drop-while #(< % 10000) triangle-numbers))         ; First 2 greater than 10,000
;;=> (10011 10153)

;; The queries use three ubiquitous Clojure functions: map, reduce, and filter. The map function applies a function to
;; each element in a sequence and returns the resulting sequence. The reduce function applies a function to each value
;; in the sequence and the running result to accumulate a final value. Finally, the filter function applies a function
;; to each element in a sequence and returns a new sequence of those elements where said function returned a truthy
;; value. All three of these functions retain the laziness of a given sequence.

;; Defining the infinite sequence of triangle numbers allows you to take elements from it as needed, only calculating
;; those particular items.
