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
