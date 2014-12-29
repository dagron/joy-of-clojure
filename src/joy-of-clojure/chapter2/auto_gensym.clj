;; Auto-gensym
;; ---------------------------------------------------------------------------------------------------------------------
;; Sometimes you need a unique symbol, such as for a parameter or let local name. The easiest way to do this inside a
;; syntax-quote is to append a # to the symbol name. This causes Clojure to create a new, unqualified, automatically
;; generated symbol:
`potion#
;;=> potion__17__auto__

;; Sometimes even this isn’t enough, either because you need to refer to the same symbol in multiple syntax-quotes or
;; because you want to capture a particular unqualified symbol. We’ll talk more about this circumstance later.
