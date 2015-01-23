;; Truncation
;; ---------------------------------------------------------------------------------------------------------------------
;; Truncation refers to limiting accuracy for a floating-point number based on a defi- ciency in the corresponding
;; representation. When a number is truncated, its precision is limited such that the maximum number of digits of
;; accuracy is bound by the number of bits that can "fit" into the storage space allowed by its representation. For
;; floating-point values, Clojure truncates by default. Therefore, if high precision is required for your floating-point
;; operations, then explicit typing is required, as seen with the use of the M literal in the following:
(let [imadeuapi 3.14159265358979323846264338327950288419716939937M]
  (println (class imadeuapi))
  imadeuapi)
;; java.math.BigDecimal
;;=> 3.14159265358979323846264338327950288419716939937M

(let [butieatedit 3.14159265358979323846264338327950288419716939937]
  (println (class butieatedit))
  butieatedit)
;; java.lang.Double
;;=> 3.141592653589793

;; As we show, the local butieatedit is truncated because the default Java double type is insufficient. On the other
;; hand, imadeuapi uses Clojure's literal notation, a suffix character M, to declare a value as requiring arbitrary
;; decimal representation. This is one possible way to mitigate truncation for an immensely large range of values, but
;; as we'll explore later on, it's not a guarantee of perfect precision.