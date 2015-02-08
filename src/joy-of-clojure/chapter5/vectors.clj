;; Vectors: creating and using them in all their varieties
;; ---------------------------------------------------------------------------------------------------------------------
;; Vectors store zero or more values sequentially, indexed by number; they're a bit like arrays but are immutable and
;; persistent. They're versatile and make efficient use of memory and processor resources at both small and large sizes.
;; Vectors are probably the most frequently used collection type in Clojure code. They're used as literals for argument
;; lists and let bindings, for holding large amounts of application data, as stacks, and as map entries. We'll also
;; address efficiency considerations, including growing on the right end, subvectors, and reversals, and finally we'll
;; discuss cases in which vectors aren't an optimal solution.

;; Building Vectors
;; ----------------
;; The vector's literal square-bracket syntax is one reason you may choose to use a vector over a list. For example, the
;; let form would work perfectly well, and with a nearly identical implementation, if it took a literal list of bindings
;; instead of a literal vector. But the square brackets are visually different from the round parentheses surrounding
;; the let form itself as well as the likely function calls in the body of the let form, and this is useful for humans
;; (so we hear). Using vectors to indicate bindings for let, with-open, fn, and such is idiomatic Clojure practice and
;; is a pattern you're encouraged to follow in any similar macros you write.

;; The most common way to create a vector is with the literal syntax like [1 2 3]. But in many cases, you'll want to
;; create a vector out of the contents of some other kind of collection. For this, there's the function vec:
(vec (range 10))
;;=> [0 1 2 3 4 5 6 7 8 9]

;; If you already have a vector but want to pour several values into it, then into is your friend:
(let [my-vector [:a :b :c]]
  (into my-vector (range 10)))
;;=> [:a :b :c 0 1 2 3 4 5 6 7 8 9]

;; If you want it to return a vector, the first argument to into must be a vector. The second arg can be any sequence,
;; such as what range returns, or anything else that works with the seq function. You can view the operation of into as
;; similar to an O(n) concatenation based on the size of the second argument. Clojure also provides a vector function to
;; build a vector from its arguments, which is handy for constructs like (map vector a b).

;; Clojure can store primitive types inside of vectors using the vector-of function, which takes any of :int, :long,
;; :float, :double, :byte, :short, :boolean, or :char as its argument and returns an empty vector. This returned vector
;; acts like any other vector, except that it stores its contents as primitives internally. All the normal vector
;; operations still apply, and the new vector attempts to coerce any additions into its internal type when being added:
(into (vector-of :int) [Math/PI 2 1.3])
;;=> [3 2 1]

(into (vector-of :char) [100 101 102])
;;=> [\d \e \f]

(into (vector-of :int) [1 2 623876371267813267326786327863])
;;=> CompilerException java.lang.IllegalArgumentException: Value out of range for long: 623876371267813267326786327863

;; In addition, all caveats mentioned previously regarding overflow, underflow, and so forth also apply to vectors of
;; primitives.

;; Using vec and into, it's easy to build vectors much larger than can be conveniently built using vector literals.
;; But once you have a large vector like that, what are you going to do with it?
