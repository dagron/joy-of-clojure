;; Loading java classes with :import
;; ---------------------------------------------------------------------------------------------------------------------
;; To use unqualified Java classes in any given namespace, you should import them via the :import directive:
(ns joy.java
  (:import [java.util HashMap]
           [java.util.concurrent.atomic AtomicLong]))

(HashMap. {"happy?" true})
;; {"happy?" true}

(AtomicLong. 42)
;; 42

;; But fully qualified Java class names are always available without any import. Finally, any classes in the Java
;; java.lang package are automatically imported when namespaces are created. We'll discuss namespaces in more detail
;; later on.
