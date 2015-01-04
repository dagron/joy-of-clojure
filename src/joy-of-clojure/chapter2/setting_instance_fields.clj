;; Setting instance fields
;; ---------------------------------------------------------------------------------------------------------------------
;; Instance fields can be set via the set! function:
(let [origin (java.awt.Point. 0 0)]
  (set! (.-x origin) 15)
  (str origin))
;;=> "java.awt.Point[x=15,y=0]"

;; The first argument to set! is the instance member access form.
