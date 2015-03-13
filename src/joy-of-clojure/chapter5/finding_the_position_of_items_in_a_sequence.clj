;; Putting it all together: finding the position of items in a sequence
;; ---------------------------------------------------------------------------------------------------------------------
;; The case study for this chapter is to design and implement a simple function to locate the positional index of an
;; element in a sequence. We're going to pool together much of the knowledge gained in this chapter in order to
;; illustrate the steps you might take in designing, writing, and ultimately optimizing a Clojure collection function.
;; Of course, we'll work against the sequence abstraction and therefore design the solution accordingly.
;; The function, named pos, must
;; * Work on any collection type, returning indices corresponding to some value
;; * Return a numerical index for sequential collections or associated key for maps and sets
;; * Otherwise return nil
;; Now that we've outlined the basic requirements of pos, we'll run through a few implementations, discussing each along
;; the way toward a Clojure-optimal version.

;; Implementation
;; --------------
;; If we were to address each of the requirements for pos literally and directly, we might come up with a function that
;; looks like the following listing.
;; First cut of the position function
(defn pos [e coll]
  (let [cmp (if (map? coll)
              #(= (second %1) %2)                           ; Map compare
              #(= %1 %2))]                                  ; Default compare
    (loop [s coll idx 0]                                    ; Start at the beginning
      (when (seq s)
        (if (cmp (first s) e)                               ; Compare
          (if (map? coll)
            (first (first s))                               ; Map returns key
            idx)                                            ; ... Else index
          (recur (next s) (inc idx)))))))

;; Pretty hideous, right? We think so too, but let's at least check whether it works as desired:
(pos 3 [:a 1 :b 2 :c 3 :d 4])
;;=> 5

(pos :foo [:a 1 :b 2 :c 3 :d 4])
;;=> nil

(pos 3 {:a 1 :b 2 :c 3 :d 4})
;;=> :c

(pos \3 ":a 1 :b 2 :c 3 :d 4")
;;=> 13

;; Apart from being overly complicated, it'd likely be more useful if it instead returned a sequence of all the indices
;; matching the item, so let's add that to the requirements. But we've built a heavy load with the first cut at pos and
;; should probably step back a moment to reflect. First, it's probably the wrong approach to handle map types and other
;; sequence types differently. The use of the predicate map? to detect the type of the passed collection is incredibly
;; constraining, in that it forces different collections to be processed differently, and sets are not handled correctly
;; at all. That's not to say the use of type-based predicates is strictly prohibited, only that you should try to favor
;; more generic algorithms or at least minimize their usage.

;; As chance has it, the exact nature of the problem demands that we view collections as a set of values paired with a
;; given index, be it explicit in the case of maps or implicit in the case of other sequences' positional information.
;; Therefore, imagine how easy this problem would be if all collections were laid out as sequences of pairs
;; ([index1 value1] [index2 value2] ... [indexn valuen]). Well, there's no reason why they can't be, as shown next:

(defn index [coll]
  (cond
    (map? coll) (seq coll)
    (set? coll) (map vector coll coll)
    :else (map vector (iterate inc 0) coll)))

;; This simple function can generate a uniform representation for indexed collections:
(index [:a 1 :b 2 :c 3 :d 4])
;;=> ([0 :a] [1 1] [2 :b] [3 2] [4 :c] [5 3] [6 :d] [7 4])

(index {:a 1 :b 2 :c 3 :d 4})
;;=> ([:a 1] [:b 2] [:c 3] [:d 4])

(index #{:a 1 :b 2 :c 3 :d 4})
;;=> ([1 1] [2 2] [3 3] [4 4] [:a :a] [:c :c] [:b :b] [:d :d])

;; As shown, we're still using type-based predicates, but we've raised the level of abstraction to the equality
;; partitions in order to build contextually relevant indices. Now, the function for finding the positional indices for
;; the desired value is trivial:

(defn pos [e coll]
  (for [[i v] (index coll) :when (= e v)] i))

(pos 3 [:a 1 :b 2 :c 3 :d 4])
;;=> (5)

(pos 3 {:a 1, :b 2, :c 3, :d 4})
;;=> (:c)

(pos 3 [:a 3 :b 3 :c 3 :d 4])
;;=> (1 3 5)

(pos 3 {:a 3, :b 3, :c 3, :d 4})
;;=> (:a :c :b)

;; Much better! But there's one more deficiency with the pos function from a Clojure perspective. Typically in Clojure
;; it's more useful to pass a predicate function in cases such as these, so that instead of pos determining raw
;; equality, it can build its result along any dimension. We can modify pos only slightly to achive the ideal level of
;; flexibility:

(defn pos [pred coll]
  (for [[i v] (index coll) :when (pred v)] i))

(pos #{3 4} {:a 1 :b 2 :c 3 :d 4})
;;=> (:c :d)

(pos even? [2 3 6 7])
;;=> (0 2)

;; We've vastly simplified the original solution and generated two potentially useful functions in the process. By
;; following some simple Clojure principles, we solved the original problem statement in a concise and elegant manner.
