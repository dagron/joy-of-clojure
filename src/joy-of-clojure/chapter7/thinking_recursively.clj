;; Thinking recursively
;; ---------------------------------------------------------------------------------------------------------------------
;; You're likely already familiar with the basics of recursion, and as a result you can take heart that we won't force
;; you to read a beginner's tutorial again. But because recursive solutions are prevalent in Clojure code, it's
;; important for us to cover it well enough that you can fully understand Clojure's recursive offerings.

;; Recursion is often viewed as a low-level operation reserved for times when solutions involving higher-order functions
;; either fail or lead to obfuscation. Granted, it's fun to solve problems recursively because even for those of us
;; who've attained some level of acumen with functional programming, finding a recursive solution still injects a bit of
;; magic into our day. Recursion is a perfect building block for creating higher-level looping constructs and functions,
;; as we'll show in this section.

;; Mundane recursion
;; A classically recursive algorithm is that of calculating some base number raised to an exponent, or the pow function.
;; A straightforward way to solve this problem recursively is to multiply the base by each successively smaller value of
;; the exponent, as implemented here:
(defn pow [base exp]
  (if (zero? exp)
    1
    (* base (pow base (dec exp)))))

(pow 2 10)
;;=> 1024

(pow 1.01 925)
;;=> 9937.353723241924

;; We say that the recursive call is mundane because it's named explicitly rather than through mutual recursion or
;; implicitly with the recur special form. Why is this a problem? The answer lies in what happens when you try to call
;; pow with a large value:
(pow 2 10000)
;;=> java.lang.StackOverflowError

;; The implementation of pow is doomed to throw java.lang.StackOverflowError because the recursive call is trapped by
;; the multiplication operation. The ideal solution is a tail-recursive version that uses the explicit recur form, thus
;; avoiding stack consumption and the resulting exception. One way to remove the mundane recursive call is to perform
;; the multiplication at a different point, thus freeing the recursive call to occur in the tail position, as shown next:
(defn pow [base exp]
  (letfn [(kapow [base exp acc]
                 (if (zero? exp)
                   acc
                   (recur base (dec exp) (* base acc))))]
    (kapow base exp 1)))
(pow 2N 10000)
;;=> ... a ridiculously large number

;; This new version of pow uses two common techniques for converting mundane recursion to tail recursion. First, it uses
;; a helper function kapow that does the majority of the work. Second, kapow uses an accumulator acc that holds the
;; result of the multiplication. The exponent exp is no longer used as a multiplicative value but instead functions as a
;; decrementing counter, eliminating a stack explosion.
