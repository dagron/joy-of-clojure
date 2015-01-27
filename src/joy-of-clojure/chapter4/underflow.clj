;; Underflow
;; ---------------------------------------------------------------------------------------------------------------------
;; Underflow is the inverse of overflow, where a number is so small that its value collapses into zero. Here are simple
;; examples of underflow for floats and doubles:
(float 0.0000000000000000000000000000000000000000000001)
;;=> 0.0

1.0E-430
;;=> 0.0

;; Underflow presents a danger similar to overflow, except that it occurs only with floating-point numbers.