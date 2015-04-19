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

;; Passing closures as functions
;; We've shown many examples in previous sections of higher-order functions built in to Clojure's core libraries. What
;; we've glossed over so far is that anywhere a function is expected, a closure can be used instead. This has dramatic
;; consequences for how powerful these functions can be. For example, filter takes a function (called a predicate in
;; this case) and a sequence, applies the predicate to each value of the sequence, and returns a sequence of just the
;; values for which the predicate returned something truthy. A simple example of its use returns only the even numbers
;; from a sequence of numbers:
(filter even? (range 10))
;;=> (0 2 4 6 8)

;; Note that filter only ever passes a single argument to the predicate given it. Without closures, this might be
;; restrictive, but with them you can close over the values needed:
(filter (divisible 4) (range 10))
;;=> (0 4 8)

;; It's common to define a closure right on the spot where it's used, closing over whatever local context is needed:
(defn filter-divisible [denom s]
  (filter (fn [num] (zero? (rem num denom))) s))

(filter-divisible 4 (range 10))
;;=> (0 4 8)

;; This kind of on-the-spot anonymous function definition is desired frequently enough that Clojure spends a little of
;; its small syntax budget on the reader feature to make such cases more succinct. This #() form was first introduced in
;; chapter 2 and in this case can be used to write the definition of filter-divisible as follows:
(defn filter-divisible [denom s]
  (filter #(zero? (rem % denom)) s))

(filter-divisible 5 (range 20))
;;=> (0 5 10 15)

;; Although certainly more succinct than the extended anonymous function form and the earlier example using a separate
;; divisible function with filter, there's a fine line to balance between reuse and clarity. Thankfully, in any case the
;; performance differences among the three choices are nominal.

;; Sharing closure context
;; So far, the closures we've shown have stood alone, but it's sometimes useful to have multiple closures closing over
;; the same values. This may take the form of an ad hoc set of closures in a complex lexical environment, such as event
;; callbacks or timer handlers in a nested GUI builder. Or it may be a tidy, specifically designed bundle of values and
;; related functions—something that can be thought of as an object.
;; To demonstrate this, let's build a robot object that has functions for moving it around a grid based on its current
;; position and bearing. For this you need a list of coordinate deltas for compass bearings, starting with north and
;; going clockwise:
(def bearings [{:x  0, :y  1}                               ; north
               {:x  1, :y  0}                               ; east
               {:x  0, :y -1}                               ; south
               {:x -1, :y  0}])                             ; west

;; Note that this is on a grid where y increases as you go north and x increases as you go east -- mathematical
;; coordinate style rather than spreadsheet cells. With this in place, it's easy to write a function forward that takes
;; a coordinate and a bearing and returns a new coordinate, having moved forward one step in the direction of the
;; bearing:
(defn forward [x y bearing-num]
  [(+ x (:x (bearings bearing-num)))
   (+ y (:y (bearings bearing-num)))])

;; Starting with a bearing of 0 (north) at 5,5 and going one step breings the bot to 5,6:
(forward 5 5 0)
;;=> [5 6]

;; You can also try starting at 5,5 and with bearing 1 (east) or bearing 2 (south) and see the desired results:
(forward 5 5 1)
;;=> [6 5]

(forward 5 5 2)
;;=> [5 4]

;; But you have no closures yet, so you'll build a bot object that keeps not just its coordinates, but also its bearing.
;; In the process, you'll move this standalone forward function into the bot object. By making this a closure, you'll
;; also open up possibilities for polymorphism later. So here's a bot that knows how to move itself forward:
(defn bot [x y bearing-num]
  {:coords [x y]
   :bearing ([:north :east :south :west] bearing-num)
   :forward (fn [] (bot (+ x (:x (bearings bearing-num)))
                        (+ y (:y (bearings bearing-num)))
                        bearing-num))})

;; You can create an instance of this bot and query it for its coordinates or its bearing:
(:coords (bot 5 5 0))
;;=> [5 5]

(:bearing (bot 5 5 0))
;;=> :north

;; But now that you've moved the forward function inside, you no longer pass in parameters, because it gets everything
;; it needs to know from the state of the bot that it closes over. Instead, you use :forward to fetch the closure from
;; inside the bot object and then use an extra set of parentheses to invoke it with no arguments:
(:coords ((:forward (bot 5 5 0))))
;;=> [5 6]

;; Now you have a somewhat complicated beastie, but there's still only a single closure in the mix. Note that the inner
;; set of parentheses is for the call of :forward, which returns the anonymous function; the outer set then calls that
;; function. To make things more interesting, let's add turn-left and turn-right functions and store them right there in
;; the object with :forward:
(defn bot [x y bearing-num]
  {:coords     [x y]
   :bearing    ([:north :east :south :west] bearing-num)
   :forward    (fn [] (bot (+ x (:x (bearings bearing-num)))
                           (+ y (:y (bearings bearing-num)))
                           bearing-num))
   :turn-right (fn [] (bot x y (mod (+ 1 bearing-num) 4)))
   :turn-left  (fn [] (bot x y (mod (- 1 bearing-num) 4)))})

(:bearing ((:forward ((:forward ((:turn-right (bot 5 5 0))))))))
;;=> :east

(:coords ((:forward ((:forward ((:turn-right (bot 5 5 0))))))))
;;=> [7 5]

;; We won't talk about the verbosity of using the bot object yet; instead we'll focus on the features used in the
;; definition of bot. You're freely mixing values computed when a bot is created (such as the :bearing) and functions
;; that create values when called later. The functions are closures, and each has full access to the lexical
;; environment. The fact that there are multiple closures sharing the same environment isn't awkward or unnatural and
;; flows easily from the properties of closures already shown.

;; We'd like to demonstrate one final feature of this pattern for building objects: polymorphism. For example, here's
;; the definition of a bot that supports all the same usage as earlier, but this one has its wires crossed or perhaps is
;; designed to work sensibly in Alice's Wonderland. Specifically, like the bots defined previously, the mirror-bot in
;; the following code has all the same names for its fields -- a form of duck typing. When told to go forward,
;; mirror-bot instead reverses, and it turns left instead of right and vice versa:
(defn mirror-bot [x y bearing-num]
  {:coords     [x y]
   :bearing    ([:north :east :south :west] bearing-num)
   :forward    (fn [] (mirror-bot (- x (:x (bearings bearing-num)))
                                  (- y (:y (bearings bearing-num)))
                                  bearing-num))
   :turn-right (fn [] (mirror-bot x y (mod (- 1 bearing-num) 4)))
   :turn-left  (fn [] (mirror-bot x y (mod (+ 1 bearing-num) 4)))})

;; By bundling the functions that operate on data inside the same structure as the data itself, simple polymorphism is
;; possible. Because each function is a closure, no object state needs to be explicitly passed; instead, each function
;; uses any locals required to do its job.
;; You probably cringed at the number of parentheses required to call these particular object closures, and rightfully
;; so. We encourage you to extrapolate from the closure examples when dealing with your own applications and see how
;; they can solve a variety of tricky and unusual problems. Although this kind of structure is simple and powerful and
;; may be warranted in some situations, Clojure provides other ways of associating functions with data objects that are
;; more flexible. In fact, the desire to avoid a widespread need for this type of ad hoc implementation inspired the
;; creation of Clojure's reify macro, which we'll cover in a later section.
