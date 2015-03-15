;; Structural sharing: a persistent toy
;; ---------------------------------------------------------------------------------------------------------------------
;; We won't go into terrible detail about the internals of Clojure's persistent data structures -- we'll leave that to
;; others. But we do want to explore the notion of structural sharing. Our example will be highly simplified compare to
;; Clojure's implementations, but it should help clarify some of the techniques used.

;; The simplest shared-structure type is the list. Two different items can be added to the front of the same list,
;; producing two new lists that share their next parts. Let's try this by creating a base list and then two new lists
;; from that same base:
(def baselist (list :barnabas :adam))
(def list1 (cons :willie baselist))
(def list2 (cons :phoenix baselist))

list1
;;=> (:willie :barnabas :adam)

list2
;;=> (:phoenix :barnabas :adam)

;; You can think of baselist as a historical version of both list1 and list2. But it's also the shared part of both
;; lists. More than being equal, the next parts of both lists are identical -- the same instance:
(= (next list1) (next list2))                               ; Not only are the nexts equal...
;;=> true

(identical? (next list1) (next list2))                      ; ...but they're the same exact object
;;=> true

;; That's not too complicated, right? But the features supported by lists are also limited. Clojure's vectors and maps
;; also provide structural sharing, while allowing you to change values anywhere in the collection, not just on one end.
;; The key is the structure each of these datatypes uses internally. You'll now build a simple tree to help demonstrate
;; how a tree can allow interior changes and maintain shared structure at the same time.

;; Each node of your tree will have three fields: a value, a left branch, and a right branch. You'll put them in a map,
;; like this:

{:val 5, :L nil, :R nil}

;; That's the simplest possible tree -- a single node holding the value 5, with empty left and right branches. This is
;; exactly the kind of tree you want to return when a single item is added to an empty tree. To represent an empty tree,
;; you'll use nil. With the structure decision made, you can write your own conj function, xconj, to build up your tree,
;; starting with the code for this initial case:
(defn xconj [t v]                                           ; Start with a tree, t, and a value to add, v
  (cond
    (nil? t) {:val v, :L nil, :R nil}))

(xconj nil 5)
;;=> {:val 5, :L nil, :R nil}

;; Hey, it works! Not too impressive yet, though, so you need to handle the case where an item is being added to a
;; nonempty tree. Keep the tree in order by putting values less than a node's :val in the left branch, and other values
;; in the right branch.  That means you need a test like this:
(< v (:val t))

;; When that's true, you need the new value v to go into the left branch, (:L t). If this were a mutable tree, you'd
;; change the value of :L to be the new node. Instead, you should build a new node, copying in the parts of the old node
;; that don't need to change. Something like this:
{:val (:val t),
 :L (insert-new-val-here),
 :R (:R t)}

;; This map will be the new root node. Now you need to figure out what to put for insert-new-val-here. If the old value
;; of :L is nil, you need a new single-node tree — you even have code for that already, so you could use (xconj nil v).
;; But what if :L isn't nil? In that case, you want to insert v in its proper place in whatever tree :L is pointing to—
;; so (:L t) instead of nil:
(defn xconj [t v]
  (cond
    (nil? t)       {:val v, :L nil, :R nil}
    (< v (:val t)) {:val (:val t), :L (xconj (:L t) v), :R (:R t)}))

(def tree1 (xconj nil 5))
tree1
;;=> {:val 5, :L nil, :R nil}

(def tree1 (xconj tree1 3))
tree1
;;=> {:val 5, :L {:val 3, :L nil, :R nil}, :R nil}

(def tree1 (xconj tree1 2))
tree1
;;=> {:val 5, :L {:val 3, :L {:val 2, :L nil, :R nil}, :R nil}, :R nil}

;; There, it's working. At least it seems to be -- there's a lot of noise in that output, making it difficult to read.
;; Here's a function to traverse the tree in sorted order, converting it to a seq that will print more succinctly:
(defn xseq [t]
  (when t
    (concat (xseq (:L t)) [(:val t)] (xseq (:R t)))))

(xseq tree1)
;;=> (2 3 5)

;; Now you need a final condition for handling the insertion of values that are not less than the node value:
(defn xconj [t v]
  (cond
    (nil? t)       {:val v, :L nil, :R nil}                         ; Nil nodes start with v
    (< v (:val t)) {:val (:val t),                                  ; When v is less than the value at the current node,
                    :L (xconj (:L t) v),                            ; it's pushed left
                    :R (:R t)}
    :else          {:val (:val t),                                  ; Otherwise it's pushed right
                    :L (:L t)
                    :R (xconj (:R t) v)}))

;; Keywords as functions for true-power elegance
;; ---------------------------------------------
;; A point of deep significance in understanding the fragment using :val and :R in the function position is how Clojure
;; uses keywords as functions. Earlier, we said that keywords, when placed in a function call position, work as
;; functions taking a map that then look up themselves (as keywords) in said map. Therefore, the snippet (:val t) states
;; that the keyword :val takes the map t and looks itself up in the map. This is functionally equivalent to
;; (get t :val). Although we prefer the keyword-as-function approach used in xconj, you'll sometimes be faced with a
;; decision and may instead choose to use get. Either choice is fine, and your decision is stylistic. A nice rule of
;; thumb to follow is that if a keyword is stored in a local or var, using get is often clearer in its "lookup" intent:
(let [some-local :a-key]
  (get {:a-key 42} :a-key))
;;=> 42

;; That is, the preceding is more clear than (some-local a-map).

;; Now that you have the thing built, we hope you understand well enough how it's put together that this demonstration
;; of the shared structure is unsurprising:
(def tree2 (xconj tree1 7))
(xseq tree2)
;;=> (2 3 5 7)

(identical? (:L tree1) (:L tree2))
;;=> true

;; Both tree1 and tree2 share a common structure, which is more easily visualized in this figure below:

;; Shared Structure Tree
;; ---------------------
;; No matter how big the left side of a tree's root node is, something can be inserted on the right side without
;; copying, changing, or even examining the left side. All those values will be included in the new tree,
;; along with the inserted value.
;;
;;                    +------------------+          +------------------+
;;                    |      tree1       |          |      tree2       |
;;                    |------------------|          |------------------|
;;                    | L      5       R |          | L      5       R |
;;                    | *             nil|          | *              * |
;;                    +------------------+          +------------------+
;;                      |                             |              |
;;                     \|/                            |             \|/
;;               +------------------+                 |  +------------------+
;;               |                  | <---------------+  |                  |
;;               |------------------|                    |------------------|
;;               | L      3       R |                    | L      7       R |
;;               | *             nil|                    |nil            nil|
;;               +------------------+                    +------------------+
;;                 |
;;                \|/
;;          +------------------+
;;          |                  |
;;          |------------------+
;;          | L      2       R |
;;          |nil            nil|
;;          +------------------+

;; This example demonstrates several features that it has in common with all of Clojure's persistent collections:
;; * Every "change" creates at least a new root node, plus new nodes as needed in the path through the tree to where the
;;   new value is being inserted.
;; * Values and unchanged branches are never copied, but references to them are copied from nodes in the old tree to
;;   nodes in the new one.
;; * This implementation is completely thread-safe in a way that's easy to check -- no object that existed before a call
;;   to xconj is changed in any way, and newly created nodes are in their final state before being returned. There's no
;;   way for any other thread, or even any other functions in the same thread, to see anything in an inconsistent state.

;; The example fails, though, when compared to Clojure's production-quality code:
;; * It's a binary tree.
;; * It can only store numbers.
;; * It'll overflow the stack if the tree gets too deep.
;; * It produces (via xseq) a non-lazy seq that will contain an entire copy of the tree.
;; * It can create unbalanced trees that have worst-case algorithmic complexity.

;; Although structural sharing as described using xconj as a basis example can reduce the memory footprint of persistent
;; data structures, it alone is insufficient. Instead, Clojure leans heavily on the notion of lazy sequences to further
;; reduce its memory footprint.
