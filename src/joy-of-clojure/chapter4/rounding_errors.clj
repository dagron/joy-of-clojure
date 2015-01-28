;; Rounding errors
;; ---------------------------------------------------------------------------------------------------------------------
;; When the representation of a floating-point value isn't sufficient for storing its actual value, then rounding errors
;; will occur. Rounding errors are an especially insidious numerical inaccuracy, because they have a habit of
;; propagating throughout a computation and/or build over time, leading to difficulties in debugging. There's a famous
;; case involving the failure of a Patriot missile caused by a rounding error, resulting in the death of 28 U.S.
;; soldiers in the first Gulf War. This occurred due to a rounding error in the representation of a count register's
;; update interval. The timer register was meant to update once every 0.1 seconds, but because the hardware couldn't
;; represent 0.1 directly, an approximation was used instead. Tragically, the approximation used was subject to rounding
;; error. Therefore, over the course of 100 hours, the rounding accumulated into a timing error of approximately 0.34
;; seconds:
(let [approx-interval (/ 209715 2097152)                  ;; <-- Patriot's approx. 0.1
      actual-interval (/ 1 10)                            ;; <-- Clojure can accurately represent 0.1
      hours           (* 3600 100 10)
      actual-total    (double (* hours actual-interval))
      approx-total    (double (* hours approx-interval))]
  (- actual-total approx-total))
;;=> 0.34332275390625

;; In the case of the Patriot missile, the deviation of 0.34 seconds was enough to cause a catastrophic software error,
;; resulting in the missile's ineffectiveness. When human lives are at stake, the inaccuracies wrought from rounding
;; errors are unacceptable. For the most part, Clojure can maintain arithmetic accuracies in a certain range, but you
;; shouldn't take for granted that such will be the case when interacting with Java libraries.

;; One way to contribute to rounding errors is to introduce doubles and floats into an operation. In Clojure, any
;; computation involving even a single double results in a value that's a double:
(+ 0.1M 0.1M 0.1M 0.1 0.1M 0.1M 0.1M 0.1M 0.1M 0.1M)
;;=> 0.9999999999999999

;; Can you spot the double?

;; This discussion was Java-centric, but Clojure's ultimate goal is to be platform agnostic, and the problem of
;; numerical consistency across platforms is a nontrivial matter. It's still unknown whether the preceding points will
;; be universal across host platforms, so bear in mind that they should be reexamined when using Clojure outside the
;; context of the JVM.
