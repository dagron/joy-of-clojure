;; On closures
;; ---------------------------------------------------------------------------------------------------------------------
;; It took only 30 years, but closures are now a key feature of mainstream programming languages -- Perl and Ruby
;; support them, and JavaScript derives much of what power it has from closures. So what's a closure? In a sentence,
;; a closure is a function that has access to locals from the context where it was created:
(def times-two
  (let [x 2]
    (fn [y] (* y x))))

;; The fn form defines a function and uses def to store it in a var named times-two. The let forms a lexical scope in
;; which the function was defined, so the function gains access to all the locals in that lexical context. That's what
;; makes this function a closure: it uses the local x that was defined outside the body of the function, and so the
;; local and its value become a proerty of the function itself. The function is said to close over the local x, as in
;; the following example:
(times-two 5)
;;=> 10

;; This isn't terribly interesting, but one way to make a more exciting closure is to have it close over something
;; mutable:
(def add-and-get
  (let [ai (java.util.concurrent.atomic.AtomicInteger.)]
    (fn [y] (.addAndGet ai y))))

(add-and-get 2)
;;=> 2

(add-and-get 2)
;;=> 4

(add-and-get 7)
;;=> 11

;; The java.util.concurrent.atomic.AtomicInteger class holds an integer value, and its .addAndGet method adds to its
;; value, stores the result, and also returns the result. The function add-and-get is holding on to the same instance
;; of AtomicInteger, and each time it's called, the value of that instance is modified. Unlike the earlier times-two
;; function, this one can't be rewritten with the local ai defined in the function. If you tried, then each time the
;; function was called, it would create a new instance with a default value of 0 to be created and stored in ai --
;; clearly not what should happen. A point of note about this technique is that when closing over something mutable, you
;; run the risk of making your functions impure and thus more difficult to test and reason about, especially if the
;; mutable local is shared.
