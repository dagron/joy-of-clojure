;; Creating instances
;; ---------------------------------------------------------------------------------------------------------------------
;; Creating instances is likewise a trivial matter with Clojure. The new special operator closely mirrors the Java and
;; JavaScript models:
(new java.awt.Point 0 1)
;;=> #<Point java.awt.Point[x=0,y=1]>

;; This example creates an instance of java.awt.Point with the numbers 0 and 1 passed in as constructor arguments.
;; An interesting aspect of Clojure is that its core collection types can be used as arguments to Java constructors for
;; the purpose of initialization. Observe how you can use a Clojure map to initialize a Java map:

(new java.util.HashMap {"foo" 42 "bar" 9 "baz" "quux"})
;;=> {"baz" "quux", "foo" 42, "bar" 9}

;; The second, more succinct, Clojure form to create instances is the preferred form:
(java.util.HashMap. {"foo" 42 "bar" 9 "baz" "quux"})
;;=> {"baz" "quux", "foo" 42, "bar" 9}

;; As you can see, the class name is followed by a dot in order to signify a constructor call. The same capability
;; exists in ClojureScript, except that when referencing core or globally accessible JavaScript types, you need
;; to prefix the type with the js namespace symbol:
(js/Date.)
;;=> #inst "2014-12-28T11:03:727-00:00"

;; There are subtle differences like this (although relatively few) between Clojure and ClojureScript, and as needed
;; we'll highlight and explain them.