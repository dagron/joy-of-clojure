;; Blocks
;; ---------------------------------------------------------------------------------------------------------------------
;; Use the do form when you have a series or block of expressions that need to be treated as one. All the expressions
;; are evaluated, but only the last one is returned:
(do
  (def x 5)
  (def y 4)
  (+ x y)
  [x y])
;;=> [5 4]

;; The expressions (def x 5), (def y 4), and (+ x y) are executed one by one in the body of the do block. Even the
;; addition (+ x y) is executed, but the value is thrown away—only the final expression [x y] is returned. The middle
;; bits of the do block are typically where the side effects occur, as shown with the use of def. Whenever you see a
;; Clojure form with a name starting with do, you can assume that its purpose is related to side-effectful activities
;; like defining a var, printing, and so on.

