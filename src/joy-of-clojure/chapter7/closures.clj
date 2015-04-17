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

;; Functions returning closures
;; Each of the previous examples creates a single closure, but by wrapping similar code in another function definition,
;; you can create more closures on demand. For example, you can take the earlier times-two example and generalize it to
;; take an argument instead of using 2 directly:
(defn times-n [n]
  (let [x n]
    (fn [y] (* y x))))

;; We've covered functions returning functions before, but if you're not already familiar with closures, this may be a
;; stretch. You now have an outer function stored in a var named times-n -- note that you use defn instead of def. When
;; times-n is called with an argument, it returns a new closure created by the fn form and closing over the local x. The
;; value of x for this closure is whatever is passed in to times-n:
(times-n 4)
;;=> #<user$times_n$fn__3454 user$times_n$fn__3454@142b4159>

;; Viewing the function form for this closure isn't too useful, so instead you can store it in a var, allowing you to
;; call it by a friendlier name such as times-four:
(def times-four (times-n 4))

;; Here you're using def again to store what times-n returns -- a closure over the number 4. Thus when you call this
;; closure with an argument of its own, it returns the value of y times x, as shown:
(times-four 10)
;;=> 40

;; Note that when you call the closure stored in times-four, it uses the local it closed over as well as the argument in
;; the call.

;; Closing over parameters
;; The definition of times-n creates a local x using let and closes over that instead of closing over the argument n
;; directly. But this was only to help focus the discussion on other parts of the function. In fact, closures close over
;; parameters of outer functions the same way they do over let locals. Thus times-n can be defined without any let at
;; all:
(defn times-n [n]
  (fn [y] (* y n)))

;; All the preceding examples would work exactly the same. Here's another function that creates and returns a closure in
;; a similar way. Note again that the inner function maintains access to the outer parameter even after the outer
;; function has returned:
(defn divisible [denom]
  (fn [num]
    (zero? (rem num denom))))

;; You don't have to store a closure in a var; you can instead create one and call it immediately:
((divisible 3) 6)
;;=> true

((divisible 3) 7)
;;=> false

;; Instead of storing or calling a closure, a particular need is best served by pasing a closure along to another
;; function that will use it.
