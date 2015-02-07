;; Nil vs false
;; ---------------------------------------------------------------------------------------------------------------------
;; Rarely do you need to differentiate between the two non-truthy values, but if you do, you can use nil? and false?:
(when (nil? nil) "Actually nil, not false")
;;=> "Actually nil, not false"

(when (false? false) "Actually false, not nil")
;;=> "Actually false, not nil"

;; Keeping in mind the basic rule that everything in Clojure is truthy unless it's false or nil is an astonishingly
;; powerful concept, allowing for elegant solutions. Often programming languages have complicated semantics for
;; truthiness, but Clojure manages to avoid those matters nicely.
