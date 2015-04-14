;; Functions in all their forms
;; ---------------------------------------------------------------------------------------------------------------------
;; Earlier we mentioned that most of Clojure's composite types can be used as functions of their elements. As a
;; refresher, recall that vectors are functions of their indices, so executing ([:a :b] 0) returns :a. But this can be
;; used to greater effect by passing the vector as a function argument:
(map [:chthon :phthor :beowulf :grendel] #{0 3})            ; Call the vector as a function, passing numbers as args
;;=> (:chthon :grendel)                                     ; Collect the results in a seq

;; The example uses the vector as the function to map over a set of indices, indicating its first and fourth elements by
;; index. Clojure collections offer an interesting juxtaposition, in that not only can Clojure collections act as
;; functions, but Clojure functions can also act as data -- an idea known as first-class functions.

;; First-class functions
;; ---------------------
;; In a programming language such as Java, there's no notion of a standalone function. Instead, every problem solvable
;; by Java must be performed with the fundamental philosophy that everything is an object. This view on writing programs
;; is therefore rooted in the idea that behaviors in a program must be either modeled as class instances or attached to
;; them -- wise or not. Clojure, on the other hand, is a functional programming language and views the problem of
;; software development as the application of functions to data. Likewise, functions in Clojure enjoy equal standing
;; with data -- functions are first-class citizens. Before we start, we should define what makes something first class:
;;
;; * It can be created on demand.
;; * It can be stored in a data structure.
;; * It can be passed as an argument to a function.
;; * It can be returned as the value of a function.
;;
;; Those of you coming from a background in Java might find the idea of creating functions on demand analogous to the
;; practice of creating anonymous inner classes to handle Swing events (to name only one use case). Although similar
;; enough to start on the way toward understanding functional programming, it's not a concept likely to bear fruit, so
;; don't draw conclusions from this analogy.

;; Creating functions on demand using composition
;; ----------------------------------------------
;; Even a cursory glance at Clojure is enough to confirm that its primary unit of computation is the function, be it
;; created or composed of other functions:
(def fifth (comp first rest rest rest rest))
(fifth [1 2 3 4 5])
;;=> 5

;; The function fifth isn't defined with fn or defn forms shown before, but instead built from existing parts using the
;; comp (compose) function. But it may be more interesting to take the idea one step further by instead proving a way to
;; build arbitrary nth functions as shown here:
(defn fnth [n]
  (apply comp
         (cons first
               (take (dec n) (repeat rest)))))

((fnth 5) '[a b c d e])
;;=> e

;; The function fnth builds a list of the function rest of the appropriate length with a final first consed onto the
;; front. This list is then fed into the comp function via apply, which takes a function and a sequence of things and
;; effectively calls the function with the list elements as its arguments. At this point, there's no longer any doubt
;; that the function fnth builds new functions on the fly based on its arguments. Creating new functions this way is a
;; powerful technique, but it takes some practice to think in a compositional way. It's relatively rare to see more than
;; one open parenthesis in a row like this in Clojure, but when you see it, it's almost always because a function (such
;; as fnth) is creating and returning a function that's called immediately. A general rule of thumb is that if you need
;; a function that applies a number of functions serially to the return of the former, then composition is a good fit:
(map (comp                                                  ; Compose fn
       keyword                                              ; . . . make a keyword
       #(.toLowerCase %)                                    ; . . . from a lowercase
       name)                                                ; . . . string name
     '(A B C))                                              ; Mapped over a list of symbols
;;=> (:a :b :c)

;; Splitting functions into smaller, well-defined pieces fosters composability and, as a result, reuse.

;; Creating functions on demand using partial functions
;; ----------------------------------------------------
;; There may be times when instead of building a new function from chains of other functions as comp allows, you need to
;; build a function from the partial application of another:
((partial + 5) 100 200)
;;=> 305

;; The function partial builds a new function that partially applies the single argument 5 to the addition function.
;; When the returned partial function is passed the arguments 100 and 200, the result is their summation plus that of
;; the value 5 captured by partial.

;; Note: The use of partial differs from the notion of currying in a fundamental way. A function built with partial
;; attempts to evaluate whenever it's given another argument. A curried function returns another curried function until
;; it receives a predetermined number of arguments -- only then does it evaluate. Because Clojure allows functions with
;; a variable number of arguments, currying makes little sense.

;; We'll discuss more about using partial later in this section, but as a final point observe that
;; ((partial + 5) 100 200) is equivalent to (#(apply + 5 %&) 100 200).
(#(apply + 5 %&) 100 200)
;;=> 305

;; Reversing truth with complement
;; -------------------------------
;; One final function builder is the complement function. This function takes a function that returns a truthy value and
;; returns the opposite truthy value:
(let [truthiness (fn [v] v)]
  [((complement truthiness) true)
   ((complement truthiness) 42)
   ((complement truthiness) false)
   ((complement truthiness) nil)])
;;=> [false false true true]

((complement even?) 2)
;;=> false

;; Note that (complement even?) is equivalent to (comp not even?) or #(not (even? %)).

;; Using functions as data
;; -----------------------
;; First-class functions can not only be treated as data; they are data. Because a function is first class, it can be
;; stored in a container expecting a piece of data, be it a local, a reference, collections, or anything able to store a
;; java.lang.Object. This is a significant departure from Java, where methods are part of a class but don't stand alone
;; at runtime. One particularly useful method for treating functions as data is the way that Clojure's testing framework
;; clojure.test stores and validates unit tests in the metadata of a var holding a function. These unit tests are keyed
;; with the :test keyword, laid out as follows:
(defn join
  {:test (fn []
           (assert
             (= (join "," [1 2 3]) "1,3,3")))}
  [sep s]
  (apply str (interpose sep s)))

;; You've modified join by attaching some metadata containing a faulty unit test. Of course, by that we mean the
;; attached unit test is meant to fail in this case. The clojure.test/run-tests function is useful for running attached
;; unit tests in the current namespace:
(use '[clojure.test :as t])
(t/run-tests)
;; Testing user
;;
;; ERROR in (join) (functions_in_all_their_forms.clj:115)
;; Uncaught exception, not in assertion.
;; expected: nil
;; actual: java.lang.AssertionError: Assert failed: (= (join "," [1 2 3]) "1,3,3")

;; As expected, the faulty unit test for join fails. Unit tests in Clojure only scratch the surface of the boundless
;; spectrum of examples using functions as data, but for now they'll do.

;; The faces of defn metadata
;; --------------------------
;; As shown in the definition of the join function with built-in tests, placing a map before a function's parameters is
;; one way of assigning metadata to a function using the defn macro. Another way is to use the shorthand notation before
;; the function name, like so:
(defn ^:private ^:dynamic sum [nums]
  (map + nums))
;; The use of the shorthand ^:private and ^:dynamic is the same as saying
(defn ^{:private true, :dynamic true} sum [nums]
  (map + nums))
;; which is the same as saying
(defn sum {:private true, :dynamic true} [nums]
  (map + nums))
;; which is also the same as saying
(defn sum
  ([nums]
   (map + nums))
  {:private true, :dynamic true})
;; The differing choices come in handy usually in different macro metaprogramming scenarios. For most human-typed
;; functions, the shorthand form works fine.

;; Higher-order functions
;; ----------------------
;; A higher-order function is a function that does at least one of the following:
;; * Takes one or more functions as arguments
;; * Returns a function as a result
;; A Java programmer may be familiar with the practices of subscriber patterns or schemes using more general-purpose
;; callback objects. There are scenarios such as these where Java treats objects like functions, but as with anything in
;; Java, you're really dealing with objects containing privileged methods.

;; Functions as arguments
;; ----------------------
;; In this book, we use and advocate the use of the sequence functions map, reduce, and filter -- all of which expect a
;; function argument that's applied to the elements of the sequence arguments. The use of functions in this way is
;; ubiquitous in Clojure and can make for truly elegant solutions. Another interesting example of a function that takes
;; a function as an argument is the sort-by function. Before we dig into that, allow us to explain the motivation behind
;; it. Specifically, Clojure provides a function named sort that works exactly as you might expect:
(sort [1 5 7 0 -42 13])
;;=> (-42 0 1 5 7 13)

;; The sort function works on many different types of elements:
(sort ["z" "x" "a" "aa"])
;;=> ("a" "aa" "x" "z")

(sort [(java.util.Date.) (java.util.Date. 100)])
;;=> (#inst "1970-01-01T00:00:00.100-00:00" #inst "2015-03-29T00:06:02.955-00:00")

(sort [[1 2 3], [-1, 0, 1], [3 2 1]])
;;=> ([-1 0 1] [1 2 3] [3 2 1])

;; But if you want to sort by different criteria, such as from greatest to least, then you can pass a function in to
;; sort for use as the sorting criteria:
(sort > [7 1 4])
;;=> (7 4 1)

;; But sort fails if given seqs containing elements that aren't mutually comparable:
(sort ["z" "x" "a" "aa" 1 5 8])
;;=> java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Number

(sort [{:age 99}, {:age 13}, {:age 7}])
;;=> java.lang.ClassCastException: clojure.lang.PersistentArrayMap cannot be cast to java.lang.Comparable

;; Likewise, it gives unwanted results if you try to compare sub-elements of a type that it sorts as an aggregate rather
;; than by specific parts:
(sort [[:a 7], [:c 13], [:b 21]])
;;=> ([:a 7] [:b 21] [:c 13])

;; That is, in this example you want to sort by the second element in the vectors, but passing the second function into
;; sort is not the solution:
(sort second [[:a 7], [:c 13], [:b 21]])
;;=> clojure.lang.ArityException: Wrong number of args (2) passed to: core/second

;; Instead, Clojure provides the sort-by function, which takes a function as an argument that is used to preprocess each
;; sortable element into something that is mutually comparable to the others:
(sort-by second [[:a 7], [:c 13], [:b 21]])
;;=> ([:a 7] [:c 13] [:b 21])

;; That looks better. And likewise for the other failed examples shown earlier, sort-by is key:
(sort-by str ["z" "x" "a" "aa" 1 5 8])
;;=> (1 5 8 "a" "aa" "x" "z")

(sort-by :age [{:age 99}, {:age 13}, {:age 7}])
;;=> ({:age 7} {:age 13} {:age 99})

;; The fact that sort-by takes an arbitrary function (and function-like thing, as shown when you used the keyword
;; earlier) to preprocess elements allows for powerful sorting techniques. For example, let's look at an example of a
;; function that takes a sequence of maps and a function working on each, and returns a sequence sorted by the results
;; of the function. The implementation in Clojure is straightforward and clean:
(def plays [{:band "Burial",     :plays 979,  :loved 9}
            {:band "Eno",        :plays 2333, :loved 15}
            {:band "Bill Evans", :plays 979,  :loved 9}
            {:band "Magma",      :plays 2665, :loved 31}])

(def sort-by-loved-ratio (partial sort-by #(/ (:plays %) (:loved %))))

;; The function with the overly descriptive name sort-by-loved-ratio is built from the partial application of the
;; function sort-by and an anonymous function dividing the :plays field by the :loved field. This is a simple solution
;; to the problem presented, and its usage is equally so:
(sort-by-loved-ratio plays)
;;=> ({:plays 2665, :band "Magma",      :loved 31}
;;    {:plays 979,  :band "Burial",     :loved 9}
;;    {:plays 979,  :band "Bill Evans", :loved 9}
;;    {:plays 2333, :band "Eno",        :loved 15})

;; This example intentionally uses the additional higher-order function sort-by to avoid reimplementing core functions
;; and instead build the program from existing parts. You should strive to do this whenever possible.

;; Functions as return values
;; --------------------------
;; You've already used functions returning functions in this chapter with comp, partial, and complement, but you can
;; build functions that do the same. Let's extend the earlier example to provide a function that sorts rows based on
;; some number of column values. This is similar to the way spreadsheets operate, in that you can sort on a primary
;; column while falling back on a secondary column to provide the sort order on matching results in the primary. This
;; behavior is typically performed along any number of columns, cascading down from the primary column to the last; each
;; subgroup is sorted appropriately, as the expected result illustrates:
;; (sort-by (columns [:plays :loved :band]) plays)

;; This kind of behavior sounds complex on the surface but is shockingly simple in its Clojure implementation:
(defn columns [column-names]
  (fn [row]
    (vec (map row column-names))))

(sort-by (columns [:plays :loved :band]) plays)
;;=> ({:plays 979,  :band "Bill Evans", :loved 9}
;;    {:plays 979,  :band "Burial",     :loved 9}
;;    {:plays 2333, :band "Eno",        :loved 15}
;;    {:plays 2665, :band "Magma",      :loved 31})

;; A quick example of what columns provides will help to clarify its intent:
(columns [:plays :loved :band])
;;=> #<user$columns$fn__2203 user$columns$fn__2203@3894ef06>

((columns [:plays :loved :band])
  {:band "Burial", :plays 979, :loved 9})
;;=> [979 9 "Burial"]

;; Running the preceding expression shows that the row for Burial has a tertiary column sorting, represented as a vector
;; of three elements that correspond to the listed column names. Specifically, the function columns returns another
;; function expecting a map. This return function is then supplied to sort-by to provide the value on which the plays
;; vector would be sorted. Perhaps you see a familiar pattern: you apply the column-names vector as a function across a
;; set of indices, building a sequence of its elements at those indices. This action returns a sequence of the values of
;; that row for the supplied column names, which is then turned into a vector so that it can be used as the sorting
;; function, as structured here:
(vec (map (plays 0) [:plays :loved :band]))
;;=> [979 9 "Burial"]

;; This resulting vector is then used by sort-by to provide the final ordering. Building your programs using first-class
;; functions in concert with higher-order functions reduces complexities and makes your code base more robust and
;; extensible. Next, we'll explore pure functions, which all prior functions in this section have been, and explain why
;; your applications should strive toward purity.

;; Prefer higher-order functions when processing sequences
;; -------------------------------------------------------
;; We earlier that one way to ensure that lazy sequences are never fully realized in memory is to prefer higher-order
;; functions for processing. Most collection processing can be performed with some combination of the following
;; functions: map, reduce, filter, for, some, repeatedly, sort-by, keep, take-while, and drop-while. But higher-order
;; functions aren't a panacea for every solution. Therefore, we'll cover the topic of recursive solutions in greater
;; depth later on for those cases when higher-order functions fail or are less than clear.

;; Pure functions
;; --------------
;; Pure functions are regular functions that, through convention, conform to the following simple guidelines:
;; * The function always returns the same result, given the same arguments.
;; * The function doesn't cause any observable side effects.

;; Although Clojure is designed to minimize and isolate side effects, it's by no means a purely functional language.
;; But there are a number of reasons why you'd want to build as much of your system as possible from pure functions, and
;; we'll enumerate a few presently.

;; Referential transparency
;; ------------------------
;; If a function of some arguments always results in the same value and changes no other values in the greater system,
;; then it's essentially a constant, or referentially transparent (the reference to the function is transparent to
;; time). Take a look at pure function keys-apply:
(defn keys-apply [f ks m]
  (let [only (select-keys m ks)]                            ; Get exact entries
    (zipmap (keys only)                                     ; Zip the keys and processed values back into a map
            (map f (vals only)))))

;; The action of keys-apply is as follows (recall that plays is a sequence of maps, defined in the previous section):
(keys-apply #(.toUpperCase %) #{:band} (plays 0))
;;=> {:band "BURIAL"}

;; Using another pure function manip-map, you can then manipulate a set of keys based on a given function:
(defn manip-map [f ks m]                                    ; Manipulates only the given keys, and then merges the
  (merge m (keys-apply f ks m)))                            ; changes into the original

;; And the use of manip-map to halve the values keyed at :plays and :loved is as follows:
(manip-map #(int (/ % 2)) #{:plays :loved} (plays 0))
;;=> {:plays 489, :band "Burial", :loved 4}

;; keys-apply and manip-map are both pure functions, illustrated by the fact that you can replace them in the context of
;; a larger program with their expected return values and not change the outcome. Pure functions exist outside the
;; bounds of time. But if you make either keys-apply or manip-map reliant on anything but its arguments or generate a
;; side effect within, then referential transparency dissolves. Let's add one more function to illustrate this:
(defn mega-love! [ks]
  (map (partial manip-map #(int (* % 1000)) ks) plays))

(mega-love! [:loved])
;;=> ({:plays 979,  :band "Burial",     :loved 9000}
;;    {:plays 2333, :band "Eno",        :loved 15000}
;;    {:plays 979,  :band "Bill Evans", :loved 9000}
;;    {:plays 2665, :band "Magma",      :loved 31000})

;; The function mega-love! works against the global var plays and is no longer limited to generating results solely from
;; its arguments. Because plays could change at any moment, there's no guarantee that mega-love! would return the same
;; value given any particular argument.

;; Optimization
;; If a function is referentially transparent, then it can more easily be optimized using techniques such as memoization
;; and algebraic manipulations.

;; Testability
;; If a function is referentially transparent, then it's easier to reason about and therefore more straightforward to
;; test. Building mega-love! as an impure function forces the need to test against the possibility that plays could
;; change at any time, complicating matters substantially. Imagine the confusion should you add further impure functions
;; based on further external transient values.

;; Named Arguments
;; ---------------
;; Some programming languages allow functions to take named arguments. Python is one such language, as shown here:
;; def slope(p1=(0,0), p2=(1,1)):
;;     return (float(p2[1] - p1[1])) / (p2[0] - p1[0])
;; slope((4,15), (3,21))
;; #=> -6.0
;; slope(p2=(2,1))
;; #=> 0.5
;; slope()
;; #=> 1.0

;; The Python function slope calculates the slope of a line given two tuples defining points on a line. The tuples p1
;; and p2 are defined as named parameters, allowing either or both to be omitted in favor of default values or passed in
;; any order as named parameters. Clojure provides a similar feature, using its destructuring mechanism coupled with the
;; optional arguments flag &. The same function can be written using Clojure's named arguments as follows:
(defn slope
  [& {:keys [p1 p2] :or {p1 [0 0] p2 [1 1]}}]
  (float (/ (- (p2 1) (p1 1))
            (- (p2 0) (p1 0)))))

(slope :p1 [4 15] :p2 [3 21])
;;=> -6.0

(slope :p2 [2 1])
;;=> 0.5

(slope)
;;=> 1.0

;; Clojure's named arguments are built on the destructuring mechanism outlined before, allowing much richer ways to
;; declare them.
