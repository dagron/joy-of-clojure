;; Thinking recursively
;; ---------------------------------------------------------------------------------------------------------------------
;; You're likely already familiar with the basics of recursion, and as a result you can take heart that we won't force
;; you to read a beginner's tutorial again. But because recursive solutions are prevalent in Clojure code, it's
;; important for us to cover it well enough that you can fully understand Clojure's recursive offerings.

;; Recursion is often viewed as a low-level operation reserved for times when solutions involving higher-order functions
;; either fail or lead to obfuscation. Granted, it's fun to solve problems recursively because even for those of us
;; who've attained some level of acumen with functional programming, finding a recursive solution still injects a bit of
;; magic into our day. Recursion is a perfect building block for creating higher-level looping constructs and functions,
;; as we'll show in this section.

;; Mundane recursion
;; A classically recursive algorithm is that of calculating some base number raised to an exponent, or the pow function.
;; A straightforward way to solve this problem recursively is to multiply the base by each successively smaller value of
;; the exponent, as implemented here:
(defn pow [base exp]
  (if (zero? exp)
    1
    (* base (pow base (dec exp)))))

(pow 2 10)
;;=> 1024

(pow 1.01 925)
;;=> 9937.353723241924

;; We say that the recursive call is mundane because it's named explicitly rather than through mutual recursion or
;; implicitly with the recur special form. Why is this a problem? The answer lies in what happens when you try to call
;; pow with a large value:
(pow 2 10000)
;;=> java.lang.StackOverflowError

;; The implementation of pow is doomed to throw java.lang.StackOverflowError because the recursive call is trapped by
;; the multiplication operation. The ideal solution is a tail-recursive version that uses the explicit recur form, thus
;; avoiding stack consumption and the resulting exception. One way to remove the mundane recursive call is to perform
;; the multiplication at a different point, thus freeing the recursive call to occur in the tail position, as shown next:
(defn pow [base exp]
  (letfn [(kapow [base exp acc]
                 (if (zero? exp)
                   acc
                   (recur base (dec exp) (* base acc))))]
    (kapow base exp 1)))
(pow 2N 10000)
;;=> ... a ridiculously large number

;; This new version of pow uses two common techniques for converting mundane recursion to tail recursion. First, it uses
;; a helper function kapow that does the majority of the work. Second, kapow uses an accumulator acc that holds the
;; result of the multiplication. The exponent exp is no longer used as a multiplicative value but instead functions as a
;; decrementing counter, eliminating a stack explosion.

;; Regular recusion is fun again with lazy-seq
;; As mentioned earlier, the lazy-seq recipe rule of thumb #1 states that you should wrap outer-layer function bodies
;; with the lazy-seq macro when generating lazy seqs. The implementation of lz-rec-step used mundane recursion but
;; managed to avoid stack-overflow exceptions thanks to the use of lazy-seq. For functions generating sequences, the use
;; of lazy-seq might be a better choice than tail recursion, because often the regular (mundane) recursive definition is
;; the most natural and understandable.

;; A recursive units calculator
;; Some problems scream out for a recursive solution; take for example the problem of unit conversions. A kilometer
;; consists of 1,000 meters, each made of 100 centimeters, each of which is 10 millimeters, each of which is 1/1,000 of
;; a meter. These types of conversions are often needed in far-ranging applications.
;; If you wanted to describe this relationship in terms of a data structure, then you might land on something like the
;; map that follows:
(def simple-metric {:meter 1,
                    :km 1000,
                    :cm 1/100,
                    :mm [1/10 :cm]})

;; The map simple-metric uses the :meter value as the base unit, or the unit used as the reference point for every other
;; unit. To calculate the answer to "How many meters are in 3 kilometers, 10 meters, 80 centimeters, 10 millimeters?"
;; you could use the map as follows:
(-> (* 3 (:km simple-metric))
    (+ (* 10 (:meter simple-metric)))
    (+ (* 80 (:cm simple-metric)))
    (+ (* (:cm simple-metric)
          (* 10 (first (:mm simple-metric)))))
    float)
;;=> 3010.81

;; Although the map is certainly usable this way, the user experience of traversing simple-metric directly is less than
;; stellar. Instead, it would be nicer to define a function named convert, shown in the following listing, that
;; essentially performs these mathematical operations.

;; Function to recursively convert units of measure
(defn convert [context descriptor]
  (reduce (fn [result [mag unit]]                           ; 1. Destructure aggregates
            (+ result
               (let [val (get context unit)]                ; 2. Look up the relative value
                 (if (vector? val)
                   (* mag (convert context val))            ; 3. Process the aggregate
                   (* mag val)))))                          ; 4. Perform the final calculation
          0
          (partition 2 descriptor)))

;; The action of the convert function programmatically mirrors the manual use of simple-metric shown earlier. The form
;; of the descriptor coming into the function and mirrored in the destructuring form (1) is the opposite of the key/value
;; mapping in the context map. This is because it allows the descriptor to take a more linguistically natural form where
;; the magnitude precedes the unit name (such as 1 :meter).
;; After binding the magnitude mag and the unit name, the value associated with the unit is retrieved (2). In the case
;; where a straight lookup results in a number, the :default case takes over and results in a magnitude multiplication.
;; This straightforward multiplication is the recursion's terminating condition. In the case where a lookup results in a
;; vector, the recursion continues with the vector itself for the purpose of traversing the recursive unit definitions
;; (3). Eventually, the function should bottom out on a nonvector, thus allowing the final magnitude multiplication (4).
;; Because of the recursion, the multiplication rolls up through all the intermediate relative unit values.
;; You can see convert in action next:
(convert simple-metric [1 :meter])
;;=> 1

(convert simple-metric [50 :cm])
;;=> 1/2

(convert simple-metric [100 :mm])
;;=> 1/10

;; And of course, convert should handle compounds:
(float (convert simple-metric [3 :km 10 :meter 80 :cm 10 :mm]))
;;=> 3010.81

;; The beauty of convert is that it's not bound to units of length. Through a synergy between recursive data and
;; recursive function, you've defined a generic unit-conversion specification, allowing other unit types:

(convert {:bit 1, :byte 8, :nibble [1/2 :byte]} [32 :nibble])
;;=> 128N

;; Tail calls and recur
;; --------------------
;; In a language such as Clojure, where function locals are immutable, the benefit of tail recursion is especially
;; important for implementing algorithms that require the consumption of a value or the accumulation of a result. Before
;; we get deeper into implementing tail recursion, we'll take a moment to appreciate the historical underpinnings of
;; tail-call recursion and expound on its further role in Clojure.

;; Generalized tail-call optimization (tco)
;; In their "Lambda Papers", Guy L. Steele and Gerald Sussman describe their experiences with the research and
;; implementation of the early versions of the Scheme programming language. The first versions of the interpreter served
;; as a model for Carl Hewitt's Actor model of concurrent computation, implementing both actors and functions. As a
;; quick summary, you can think of the Actor model as one where each actor is a process with local state and an event
;; loop processing messages from other actors to read, write, and compute on that state. In any case, one day, while
;; eating Ho-Hos, Steele and Sussman noticed that the implementation of control flow in Scheme, implemented using
;; actors, always ended with one actor calling another in its tail position, with the return to the callee being
;; deferred. Armed with their intimate knowledge of the Scheme compiler, Steele and Sussman were able to infer that
;; because the underlying architecture dealing with actors and functions was the same, retaining both was redundant.
;; Therefore, actors were removed from the language and functions remained as the more general construct. Thus,
;; generalized tail-call optimization was thrust into the world of computer science.
;; Generalized tail-call optimization as found in Scheme can be viewed as analogous to object delegation. Hewitt's
;; original Actor model was rooted heavily in message delegation of arbitrary depth, with data manipulation occurring at
;; any and all levels along the chain. This is similar to an adapter, except that there's an implicit resource-
;; management element involved. In Scheme, any tail call from a function A to a function B results in the deallocation
;; of all of A local resources and the full delegation of execution to B. As a result of this generalized tail-call
;; optimization, the return to the original caller of A is directly from B instead of back down the call chain through A
;; again. Unfortunately for Clojure, neither the Java Virtual Machine nor its bytecode provide generalized tail-call
;; optimization facilities. Clojure does provide a tail call special form recur, but it only optimizes the case of a
;; tail-recursive self call and not the generalized tail call. In the general case, there's currently no way to reliably
;; optimize tail calls.

;; Tail recursion
;; The following function calculates the greatest common denominator of two numbers:
(defn gcd [x y]
  (cond
    (> x y) (gcd (- x y) y)
    (< x y) (gcd x (- y x))
    :else x))

;; The implementation of gcd is straightforward, but notice that it uses mundane recursion instead of tail recursion via
;; recur. In a language such as Scheme, containing generalized tail-call optimization, the recursive calls are optimized
;; automatically. On the other hand, because of the JVM's lack of tail-call optimization, the recur is needed in order
;; to avoid stack-overflow errors.

;; Using the information in the table below, you can replace the mundane recursive calls with the recur form,
;; causing gcd to be optimized by Clojure's compiler.

;; Tail positions and recur targets
;; ---------------------------------+-------------------------------------------------------------------+---------------
;;             Form(s)              |                            Tail position                          | Recur target?
;; ---------------------------------+-------------------------------------------------------------------+---------------
;; fn, defn                         | (fn [args] expressions tail)                                      | Yes
;; loop                             | (loop [bindings] expressions tail)                                | Yes
;; let, letfn, binding              | (let [bindings] expressions tail)                                 | No
;; do                               | (do expressions tail)                                             | No
;; if, if-not                       | (if test then-tailelse-tail)                                      | No
;; when, when-not                   | (when test expressions tail)                                      | No
;; cond                             | (cond test test tail ...:else else tail)                          | No
;; or, and                          | (or test test... tail)                                            | No
;; case                             | (case const const tail ... default tail)                          | No

;; Why recur?
;; If you think you understand why Clojure provides an explicit tail-call optimization form rather than an implicit one,
;; then go ahead and skip to the next section.
;; There's no technical reason why Clojure couldn't automatically detect and optimize recursive tail calls — Scala does
;; this - but there are valid reasons why Clojure doesn't. First, because there's no generalized TCO in the JVM, Clojure
;; can only provide a subset of tail-call optimizations: the recursive case and the mutually recursive case (see the
;; next section). By making recur an explicit optimization, Clojure doesn't give the pretense of providing full TCO.
;; Second, having recur as an explicit form allows the Clojure compiler to detect errors caused by an expected tail call
;; being pushed out of the tail position. If you change gcd to always return an integer, then an exception is thrown
;; because the recur call is pushed out of the tail position:
(defn gcd [x y]
  (int
    (cond
      (> x y) (recur (- x y) y)
      (< x y) (recur x (- y x))
      :else x)))
;;=> java.lang.UnsupportedOperationException: Can only recur from tail position...

;; With automatic recursive tail-call optimization, the addition of an outer int call wouldn't necessarily trigger an
;; error condition. But Clojure enforces that a call to recur be in the tail position. This benefit will likely cause
;; recur to live on, even should the JVM acquire TCO.
;; The final benefit of recur is that it allows the forms fn and loop to act as anonymous recursion points. Why recur,
;; indeed.

;; Don't forget your trampoline
;; ----------------------------
;; We touched briefly on the fact that Clojure can also optimize a mutually recursive function relationship, but like
;; the tail-recursive case, it's done explicitly. Mutually recursive functions are nice for implementing finite state
;; machines (FSAs), and in this section we'll show an example of a simple state machine modeling the operation of an
;; elevator for a two-story building. The elevator FSA allows only four states: on the first floor with the doors open
;; or closed, and on the second floor with the doors open or closed. The elevator can also take four distinct commands:
;; open doors, close doors, go up, and go down. Each command is valid only in a certain context: for example, the close
;; command is valid only when the elevator door is open. Likewise, the elevator can only go up when on the first floor
;; and can only go down when on the second floor, and the door must be shut in both instances.
;; You can directly translate these states and transitions into a set of mutually recursive functions by associating the
;; states as a set of functions ff-open, ff-closed, sf-closed, and sf-open, and the transitions :open, :close, :up, and
;; :down, as conditions for calling the next function. Let's create a function elevator that starts in the ff-open
;; state, takes a sequence of commands, and returns true or false if the commands correspond to a legal schedule
;; according to the FSA. For example, the sequence [:close :open :done] would be legal, if pointless, whereas
;; [:open :open :done] wouldn't be legal, because an open door can't be reopened. The function elevator can be
;; implemented as shown next.

;; Using mutually recursive functions to implement a finite state machine
(defn elevator [commands]
  (letfn                                                    ; Local functions
    [(ff-open [[_ & r]]                                     ; 1st floor open
              "When the elevator is open on the 1st floor
              it can either close or be done."
              #(case _
                :close (ff-closed r)
                :done true
                false))
     (ff-closed [[_ & r]]                                   ; 1st floor closed
                "When the elevator is closed on the 1st floor
                it can either open or go up."
                #(case _
                       :open (ff-open r)
                       :up (sf-closed r)
                       false))
     (sf-closed [[_ & r]]                                   ; 2nd floor closed
                "When the elevator is clsoed on the 2nd floor
                it can either go down or open."
                #(case _
                  :down (ff-closed r)
                  :open (sf-open r)
                  false))
     (sf-open [[_ & r]]                                     ; 2nd floor open
              "When the elevator is open on the 2nd floor
              it can either close or be done."
              #(case _
                :close (sf-closed r)
                :done true
                false))]
    (trampoline ff-open commands)))                         ; Trampoline call

;; Using letfn this way allows you to create local functions that reference each other, whereas
;; (let [ff-open #(...)] ...) wouldn't, because it executes its bindings serially. Each state function contains a case
;; macro that dispatches to the next state based on a contextually valid command. For example, the sf-open state
;; transitions to the sf-closed state given a :close command, returns true on a :done command (corresponding to a legal
;; schedule), or otherwise returns false. Each state is similar in that the default case command is to return false,
;; indicating an illegal schedule. One other point of note is that each state function returns a function returning a
;; value, rather than directly returning the value. This is done so that the trampoline function can manage the stack on
;; the mutually recursive calls, thus avoiding cases where a long schedule would blow the stack. The trampoline manages
;; the process of the self calls through the placement of the functions in a list, where each function is bounced back
;; and forth explicitly.
;; Here's the operation of elevator given a few example schedules:
(elevator [:close :open :close :up :open :open :done])
;;=> false

(elevator [:close :up :open :close :down :open :done])
;;=> true

;; Run at your own risk!
(elevator (cycle [:close :open]))
;; ... runs forever

;; Like the recur special form, the trampoline for mutual recursion has a definitive syntactic and semantic cost on the
;; structure of your code. But whereas the call to recur can be replaced by mundane recursion without too much effect,
;; except at the edges, the rules for mutual recursion aren't general. Having said that, the actual rules are simple:
;; 1. Make all functions participating in the mutual recursion return a function instead of their normal result.
;;    Normally this is as simple as tacking a # onto the front of the outer level of the function body.
;; 2. Invoke the first function in the mutual chain via the trampoline function.
;; The final example doesn't cause a stack overflow because the trampoline function handles the calls explicitly. The
;; typical use case for mutually recursive functions is a state machine,
;; of which the elevator FSA is only a simple case.

