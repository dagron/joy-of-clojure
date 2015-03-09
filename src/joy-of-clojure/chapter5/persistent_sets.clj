;; Persistent sets
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure sets work the same as mathematical sets, in that they're collections of unsorted unique elements. In this
;; section, we'll cover sets by explaining their strong points, weaknesses, and idioms. We'll also cover some of the
;; functions from the clojure.set namespace.

;; Basic properties of Clojure sets
;; --------------------------------
;; Sets are functions of their elements that return the matched element or nil:
(#{:a :b :c :d} :c)
;;=> :c

(#{:a :b :c :d} :e)
;;=> nil

;; Set elements can be accessed via the get function, which returns the queried value if it exists in the given set:
(get #{:a 1 :b 2} :b)
;;=> :b

(get #{:a 1 :b 2} :z :nothing-doing)
;;=> :nothing-doing

;; An added advantage of using get is shown here in the second call: get allows a third argument that is used as the
;; "not found" value, should a key lookup fail. As a final point, sets, like all Clojure's collections, support
;; heterogeneous values.

;; How Clojure populates sets
;; --------------------------
;; The key to understanding how Clojure sets determine which elements are discrete lies in one simple statement. Given
;; two elements evaluating as equal, a set will contain only one, independent of concrete types:
(into #{[]} [()])
;;=> #{[]}

(into #{[1 2]} '[(1 2)])
;;=> #{[1 2]}

(into #{[] #{} {}} [()])
;;=> #{[] #{} {}}

;; From the first two examples, even though [] and () are of differing types, they're considered equal because their
;; elements are equal or (in this case) empty. But the last example illustrates nicely that collections in an equality
;; partition will always be equal if their elements are equal, but will never be considered equal if the collections are
;; of types in different equality partitions.

;; Finding items in a sequence using a set and the some function
;; -------------------------------------------------------------
;; Trying to find a value in a vector using the contains? function doesn't work the way we'd hope. Instead, the some
;; function takes a predicate and a sequence. It applies the predicate to each element in turn, returning the first
;; truthy value returned by the predicate or else nil:
(some #{:b} [:a 1 :b 2])
;;=> :b

(some #{1 :b} [:a 1 :b 2])
;;=> 1

;; Using a set as the predicate supplied to some allows you to check whether any of the truthy values in the set are
;; contained within the given sequence. This is a frequently used Clojure idiom for searching for containment within
;; a sequence.

;; Keeping your sets in order with sorted-set
;; ------------------------------------------
;; There's not much to say about creating sorted sets with the sorted-set function. But there's a simple rule you should
;; bear in mind:
(sorted-set :b :c :a)
;;=> #{:a :b :c}

(sorted-set [3 4] [1 2])
;;=> #{[1 2] [3 4]}

(sorted-set :b 2 :c :a 3 1)
; CompilerException java.lang.RuntimeException: Unable to resolve symbol: b in this context

;; As long as the arguments to the sorted-set function are mutually comparable, you'll receive a sorted set; otherwise
;; an exception is thrown. This can manifest itself when you're dealing with sorted sets downstream from their point of
;; creation, leading to potential confusion:
(def my-set (sorted-set :a :b))

;; ... some time later
(conj my-set "a")
; CompilerException java.lang.ClassCastException: clojure.lang.Keyword cannot be cast to java.lang.String

;; The difficulty in finding the reason for this exception will increase as the distance between the creation of my-set
;; and the call to conj increases. You can adjust this rule a bit by using sorted-set-by instead and providing your own
;; comparator. This works exactly like the comparator for sorted-map-by, which we'll cover later on. Sorted maps and
;; sorted sets are also similar in their support of subseq, to allow efficiently jumping to a particular key in the
;; collection and walking through it from there.

;; The contains? function
;; ----------------------
;; As we touched on earleir, there's sometimes confusion regarding the usage of Clojure's contains? function. Many
;; newcomers to Clojure expect this function to work the same as Java's java.util.Collection#contains method; this
;; assumption is false, as shown:
(contains? #{1 2 3 4} 4)
;;=> true

(contains? [1 2 3 4] 4)
;;=> false

;; If you were to draw a false analogy between Java's .contains methods and contains?, then both of the function calls
;; noted here should return true. The official documentation for contains? describes it as a function that returns true
;; if a given key exists in a collection. When reading the word key, the notion of a map springs to mind, but the fact
;; that this function also works on sets hints at their implementation details. Sets are implemented as maps with the
;; same element as the key and value, but there's an additional check for containment before insertion.

;; The clojure.set namespace
;; -------------------------
;; Mathematical sets form the basis of much of modern mathematical thought, and Clojure's basic set functions in the
;; clojure.set namespace are a clear reflection of the classical set operations. In this subsection, we'll briefly cover
;; each function and talk about how, when applicable, they differ from the mathematical model. To start using the
;; functions in the clojure.set namespace, enter the following into your REPL:
(require 'clojure.set)

;; Or, if you wish to use these functions in your namespace, use the following inclusion:
(ns your.namespace.here
  (:require clojure.set))

;; Note that Clojure's set functions take an arbitrary number of sets and apply the operation incrementally.

;; The intersection function
;; -------------------------
;; Clojure's clojure.set/intersection function works as you might expect. Given two sets, intersection returns a set of
;; the common elements. Given n sets, it incrementally returns the intersection of resulting sets and the next set, as
;; shown in the following code:
(clojure.set/intersection #{:humans :fruit-bats :zombies}
                          #{:chupacabra :zombies :humans})
;;=> #{:zombies :humans}

(clojure.set/intersection #{:pez :gum :dots :skor}
                          #{:pez :skor :pocky}
                          #{:pocky :gum :skor})
;;=> #{:skor}

;; In the first example, the resulting set is the common elements between the given sets. The second example is the
;; result of the intersection of the first two sets then intersected with the final set.

;; The union function
;; ------------------
;; There's also likely no surprise when using the clojure.set/union function:
(clojure.set/union #{:humans :fruit-bats :zombies}
                   #{:chupacabra :zombies :humans})
;;=> #{:chupacabra :zombies :humans :fruit-bats}

(clojure.set/union #{:pez :gum :dots :skor}
                   #{:pez :skor :pocky}
                   #{:pocky :gum :skor})
;;=> #{:pocky #:pez :skor :dots :gum}

;; Given two sets, the resulting set contains all the distinct elements from both. In the first example, this means
;; :zombies and :humans show up only once each in the return value. Note in the second example that more than two sets
;; may be given to union, but as expected each value given in any of the input sets is included exactly once in the
;; output set.

;; The difference function
;; -----------------------
;; The only set function that could potentially cause confusion on first glance is clojure.set/difference, which by name
;; implies some sort of opposition to a union operation. Working under this false assumption, you might assume that
;; difference would operate thusly:

;; This is not what really happens
(clojure.set/difference #{1 2 3 4} #{3 4 5 6})
;;=> #{1 2 5 6}

;; But if you were to evaluate this expression in your REPL, you'd receive a very different result:
(clojure.set/difference #{1 2 3 4} #{3 4 5 6})
;;=> #{1 2}

;; The reason for this result is that Clojure's difference function calculates what's known as a relative complement
;; between two sets. In other words, difference can be viewed as a set-subtraction function, "removing" all elements in
;; a set A that are also in another set B.
