;; Blocks
;; ---------------------------------------------------------------------------------------------------------------------
;; Use the do form when you have a series or block of expressions that need to be treated as one. All the expressions
;; are evaluated, but only the last one is returned:
(do
  (def x 5)
  (def y 4)
  (+ x y)
  [x y])
;;=> [5 4]

;; The expressions (def x 5), (def y 4), and (+ x y) are executed one by one in the body of the do block. Even the
;; addition (+ x y) is executed, but the value is thrown away—only the final expression [x y] is returned. The middle
;; bits of the do block are typically where the side effects occur, as shown with the use of def. Whenever you see a
;; Clojure form with a name starting with do, you can assume that its purpose is related to side-effectful activities
;; like defining a var, printing, and so on.


;; Locals
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure doesn't have local variables, but it does have locals; they just can't vary. Locals are created and their
;; scope is defined using a let form, which starts with a vector that defines the bindings, followed by any number
;; of expressions that make up the body. The vector begins with a binding form (usually a symbol), which is the name
;; of a new local. This is followed by an expression whose value is bound to this new local for the remainder of
;; the let form. You can continue pairing binding names and expressions to create as many locals as you need.
;; All of them are available in the body of the let:
(let [r         5
      pi        3.1415
      r-squared (* r r)]
  (println "radius is" r)
  (* pi r-squared))
;; radius is 5
;;=> 78.53750000000001


;; Loops
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure has a special form called recur that's specifically for tail recursion. The following function prints the
;; integers from x to 1, counting backward:
(defn print-down-from [x]
  (when (pos? x)
    (println x)
    (recur (dec x))))

;; This is nearly identical to how you'd structure a while loop in an imperative language. One significant difference is
;; that the value of x isn't decremented somewhere in the body of the loop. Instead, a new value is calculated
;; as a parameter to recur, which immediately does two things: rebinds x to the new value and returns control to the top
;; of print-down-from.

;; If the function has multiple arguments, the recur call must as well, just as if you were calling the function by name
;; instead of using the recur special form. And just as with a function call, the expressions in the recur are evaluated
;; in order first and only then bound to the function arguments simultaneously.

;; The previous example doesn't concern itself with return values; it's just about the println side effects.
;; Here's a similar loop that builds up an accumulator named sum, which adds the numbers between 1 and x, inclusive,
;; and returns the sum:
(defn sum-down-from [sum x]
  (if (pos? x)
    (recur (+ sum x) (dec x))
    sum))

;; You may have noticed that the two preceding functions use different blocks: the first when and the second if.
;; You'll often see one or the other used as a conditional, but it's not always immediately apparent why.

;; In general, you should use when in these cases:
;; * No else part is associated with the result of a conditional.
;; * You require an implicit do in order to perform side effects.
;; If neither of these is true, you should use if.

;; Sometimes you want to loop back not to the top of the function, but to somewhere inside it. For example,
;; in sum-down-from, you might prefer that callers not have to provide an initial value for sum. To help, there's a loop
;; form that acts exactly like let but provides a target for recur to jump to. It's used like this:
(defn sum-down-from [initial-x]
  (loop [sum 0, x initial-x]
    (if (pos? x)
      (recur (+ sum x) (dec x))
      sum)))

;; Upon entering the loop form, the locals sum and x are initialized, just as they would be for a let. A recur always
;; loops back to the closest enclosing loop or fn, so in this case it goes to loop. The loop locals are re-bound to the
;; values given in recur. The looping and rebinding continue until finally x is no longer positive. The return value of
;; the entire loop expression is sum, just as it was for the earlier function.

;; If you try to use the recur form somewhere other than a tail position, Clojure will remind you at compile time:
(fn [x] (recur x) (println x))
;; java.lang.UnsupportedOperationException: Can only recur from tail position
