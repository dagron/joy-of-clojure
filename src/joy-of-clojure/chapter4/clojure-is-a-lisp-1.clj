;; Lisp-1
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure is what's known as a Lisp-1, which in simple terms means it uses the same name resolution for function and
;; value bindings. In a Lisp-2 programming language like Common Lisp, these name resolutions are performed differently
;; depending on the context of the symbol, be it in a function-call position or a function-argument position. There are
;; many arguments for and against both Lisp-1 and Lisp-2, but one downside of Lisp-1 bears consideration. Because the
;; same name-resolution scheme is used for functions and their arguments, there's a real possibility of shadowing
;; existing functions with other locals or vars. Name shadowing isn't necessarily bad if done thoughtfully, but if done
;; accidentally it can lead to unexpected and obscure errors. You should take care when naming locals and defining new
;; functions so that name-shadowing complications can be avoided.

;; Because name-shadowing errors tend to be rare, the benefits of a simplified mechanism for calling and passing
;; first-class functions far outweigh the detriments. Clojure's adoption of a Lisp-1 resolution scheme makes for cleaner
;; implementations and therefore highlights the solution rather than muddying the waters with the nuances of symbolic
;; lookup. For example, the best function highlights this perfectly in the way that it takes the greater-than function
;; > and calls it in its body as f:
(defn best [f xs]
  (reduce #(if (f %1 %2) %1 %2) xs))

(best > [1 3 4 2 7 5 3])
;;=> 7

;; A similar function body using a Lisp-2 language requires the intervention of another function (in this case, funcall)
;; responsible for invoking the function explicitly. Likewise, passing any function requires the use of a qualifying tag
;; marking it as a function object, as shown here:

;; This is Common Lisp and NOT Clojure code
(defun best (f xs)
  (reduce #'(lambda (l r)
              (if (funcall f l r) l r))
    xs))
(best #'> '(1 3 4 2 7 5 3))
;;=> 7

;; This section isn't intended to champion the cause of Lisp-1 over Lisp-2, but rather to highlight the differences
;; between the two. Many of the design decisions in Clojure provide succinctness in implementation, and Lisp-1 is no
;; exception. The preference for Lisp-1 versus Lisp-2 typically boils down to matters of style and taste; by all
;; practical measures, they're equivalent.
