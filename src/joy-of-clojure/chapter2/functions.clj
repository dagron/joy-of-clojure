;; Anonymous Functions
;; ---------------------------------------------------------------------------------------------------------------------
;; An anonymous (unnamed) Clojure function can be defined using a special form. A special form is a Clojure expression
;; that's part of the core language but not created in terms of functions, types, or macros.
;; Here's an example of a function that takes two elements and returns a set of those elements:
(fn [x y]                     ;; A vector contains the function parameters
  (println "Making a set")    ;; The function body
  #{x y})                     ;; The last expression in a function is its return value

;; This is nice, but how can you call an anonymous function if it doesn't have a name? You lose all reference to it
;; after it's been declared. Well, actually, you can define a function and call it in a single expression,
;; as in this example:
((fn [x y]
   (println "Making a set")   ;; Define the function,
     #{x y})                  ;; and call it right away,
  1 2)                        ;; passing 1 and 2 to it


;; Creating named functions with def
;; ---------------------------------------------------------------------------------------------------------------------
;; In order to associate a name with the previous function using def, you use
(def make-set
  (fn [x y]
    (println "Making a set")
      #{x y}))

;; And you can now call it in a more intuitive way:
(make-set 1 2)
;; Making a set
;;=> #{1 2}


;; Creating named functions with defn
;; ---------------------------------------------------------------------------------------------------------------------
;; Another way to define functions in Clojure is using the defn macro. Although def is one way to define functions by
;; name, as shown it's cumbersome to use. The defn macro is a more convenient and concise way to create named functions.
;; It provides a syntax similar to the original fn form and also allows an additional documentation string:
(defn make-set
  "Takes two values and makes a set from them."
  [x y]
  (println "Making a set")
    #{x y})


;; Functions with multiple arities
;; ---------------------------------------------------------------------------------------------------------------------
;; Arity refers to the differences in the argument count that a function will accept. Changing the previous simple set-
;; creating function to accept either one or two arguments is represented as
(defn make-set
  ([x]   #{x})
  ([x y] #{x y}))

;; The difference from the previous form is that you can now have any number of argument/body pairs as long as the
;; arities of the arguments differ. Naturally, the execution of such a function for one argument is
(make-set 42)
;;=> #{42}

;; As you saw, arguments to functions are bound one for one to symbols during the function call, but there’s a way for
;; functions to accept a variable number of arguments:
(make-set 1 2 3)
;; ArityException Wrong number of args passed...


;; As shown, calling the make-set function with three arguments won’t work. But what if you want it to take any number
;; of arguments? The way to denote variable arguments is to use the & symbol followed by symbols or destructuring forms.
;; Every symbol in the arguments list before the & is still bound one for one to the same number of arguments passed
;; during the function call. But any additional arguments are aggregated in a sequence bound to the symbol following
;; the & symbol:
(defn arity2+ [first second & more]   ; Defines a function taking 2 or more args
  (vector first second more))

(arity2+ 1 2)                                       ; Extra args are nil
;;=> [1 2 nil]

(arity2+ 1 2 3 4)                                   ; Extra args are a list
;;=> [1 2 (3 4)]

(arity2+ 1)                                         ; Too few args is an error
;; ArityException Wrong number of args passed


;; In-place functions with #()
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure provides a shorthand notation for creating an anonymous function using the #() reader feature. In a nutshell,
;; reader features are loosely analogous to C++ preprocessor directives in that they signify that some given form should
;; be replaced with another at read time. In the case of the #() form, it's effectively replaced with the special form
;; fn. Anywhere that it's appropriate to use #(), it's likewise appropriate for the fn special form.
;; The #() form can also accept arguments that are implicitly declared through the use of special symbols
;; prefixed with %:
(def make-list0 #(list))                            ; Takes no args
(make-list0)
;;=> ()

(def make-list2 #(list %1 %2))                      ; Takes exactly two args
(make-list2 1 2)
;;=> (1 2)

(def make-list2+ #(list %1 %2 %&))                  ; Takes two or more args
(make-list2+ 1 2 3 4 5)
;;=> (1 2 (3 4 5))

;; A couple of notes about these examples are worth mentioning. First, a function taking one argument can be written
;; using either the explicit #(list %1) or the implicit #(list %). The % symbol means the same as %1, but we prefer the
;; numbered version. Also note that the %& symbol in make-a-list2+ is used to refer to the variable arguments
;; passed as arguments.
