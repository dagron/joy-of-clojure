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
