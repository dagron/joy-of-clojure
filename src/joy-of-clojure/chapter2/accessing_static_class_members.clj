;; Accessing static class members (Clojure only)
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure provides powerful mechanisms for accessing, creating, and mutating Java classes and instances. The trivial
;; case is accessing static class properties:
java.util.Locale/JAPAN
;;=> #<Locale ja_JP>

;; Clojure programmers usually prefer to access static class members using a syntax that's like accessing a
;; namespace-qualified var:
(Math/sqrt 9)
;;=> 3.0

;; The preceding call is to the java.lang.Math#sqrt static method. By default, all classes in the root java.lang package
;; are available for immediate use. ClojureScript doesn't provide access to static members because JavaScript
;; has no such feature.