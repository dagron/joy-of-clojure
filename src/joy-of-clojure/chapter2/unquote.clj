;; Unquote
;; ---------------------------------------------------------------------------------------------------------------------
;; As you discovered, the quote special operator prevents its argument, and all of its subforms, from being evaluated.
;; But there will come a time when you'll want _some_ of its constituent forms to be evaluated. The way to accomplish
;; this feat is to use what's known as an unquote. An unquote is used to demarcate specific forms as requiring
;; evaluation by prefixing them with the symbol ~ within the body of a syntax-quote:
`(+ 10 (* 3 2))
;;=> (clojure.core/+ 10 (clojure.core/* 3 2))

`(+ 10 ~(* 3 2))
;;=> (clojure.core/+ 10 6)

;; What just happened? The final form uses an unquote to evaluate the subform (* 3 2), performing a multiplication
;; of 3 and 2, thus inserting the result into the outermost syntax-quoted form. The unquote is used to denote any
;; Clojure expression as requiring evaluation:

(let [x 2]
  `(1 ~x 3))
;;=> (1 2 3)

`(1 ~(2 3))
;; ClassCastException java.lang.Long cannot be cast to clojure.lang.IFn

;; Whoops! Using the unquote told Clojure that the embedded form should be evaluated. But the marked form here is (2 3);
;; and remember what happens when Clojure encounters an expression like this? It attempts to evaluate it as a function!
;; Therefore, you should take care with unquote to ensure that the form form requiring evaluation is the form
;; you expect. The more appropriate way to perform the previous task would be:
(let [x '(2 3)] `(1 ~x))
;;=> (1 (2 3))

;; This provides a level of indirection such that the expression being evaluated is no longer (2 3) but x. But this new
;; way breaks the pattern of the previous examples that returned a list of (1 2 3).
