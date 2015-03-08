;; Lists: Clojure's code-form data structure
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure's PersistentLists are by far the simplest of Clojure's persistent collection types. A PersistentList is a
;; singly linked list where each node knows its distance from the end. List elements can only be found by starting with
;; the first element and walking each prior node in order, and can only be added or removed from the left end.
;; Lists are almost exclusively used to represent code forms. They're used literally in code to call functions, macros,
;; and so forth, as we'll discuss shortly. Code forms are also built programmatically to then be eval'ed or used as the
;; return value for a macro. If the final usage of a collection isn't as Clojure code, lists rarely offer any value over
;; vectors and are thus rarely used. But lists have a rich heritage in Lisps, so we'll discuss when they should be used
;; in Clojure, and also when they shouldn't -- situations in which there are now better options.

;; Lists like Lisps like
;; ---------------------
;; All flavors of Lisp have lists that they like to use, and Clojure lists, already introduced earlier, are similar
;; enough to be familiar. The functions have different names, but what other Lisps call car is the same as first on a
;; Clojure list. Similarly, cdr is the same as next. But there are substantial differences as well. Perhaps the most
;; surprising is the behavior of cons. Both cons and conj add something to the front of a list, but the order of their
;; arguments is different:
(cons 1 '(2 3))
;;=> (1 2 3)

(conj '(2 3) 1)
;;=> (1 2 3)

;; In a departure from classic Lisps, the "right" way to add to the front of a list is with conj. For each concrete
;; type, conj adds elements in the most efficient way, and for lists this means at the left side. Additionally, a list
;; built using conj is homogeneous -- all the objects on its next chain are guaranteed to be lists, whereas sequences
;; built with cons only promise that the result will be some kind of seq. So you can use cons to add to the front of a
;; lazy seq, a range, or any other type of seq, but the only way to get a bigger list is to use conj. Either way, the
;; next part has to be some kind of sequence, which points out another difference from other Lisps: Clojure has no
;; dotted pair, a cons-cell whose cdr is a value, not another cons, as we showed earlier. All you need to know is that
;; if you want a simple pair in a Clojure program, you should use a vector of two items.
;; All seqs print with rounded parentheses, but this does not mean they're the same type or will behave the same way.
;; For example, many of these seq types don't know their own size the way lists do, so calling count on them may be O(n)
;; instead of O(1). An unsurprising difference between lists in Clojure and other Lisps is that they're immutable.
;; At least that had better not be surprising anymore. Changing values in a list is generally discouraged in other Lisps
;; anyway, but in Clojure it's impossible.

;; Lists as stacks
;; ---------------
;; Lists in all Lisps can be used as stacks, but Clojure goes further by supporting the IPersistentStack interface. This
;; means you can use the functions peek and pop to do roughly the same thing as first and next. Two details are worth
;; noting. One is that next and rest are legal on an empty list, but pop throws an exception. The other is that next on
;; a one-item list returns nil, whereas rest and pop both return an empty list.
;; When you want a stack, the choice between using a list and a vector is a somewhat subtle decision. Their memory
;; organization is different, so it may be worth testing your usage to see which performs better. Also, the order of
;; values returned by seq on a list is backward compared to seq on a vector, and in rare cases this can point to one or
;; the other as the best solution. In the end, it may come down primarily to personal taste.

;; What lists aren't
;; -----------------
;; Probably the most common misuse of lists is to hold items that will be looked up by index. Although you can use nth
;; to get the 42nd (or any other) item from a list, Clojure has to walk the list from the beginning to find it. Don't do
;; that. In fact, this is a practical reason why lists can't be used as functions, as in ((list :a) 0). Vectors are good
;; at looking things up by index, so use one of those instead.
;; Lists are also not sets. All the reasons we gave in the previous section for why it's a bad idea to frequently search
;; a vector looking for a particular value apply to lists as well. Even more so because contains? always returns false
;; for a list. Finally, lists aren't queues. You can add items to one end of a list, but you can't remove things from
;; the other end.
