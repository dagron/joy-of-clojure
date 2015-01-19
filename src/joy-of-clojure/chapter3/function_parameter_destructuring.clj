;; Destructuring in function parameters
;; ---------------------------------------------------------------------------------------------------------------------
;; All of the same features of destructuring that are possible in let forms are also available in function parameters.
;; Each function parameter can destructure a map or sequence:
(defn print-last-name [{:keys [last]}]
  (println last))

(print-last-name guys-name-map)
;; Steele
;;=> nil

;; Note that function arguments can include an ampersand as well, but this isn't the same as destructuring. Instead,
;; that's part of their general support for multiple function bodies, each with its own number of parameters.
