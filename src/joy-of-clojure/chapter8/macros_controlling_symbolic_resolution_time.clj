;; Using macros to control symbolic resolution time
;; ---------------------------------------------------------------------------------------------------------------------
;; Whereas functions accept and return values that are meaningful to an application at runtime, macros accept and return
;; code forms that are meaningful at compile time. Any symbol has subtleties depending on whether it's fully qualified,
;; its resolution time, and its lexical context. These factors can be controlled in any particular case by the
;; appropriate use of quoting and unquoting, which we explore in this section.
;; The term "name capture" refers to a potential problem in macro systems where a name generated at compile time clashes
;; with a name that exists at runtime. Clojure macros are mostly safe from name capture, because the use of syntax-quote
;; attempts to resolve symbols at macro-expansion time. This strategy reduces complexity by ensuring that symbols refer
;; to those available at macro-expansion time, rather than to those unknown in the execution context.
;; For example, consider one of the simplest possible macros:
(defmacro resolution [] `x)

;; Viewing the expansion of this macro is illuminating in understanding how Clojure macros resolve symbols:
(macroexpand '(resolution))
;;=> user/x

;; The expansion of the macro resolves the namespace of the syntax-quoted symbol x. This behavior is useful in Clojure
;; because it helps to avoid problems with free name capturing that are possible in a macro system such as that found in
;; Common Lisp. Here's an example that would trip up a lesser implementation of syntax-quote, but which does just what
;; you want in Clojure:
(def x 9)
(let [x 109] (resolution))
;;=> 9

;; The x defined in the let isn't the same as the namespace-qualified user/x referred to by the macro resolution. As you
;; might expect, the macro would throw an unbound var exception if you didn't first execute the call to def. If it
;; didn't error out this way, the local version of x would be used instead, which might not be what you intend.
;; Clojure does provide a way to defer symbolic resolution for those instances where it may be useful to resolve it in
;; the execution context, which we'll show now.

;; Anaphora
;; --------
;; Anaphora in spoken language is a term used in a sentence that refers back to a previously identified subject or
;; object. It helps to reduce repetition in a phrase by replacing "Jim bought 6,000 Christmas lights and hung all the
;; Christmas lights," with “Jim bough 6,000 Christmas lights and hung them all." In this case, the word "them" is the
;; anaphora. Some programming languages use anaphora, or allow for their simple definition. Scala has a rich set of of
;; anaphoric patterns primarily focused around its _ operator:

;; // NOTE: This is Scala, not Clojure
;; Array(1, 2, 3, 4, 5).map(2 * _)
;; //=> res0: Array[Int] = Array(2, 4, 6, 8, 10)

;; In this Scala example, the underscore serves to refer back to an implicitly passed argument to the map function,
;; which in this case would be each element of the array in succession. The same expression could be written with
;; (x) => 2 * x -- the syntax for an anonymous function -- in the body of the map call, but that would be unnecessarily
;; verbose.
;; Anaphora don't nest and as a result generally aren't employed in Clojure. Within a nested structure of anaphoric
;; macros, you can only refer to the most immediate anaphoric binding, and never those from outer lexical contours, as
;; demonstrated in the next example. For example, the Arc programming language contains a macro named awhen that is
;; similar to Clojure's when, except that it implicitly defines a local named it used in its body to refer to the value
;; of the checked expression. You can implement the same macro, called awhen in Clojure, as shown here:
(defmacro awhen [expr & body]
  `(let [~'it ~expr]                                        ; Define anaphora
     (if ~'it                                               ; Check its truth
       (do ~@body))))                                       ; Inline the body

(awhen [1 2 3] (it 2))                                      ; Use "it" in the body
;;=> 3

(awhen nil (println "Will never get here"))
;;=> nil

(awhen 1 (awhen 2 [it]))                                    ; Fails to nest
;;=> [2]

;; Clojure provides similar macros that do nest and replace the need for anaphora: if-let and when-let. When designing
;; your own macros, it's preferable to build them along these lines so that the macro itself takes the name to be bound.
;; But just because typical anaphorics are limited, that's not to say they're entirely useless. Instead, for your own
;; libraries you may find that their usage is intuitive. You'll see the pattern ~'symbol at times in Clojure macros for
;; selectively capturing a symbolic name in the body of a macro. The reason for this bit of awkwardness is that
;; Clojure's syntax-quote attempts to resolve symbols in the current context, resulting in fully qualified symbols.
;; Therefore, ~' avoids that resolution by unquoting a quote.
