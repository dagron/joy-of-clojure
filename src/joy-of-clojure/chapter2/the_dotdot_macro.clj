;; The .. macro
;; ---------------------------------------------------------------------------------------------------------------------
;; When working with Java, it’s common practice to chain together a sequence of method calls on the return type of the
;; previous method call:

;; new java.util.Date().toString().endsWith("2014")" /* Java code */

;; Using Clojure’s dot special operator, the following code is equivalent:
(.endsWith (.toString (java.util.Date.)) "2014") ; Clojure code
;;=> true
;; (Depending on when you run this code, you may get false as the answer. To fix that, change the string "2014" to
;; whatever year it happens to be (for example, "2112").

;; Although correct, the preceding code is difficult to read and will only become more so when you lengthen the chain of
;; method calls. To combat this, Clojure provides the .. macro, which can simplify the call chain as follows:

(.. (java.util.Date.) toString (endsWith "2014"))
;;=> true

;; This .. call closely follows the equivalent Java code and is much easier to read. Bear in mind, you may not see ..
;; used often in Clojure code found in the wild, outside the context of macro definitions. Instead, Clojure provides the
;; -> and ->> macros, which can be used similarly to the .. macro but are also useful in non-interop situations;
;; this makes them the preferred method-call facilities in most cases.
