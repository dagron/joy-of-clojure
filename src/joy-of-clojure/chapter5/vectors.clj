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

;; Large Vectors
;; -------------
;; When collections are small, the performance differences between vectors and lists hardly matter. But as both get
;; larger, each becomes dramatically slower at operations the other can still perform efficiently. Vectors are
;; particularly efficient at three things relative to lists: adding or removing things from the right end of the
;; collection, accessing or changing items in the interior of the collection by numeric index, and walking in reverse
;; order. Adding and removing from the end is done by treating the vector as a stack we'll cover that in just a bit.

;; Any item in a vector can be accessed by its index number from 0 up to but not including (count my-vector) in
;; essentially constant time. You can do this using the function nth; using the function get, essentially treating the
;; vector like a map; or by invoking the vector as a function. Look at each of these as applied to this example vector:
(def a-to-j (vec (map char (range 65 75))))

a-to-j
;;=> [\A \B \C \D \E \F \G \H \I \J]

;; All three of these do the same work, and each returns \E:
(nth a-to-j 4)
;;=> \E

(get a-to-j 4)
;;=> \E

(a-to-j 4)
;;=> \E

;; Which to use is a judgment call, but the table below highlights some points you might consider when choosing.

;; Vector lookup options: the three ways to look up an item in a vector,
;; and how each responds to different exceptional circumstances

;;                                  nth                                 get                         Vector as a function
;;----------------------------------------------------------------------------------------------------------------------
;; If the vector is nil             Returns nil                         Returns nil                 Throws an exception
;; If the index is out of range     Throws exception by default         Returns nil                 Throws an exception
;;                                  or returns a "not found" if
;;                                  supplied
;; Supports a "not found" arg       Yes                                 Yes                         No
;;                                  (nth [] 9 :whoops)                  (get [] 9 :whoops)

;; Because vectors are indexed, they can be efficiently walked in either direction, left to right or right to left.
;; The seq and rseq functions return sequences that do exactly that:
(seq a-to-j)
;;=> (\A \B \C \D \E \F \G \H \I \J)

(rseq a-to-j)
;;=> (\J \I \H \G \F \E \D \C \B \A)

;; Any item in a vector can be changed using the assoc function. Clojure does this in essentially constant time using
;; structural sharing between the old and new vectors as described at the beginning of this chapter:
(assoc a-to-j 4 "no longer E")
;;=> [\A \B \C \D "no longer E" \F \G \H \I \J]

;; The assoc function for vectors only works on indices that already exist in the vector or, as a special case, exactly
;; one step past the end. In this case, the returned vector is one item larger than the input vector. More frequently,
;; vectors are grown using the conj function, as you'll see in the next section.

;; A few higher-powered functions are provided that use assoc internally. For example, the replace function works on
;; both seqs and vectors, but when given a vector, it uses assoc to fix up and return a new vector:
(replace {2 :a 4 :b} [1 2 3 2 3 4])
;;=> [1 :a 3 :a 3 :b]

;; The functions assoc-in and update-in are for working with nested structures of vectors and/or maps, like this one:
(def matrix
  [[1 2 3]
   [4 5 6]
   [7 8 9]])

;; assoc-in, get-in, and update-in take a series of indices to pick items from each more deeply nested level. For a
;; vector arranged like the earlier matrix example, this amounts to row and column coordinates:
(get-in matrix [1 2])
;;=> 6

(assoc-in matrix [1 2] 'x)
;;=> [[1 2 3] [4 5 x] [7 8 9]]

;; The update-in function works the same way, but instead of taking a value to overwrite an existing value, it takes a
;; function to apply to an existing value. It replaces the value at the given coordinates with the return value of the
;; function given:
(update-in matrix [1 2] * 100)
;;=> [[1 2 3] [4 5 600] [7 8 9]]

;; The coordinates refer to the value 6, and the function given here is * taking an argument 100, so the slot becomes
;; the return value of (* 6 100). There's also a function get-in for retrieving a value in a nested vector. Before
;; exploring its operation, let's create a function neighbors in the following listing that, given a y-x location in an
;; square 2D matrix, returns a sequence of the locations surrounding it.
(defn neighbors
  ([size yx]
    (neighbors [[-1 0] [1 0] [0 -1] [0 1]]                  ; define neighbors to be 1 spot away, crosswise
               size
               yx))
  ([deltas size yx]
    (filter (fn [new-yx]                                    ; remove illegal coordinates
              (every? #(< -1 % size) new-yx))
            (map #(vec (map + yx %))                        ; blindly calculate possible coordinates
                 deltas))))

;; The operation of neighbors is fairly straightforward, but let's walk through it a little. The deltas local describes
;; that a neighbor can be one spot away, but only along the x or y axis (not diagonally). The function first walks
;; through deltas and builds a vector of each added to the yx point provided. This operation of course generates illegal
;; point coordinates, so those are removed using filter, which checks to ensure that the indices lie between -1 and the
;; provided size.

;; To show this in action, you can call neighbors, indicating a 3 x 3 matrix and asking for the neighbors of the
;; top-left cell:
(neighbors 3 [0 0])
;;=> ([1 0] [0 1])

;; The result indicates that the crosswise neighbors of cell y=0|x=0 are the cells y=1|x=0 and y=0|x=1. You can see how
;; this result is correct graphically:
;;
;; +------+------+------+
;; |      |******|      |
;; | 0,0  |******|      |
;; +------+------+------+
;; |******|      |      |
;; |******|      |      |
;; +------+------+------+
;; |      |      |      |
;; |      |      |      |
;; +------+------+------+

;; You can also call neighbors with the same-sized matrix but asking for the neighbors of the center cell:
(neighbors 3 [1 1])
;;=> ([0 1] [2 1] [1 0] [1 2])

;; The result indicates that the crosswise neighbors of the center cell as y=1|x=1 are the cells forming a plus, as
;; shown here:
;;
;; +------+------+------+
;; |      |******|      |
;; |      |******|      |
;; +------+------+------+
;; |******|      |******|
;; |******| 1,1  |******|
;; +------+------+------+
;; |      |******|      |
;; |      |******|      |
;; +------+------+------+

;; You can test the results of neighbors on cell 0,0 using get-in as follows:
(map #(get-in matrix %) (neighbors 3 [0 0]))
;;=> (4, 2)

;;  For each neighbor coordinate returned from neighbors, you use get-in to retrieve the value at that point. The
;; position [0 0] corresponding to the value 1 has the neighboring values 4 and 2. You'll use neighbors again later; but
;; next we'll look at growing and shrinking vectors -- treating them like stacks.

;; Vectors as stacks
;; -----------------
;; Classic stacks have at least two operations, push and pop. With respect to Clojure vectors, these operations are
;; called conj and pop, respectively. The conj function adds elements to, and pop removes elements from, the right side
;; of the stack. Because vectors are immutable, pop returns a new vector with the rightmost item dropped -- this is
;; different from many mutable stack APIs, which generally return the dropped item. Consequently, peek becomes more
;; important as the primary way to get an item from the top of the stack:
(def my-stack [1 2 3])

(peek my-stack)
;;=> 3

(pop my-stack)
;;=> [1 2]

(conj my-stack 4)
;;=> [1 2 3 4]

(+ (peek my-stack) (peek (pop my-stack)))
;;=> 5

;; Each of these operations completes in essentially constant time. Most of the time, a vector that's used as a stack is
;; used that way throughout its life. It's helpful to future readers of your code to keep this is mind and use the stack
;; operations consistently, even when other functions might work. For example, last on a vector returns the same thing
;; as peek; but besides being slower, it leads to unnecessary confusion about how the collection is being used. If the
;; algorithm involved calls for a stack, use conj, not assoc, for growing the vector; peek, not last; and pop, not
;; dissoc, for shrinking it.

;; Any object that implements clojure.lang.IPersistentStack can use the functions conj, pop, and peek. In addition to
;; vectors, Clojure lists also implement this interface, but the functions operate on the left side of lists instead of
;; the right side as with vectors. When operating on either via the stack discipline, it's best to ignore the ordering,
;; because it tends to add confusion.

;; Using vectors instead of reverse
;; --------------------------------
;; The ability of vectors to grow efficiently on the right side and then be walked right to left produces a noteworthy
;; emergent behavior: you rarely need the reverse function. This is different from most Lisps and schemes. When
;; processing a list, it's pretty common to want to produce a new list in the same order. But if all you have are
;; classic Lisp lists, often the most natural algorithm leaves you with a backward list that needs to be reversed.
;; Here's an example of a function similar to Clojure's map:
(defn strict-map1 [f coll]
  (loop [coll coll, acc nil]                                ; Accumulator starts as nil
    (if (empty? coll)
      (reverse acc)                                         ; Final result is reversed
      (recur (next coll)
             (cons (f (first coll)) acc)))))                ; Put the element onto the front of the accumulator

(strict-map1 - (range 5))
;;=> (0 -1 -2 -3 -4)

;; This is adequate code, except for that glaring reverse of the final return value. After the entire list has been
;; walked once to produce the desired values, reverse walks it again to get the values in the right order. This is both
;; inefficient and unnecessary. One way to get rid of the reverse is to use a vector instead of a list as the
;; accumulator:

(defn stric-map2 [f coll]
  (loop [coll coll acc []]                                  ; Accumulator now starts as a vector
    (if (empty? coll)
      acc                                                   ; Final result is unchanged
      (recur (next coll)
             (conj acc (f (first coll)))))))                ; Put the element onto the rear of the vector accumulator

(stric-map2 - (range 5))
;;=> [0 -1 -2 -3 -4]

;; A small change, but the code is now a touch cleaner and faster. It does return a vector instead of a list, but this
;; is rarely a problem, because any client code that wants to treat this as a seq can usually do so automatically.
;; The examples we've shown so far have all been plain vectors, but we'll turn now to the special features of some other
;; vector types, starting with subvectors.

;; Subvectors
;; ----------
;; Although items can't be removed efficiently from a vector (except the rightmost item), Clojure provide a fast way to
;; take a slice of an existing vector such as a-to-j (previously defined as [\A \B \C \D \E \F \G \H \I \J]) based on
;; start and end indices with the subvec function:
(subvec a-to-j 3 6)
;;=> [\D \E \F]

;; The first index given to subvec is inclusive (starts at index 3), but the second is exclusive (ends before index 6).
;; The new subvector internally hangs on to the entire original a-to-j vector, making each lookup performed on the new
;; vector cause the subvector to do a little offset math and then look it up in the original. This makes creating a
;; subvector fast. You can use subvec on any kind of vector, and it'll work fine. But there's special logic for taking
;; a subvec of a subvec, in which case the newest subvector keeps a reference to the original vector, not the
;; intermediate subvector. This prevents subvectors of subvectors from stacking up needlessly, and it keeps both the
;; creation and use of the sub-subvecs fast and efficient.
