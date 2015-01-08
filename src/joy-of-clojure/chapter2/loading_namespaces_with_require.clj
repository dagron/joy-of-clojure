;; Loading other namespaces with :require
;; ---------------------------------------------------------------------------------------------------------------------
;; Creating a namespace at the REPL is straightforward. But just because you've created one and populated it with useful
;; functions doesn't mean its awesomeness is available for use by any other namespace you create. Instead, in order
;; to use functions from any given namespace, you need to load it from disk. But how do you load namespaces?
;; Clojure provides the convenience directive :require to take care of this task. Here’s an example:
(ns joy.req
  (:require clojure.set))

(clojure.set/intersection #{1 2 3} #{3 4 5})
;;=> #{3}

;; Using :require indicates that you want the clojure.set namespace loaded. You can also use the :as directive to create
;; an additional alias to clojure.set:
(ns joy.req-alias
  (:require [clojure.set :as s]))

(s/intersection #{1 2 3} #{3 4 5})
;;=> #{3}

;; The qualified namespace form looks the same as a call to a static class method. The difference is that a namespace
;; symbol can only be used as a qualifier, whereas a class symbol can also be referenced independently:

clojure.set
;;=> java.lang.ClassNotFoundException: clojure.set

java.lang.Object
;; java.lang.Object

;; The vagaries of namespace mappings from symbols to vars, both qualified and unqualified, can cause confusion between
;; class names and static methods in the beginning, but the differences will begin to feel natural as you progress.
;; In addition, Clojure code in the wild tends to use my.Class and my.ns for naming classes and namespaces,
;; respectively, to help eliminate potential confusion.
