;; Accessing instance fields with the dot operator
;; ---------------------------------------------------------------------------------------------------------------------
;; To access public instance variables, precede the field name with a dot and a hyphen:
(.-x (java.awt.Point. 10 20))
;;=> 10

;; This returns the value of the field x from the Point instance given. To access instance methods, the dot form allows
;; an additional argument to be passed to the method:
(.divide (java.math.BigDecimal. "42") 2M)
;=> 21M

;; This example calls the #divide method on the class BigDecimal. Notice that the instance you're accessing is
;; explicitly given as the first argument to the .divide call. This inverts the way that Java instance calls happen,
;; where the instance is the implicit method target. Note that the example also uses the 2M literal to denote that you
;; want to use an arbitrarily precise numeric value.
