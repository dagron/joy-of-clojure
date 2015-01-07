;; Creating namespaces using ns
;; ---------------------------------------------------------------------------------------------------------------------
;; To create a new namespace, you can use the ns macro:
(ns joy.ch2)

;; Your REPL prompt now displays:
;; joy.ch2=>

;; This prompt shows that you're working in the context of the joy.ch2 namespace. Clojure also provides a var *ns* that
;; refers to the current namespace. Any var created is a member of the current namespace:
(defn hello []
  (println "Hello Cleveland!"))
(defn report-ns []
  (str "The current namespace is " *ns*))
(report-ns)

;;=> "The current namespace is joy.ch2"

;; Entering a symbol in a namespace causes Clojure to attempt to look up its value in the current namespace:
;; joy.ch2=> hello
;; #<ch2$hello joy.ch2$hello@26b61de0>

;; You can create new namespaces at any time:
(ns joy.another)

;; joy.another=>

;; Again, notice that the prompt has changed, indicating that the new context is joy.another. By using the ns form, you
;; tell Clojure to create another namespace and switch over to it. Because the joy.another namespace is new and not
;; nested inside of nor in any way part of the previously created joy.ch2 namespace, attempting to run report-ns will no
;; longer work:
(report-ns)
;;=> CompilerException java.lang.RuntimeException: Unable to resolve symbol: report-ns in this context,

;; This is because report-ns exists in the joy.ch2 namespace and is only accessible via its fully qualified name,
;; joy.ch2/report-ns. Any namespaces referenced must already be loaded implicitly by being previously defined or by
;; being one of Clojure's core namespaces, or explicitly loaded through the use of :require, which we'll discuss next.
