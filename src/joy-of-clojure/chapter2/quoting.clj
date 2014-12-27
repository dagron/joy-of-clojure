;; Quoting
;; ---------------------------------------------------------------------------------------------------------------------
;; Using a special operator looks like calling a function -- a symbol as the first item of a list:
(quote age)

;; Each special operator has its own evaluation rules. The quote special operator prevents its argument from being
;; evaluated at all. Although the symbol age by itself might evaluate to the value of a var, when it's inside a quote
;; form, it isn't:
(def age 9)
(quote age)
;;=> age

;; Instead, the entire form evaluates to just the symbol. This works for arbitrarily complex arguments to quote: nested
;; vectors, maps, and even lists that would otherwise be function calls, macro calls, or other special forms. The whole
;; thing is returned:

(quote (cons 1 [2 3]))
;;=> (cons 1 [2 3]))

;; There are a few reasons why you might use the quote form, but by far the most common is so that you can use a literal
;; list as a data collection without having Clojure try to call a function. We've been careful to use vectors in the
;; examples so far in this section because vectors are never function calls. But if you wanted to use a list instead,
;; a naive attempt would fail:
(cons 1 (2 3))
;; java.lang.ClassCastException: java.lang.Integer cannot be cast to clojure.lang.IFn

;; That's Clojure telling you that an integer (the number 2 here) can't be used as a function. You have to prevent the
;; form (2 3) from being treated like a function call -- which is exactly what quote is for:
(cons 1 (quote (2 3)))
;;=> (1 2 3)

;; In other Lisps, this need is so common that they provide a shortcut: a single quote. Although this is used less in
;; Clojure, it's still provided. The previous example can also be written as follows:
(cons 1 '(2 3))
;;=> (1 2 3)

;; And look at that: one less pair of parentheses -- always welcome in a Lisp. Remember, though, that quote affects all
;; of its argument, not just the top level. Even though it worked in the preceding examples to replace [] with '(),
;; this may not always give the results you want:
[1 (+ 2 3)]   ;=> [1 5]
'(1 (+ 2 3))  ;=> (1 (+ 2 3))

;; Finally, note that the empty list () already evaluates to itself; it doesn't need to be quoted. Quoting the empty
;; list isn't required in Clojure.


;; Syntax-quote
;; ---------------------------------------------------------------------------------------------------------------------
;; Like quote, the syntax-quote prevents its argument and subforms from being evaluated. Unlike quote, it has a few
;; extra features that make it ideal for constructing collections to be used as code.
;; Syntax-quote is written as a single back-quote (`):
`(1 2 3)
;;=> (1 2 3)

;; It doesn't expand to a simple form like quote, but to whatever set of expressions is required to support the
;; following features.

;; *Symbol Auto-qualification*
;; A symbol can begin with a namespace and a slash (/). These can be called qualified symbols:
clojure.core/map
clojure.set/union
i.just.made.this.up/qux

;; Syntax quote automatically qualifies all unqualified symbols in its argument:
`map
;;=> clojure.core/map

`Integer
;;=> java.lang.Integer

`(map even? [1 2 3])
;;=> (clojure.core/map clojure.core/even? [1 2 3])

;; If the symbol doesn't name a var or class that exists yet, syntax-quote uses the current namespace:
`is-always-right
;;=> user/is-always-right

;; This behavior will come in handy later on, when we discuss macros.
