;; Symbols and Namespaces
;; ---------------------------------------------------------------------------------------------------------------------
;; Like keywords, symbols don't belong to any specific namespace. Take, for example, the following code:
(ns where-is)
(def a-symbol 'where-am-i)
a-symbol
;;=> where-am-i

(resolve 'a-symbol)
;;=> #"where-is/a-symbol

`a-symbol
;;=> where-is/a-symbol

;; The initial evaluation of a-symbol shows the expected value where-am-i. But attempting to resolve the symbol using
;; resolve and using syntax-quote returns what looks like (as printed at the REPL) a namespace-qualified symbol. This is
;; because a symbol's qualification is a characteristic of evaluation and not necessarily inherent in the symbol.
;; This also applies to symbols qualified with class names. This evaluation behavior will prove beneficial when we
;; discuss macros.

