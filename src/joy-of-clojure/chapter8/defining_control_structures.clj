;; Defining Control Structures
;; ---------------------------------------------------------------------------------------------------------------------
;; Most control structures in Clojure are implemented via macros, so they provide a nice starting point for learning how
;; macros can be useful. Macros can be built with or without using syntax-quote, so we'll show examples of each.
;; In languages lacking macros, such as Haskell for example, the definition of control structures relies on the use of
;; higher-order functions such as we showed earlier. Although this fact in no way limits the ability to create control
;; structures in Haskell, the approach that Lisps take to the problem is different. The most obvious advantage of macros
;; over higher-order functions is that the former manipulate compile-time forms, transforming them into runtime forms.
;; This allows your programs to be written in ways natural to your problem domain, while still maintaining runtime
;; efficiency. Clojure already provides a rich set of control structures, including but not limited to doseq, while, if,
;; if-let, and do, but in this section you'll write a few others.

;; Defining control structures without syntax-quote
;; ------------------------------------------------
;; Because the arguments to defmacro aren't evaluated before being passed to the macro, they can be viewed as pure data
;; structures and manipulated and analyzed as such. Because of this, amazing things can be done on the raw forms
;; supplied to macros even in the absence of unquoting.
;; Imagine a macro named do-until that executes all of its clauses evaluating as true until it gets one that is falsey:
(do-until
  (even? 2) (println "Even")
  (odd?  3) (println "Odd")
  (zero? 1) (println "You never see me")
  :lollipop (println "Truthy thing"))
;  Even
;  Odd
;;=> nil

;; A good example of this type of macro is Clojure's core macro cond, which with some minor modifications can be made to
;; behave differently:
(defmacro do-until [& clauses]
  (when clauses                                             ; When there are clauses
    (list 'clojure.core/when (first clauses)                ; ... build up a list of each paired clause
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException. "do-until requires an even number of forms")))
          (cons 'do-until (nnext clauses)))))               ; ... recursively

;; The first expansion of do-until illustrates how this macro operates:
(macroexpand-1 '(do-until true (prn 1) false (prn 2)))
;;=> (clojure.core/when true (prn 1) (do-until false (prn 2)))

;; do-until recursively expands into a series of when calls, which themselves expand into a series of if expressions
;; (because when is a macro defined in terms of the built-in if):
(require '[clojure.walk :as walk])
(walk/macroexpand-all '(do-until true (prn 1) false (prn 2)))
;;=> (if true (do (prn 1) (if false (do (prn 2) nil))))

(do-until true (prn 1) false (prn 2))
;  1
;;=> nil

;; You could write out the nested if structure manually and achieve the same result, but the beauty of macros lies in
;; the fact that they can do so on your behalf while presenting a lightweight and intuitive form. In cases where
;; do-until can be used, it removes the need to write and maintain superfluous boilerplate code. This idea can be
;; extended to macros in general and their propensity to reduce unneeded boilerplate for a large category of
;; circumstances, as you desire. One thing to note about do-until is that it's meant to be used only for side effects,
;; because it's designed to always return nil. Macros starting with do tend to act the same way.


