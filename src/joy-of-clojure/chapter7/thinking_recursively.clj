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

;; Regular recusion is fun again with lazy-seq
;; As mentioned earlier, the lazy-seq recipe rule of thumb #1 states that you should wrap outer-layer function bodies
;; with the lazy-seq macro when generating lazy seqs. The implementation of lz-rec-step used mundane recursion but
;; managed to avoid stack-overflow exceptions thanks to the use of lazy-seq. For functions generating sequences, the use
;; of lazy-seq might be a better choice than tail recursion, because often the regular (mundane) recursive definition is
;; the most natural and understandable.

;; A recursive units calculator
;; Some problems scream out for a recursive solution; take for example the problem of unit conversions. A kilometer
;; consists of 1,000 meters, each made of 100 centimeters, each of which is 10 millimeters, each of which is 1/1,000 of
;; a meter. These types of conversions are often needed in far-ranging applications.
;; If you wanted to describe this relationship in terms of a data structure, then you might land on something like the
;; map that follows:
(def simple-metric {:meter 1,
                    :km 1000,
                    :cm 1/100,
                    :mm [1/10 :cm]})

;; The map simple-metric uses the :meter value as the base unit, or the unit used as the reference point for every other
;; unit. To calculate the answer to "How many meters are in 3 kilometers, 10 meters, 80 centimeters, 10 millimeters?"
;; you could use the map as follows:
(-> (* 3 (:km simple-metric))
    (+ (* 10 (:meter simple-metric)))
    (+ (* 80 (:cm simple-metric)))
    (+ (* (:cm simple-metric)
          (* 10 (first (:mm simple-metric)))))
    float)
;;=> 3010.81

;; Although the map is certainly usable this way, the user experience of traversing simple-metric directly is less than
;; stellar. Instead, it would be nicer to define a function named convert, shown in the following listing, that
;; essentially performs these mathematical operations.

;; Function to recursively convert units of measure
(defn convert [context descriptor]
  (reduce (fn [result [mag unit]]                           ; 1. Destructure aggregates
            (+ result
               (let [val (get context unit)]                ; 2. Look up the relative value
                 (if (vector? val)
                   (* mag (convert context val))            ; 3. Process the aggregate
                   (* mag val)))))                          ; 4. Perform the final calculation
          0
          (partition 2 descriptor)))

;; The action of the convert function programmatically mirrors the manual use of simple-metric shown earlier. The form
;; of the descriptor coming into the function and mirrored in the destructuring form (1) is the opposite of the key/value
;; mapping in the context map. This is because it allows the descriptor to take a more linguistically natural form where
;; the magnitude precedes the unit name (such as 1 :meter).
;; After binding the magnitude mag and the unit name, the value associated with the unit is retrieved (2). In the case
;; where a straight lookup results in a number, the :default case takes over and results in a magnitude multiplication.
;; This straightforward multiplication is the recursion's terminating condition. In the case where a lookup results in a
;; vector, the recursion continues with the vector itself for the purpose of traversing the recursive unit definitions
;; (3). Eventually, the function should bottom out on a nonvector, thus allowing the final magnitude multiplication (4).
;; Because of the recursion, the multiplication rolls up through all the intermediate relative unit values.
;; You can see convert in action next:
(convert simple-metric [1 :meter])
;;=> 1

(convert simple-metric [50 :cm])
;;=> 1/2

(convert simple-metric [100 :mm])
;;=> 1/10

;; And of course, convert should handle compounds:
(float (convert simple-metric [3 :km 10 :meter 80 :cm 10 :mm]))
;;=> 3010.81

;; The beauty of convert is that it's not bound to units of length. Through a synergy between recursive data and
;; recursive function, you've defined a generic unit-conversion specification, allowing other unit types:

(convert {:bit 1, :byte 8, :nibble [1/2 :byte]} [32 :nibble])
;;=> 128N
