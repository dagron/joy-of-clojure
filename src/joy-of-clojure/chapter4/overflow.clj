;; Overflow
;; ---------------------------------------------------------------------------------------------------------------------
;; Integer and long values in Java are subject to overflow errors. When a numeric calculation results in a value that's
;; larger than 32 bits of representation will allow, the bits of storage wrap around. When you're operating in Clojure,
;; overflow isn't an issue for most cases, thanks to promotion. But when you're dealing with numeric operations on
;; primitive types, overflow can occur. Fortunately, in these instances an exception will occur rather than propagating
;; inaccuracies:
(+ Long/MAX_VALUE Long/MAX_VALUE)
;;=> ArithmeticException integer overflow  clojure.lang.Numbers.throwIntOverflow (Numbers.java:1388)

;; Clojure provides a class of unchecked integer and long mathematical operations that assume their arguments are
;; primitive types. These unchecked operations will overflow if given excessively large values:
(unchecked-add Long/MAX_VALUE Long/MAX_VALUE)
;;=> -2

;; You should take care with unchecked operations, because there's no way to detect overflowing values and no reliable
;; way to return from them. Use the unchecked functions only when overflow is desired.
