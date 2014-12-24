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


;; Locals
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure doesn't have local variables, but it does have locals; they just can't vary. Locals are created and their
;; scope is defined using a let form, which starts with a vector that defines the bindings, followed by any number
;; of expressions that make up the body. The vector begins with a binding form (usually a symbol), which is the name
;; of a new local. This is followed by an expression whose value is bound to this new local for the remainder of
;; the let form. You can continue pairing binding names and expressions to create as many locals as you need.
;; All of them are available in the body of the let:
(let [r         5
      pi        3.1415
      r-squared (* r r)]
  (println "radius is" r)
  (* pi r-squared))
;; radius is 5
;;=> 78.53750000000001
