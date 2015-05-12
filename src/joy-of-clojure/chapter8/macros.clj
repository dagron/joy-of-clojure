;; Macros
;; ---------------------------------------------------------------------------------------------------------------------
;; Macros are where the rubber of "code is data" meets the road of making programs simpler and cleaner. To fully
;; understand macros, you need to understand the different times of Clojure: read time, macro-expansion time, compile
;; time, and run-time. Macros perform the bulk of their work at compile time. We'll start by looking at what it means
;; for code to be data and data to be used as code. This is the background you'll need to understand that control
;; structures in Clojure are built out of macros, and how you can build your own. The mechanics of macros are relatively
;; simple, and before you're halfway through this section you'll have learned all you technically need to write your
;; own. Where macros get complicated is when you try to bring theoretical knowledge of them into the real world, so to
;; help you combat that we'll lead you on a tour of practical applications of macros.

;; What kinds of problems do macros solve? One extremely important role that macros perform is to let you transform an
;; expression into something else, before runtime. Consider Clojure's -> macro, which returns the result of evaluating a
;; succession of functions on an initial value. To understand the arrow macro, we find it useful to think of it as an
;; arrow indicating the flow of data from one function to another -- the form (-> 25 Math/sqrt int list) can be read
;; as follows:

;; 1 Take the value 25.
;; 2 Feed it into the method Math/sqrt.
;; 3 Feed that result into the function int.
;; 4 Feed that result into the function list.

;; Graphically, this can be viewed as shown in the figure below.
;; (-> 25
;;    |  |
;;    +--+
;;      |
;;      +--------+
;;               |
;;              \|/
;;       (Math/sqrt)
;;      |           |
;;      +-----------+
;;             |
;;        +----+
;;        |
;;       \|/
;;      (int)
;;     |     |
;;     +-----+
;;        |
;;        +---+
;;            |
;;           \|/
;;        (list))

;; It expands into the following expression:
(list (int (Math/sqrt 25)))

;; When viewed this way, the -> macro can be said to weave a sequence of forms into each in turn. This weaving can be
;; done in any form and is always stitched in as the first argument to the outermost expression. Observe how the
;; placement of snowmen provides visual markers for the weave point:
(-> (/ 144 12) (/ 􏰂 2 3) str keyword list)
;;=> (:2)

(-> (/ 144 12) (* 􏰂 4 (/ 2 3)) str keyword (list 􏰂 :33))
;;=> (:32 :33)

;; As shown via snowman, each expression is inserted into the following one at compile time, allowing you to write the
;; whole expression inside-out. Using one of the arrow macros is useful when many sequential operations need to be
;; applied to a single object. So this is a potential use case for macros: taking one form of an expression and
;; transforming it into another form. In this chapter, wewll also look at using macros to combine forms, change forms,
;; control evaluation and resolution of arguments, manage resources, and build functions. But first, what does it mean
;; that Clojure code is data, and why should you care?

;; Data is code is data
;; --------------------
;; You're already familiar with textual representations of data in your programs, at least with strings, lists, vectors,
;; maps, and so on. Clojure, like other Lisps, takes this one step further by having programs be made entirely out of
;; data. Function definitions in Clojure programs are also represented using an aggregation of the various data
;; structures mentioned in the previous sections. Likewise, the expressions representing the execution of functions and
;; the use of control structures are also data structures! These data representations of functions and their executions
;; represent a concept different from the way other programming languages operate. Typically, there's a sharp
;; distinction between data structures and functions of the language. In fact, most programming languages don't even
;; remotely describe the form of functions in their textual representations. With Clojure, there's no distinction
;; between the textual form and the actual form of a program. When a program is the data that composes the program, then
;; you can write programs to write programs. This may seem like nonsense now, but as you'll see throughout this chapter,
;; it's powerful.

;; To start with, look at the built-in Clojure function eval, whose purpose is to take a data structure representing a
;; Clojure expression, evaluate it, and return the result. This behavior can be seen in the following examples:
(eval 42)
;;=> 42

(eval '(list 1 2))
;;=> (1 2)

(eval (list 1 2))
;;=> java.lang.ClassCastException: java.lang.Long cannot be cast to clojure.lang.IFn

;; Why did we get an exception for the last example? The answer lies in the previous example. The quote in '(list 1 2)
;; causes eval to view it as (list 1 2), which is the function call to create the resulting list. Likewise, for the
;; final example eval received a list of (1 2) and attempted to use 1 as a function, thus failing. Not very exciting, is
;; it? The excitement inherent in eval stems from something that we mentioned earlier -- if you provide eval a list in
;; the form expected of a function call, then something else should happen. This something else is the evaluation of a
;; function call and not of the data structure itself. Look at what happens if you try evaluating something more
;; complicated:
(eval (list (symbol "+") 1 2))
;;=> 3

;; In words, the steps involved are as follows:
;; 1 The function symbol receives a string + and returns a symbol data type of +.
;; 2 The function list receives three arguments -- a symbol +, the integer 1, and the integer 2 -- and returns a list of
;;   these elements.
;; 3 The eval function receives a list data type of (+ 1 2), recognizes it as the function call form, and executes the
;;   + function with the arguments 1 and 2, returning the integer 3.
