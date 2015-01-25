;; Promotion
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure is able to detect when overflow occurs, and it promotes the value to a numerical representation that can
;; accommodate larger values. In many cases, promotion results in the usage of a pair of classes used to hold
;; exceptionally large values. This promotion in Clojure is automatic, because the primary focus is first correctness of
;; numerical values, then raw speed. It's important to remember that this promotion will occur, as shown in the
;; following snippet, and your code should accommodate this certainty:
(def clueless 9)

(class clueless)                          ;; <-- Long by default
;;=> java.lang.Long

(class (+ clueless 9000000000000000))     ;; <-- Long can hold large values
;;=> java.lang.Long

(class (+ clueless 90000000000000000000)) ;; <-- But when too large, the type promotes to BigInt
;;=> clojure.lang.BigInt

(class (+ clueless 9.0))                  ;; <-- Floating-point doubles are contagious
;;=> java.lang.Double

;; Java has a bevy of contexts under which automatic type conversion occurs, so we advise you to familiarize yourself
;; with them when dealing with Java native libraries.
