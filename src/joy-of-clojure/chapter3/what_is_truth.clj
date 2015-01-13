;; What is truth?
;; ---------------------------------------------------------------------------------------------------------------------
;; Every value looks like true to if, except for false and nil. That means values some languages treat as false --
;; zero-length strings, empty lists, the number zero, and so on -- are all treated as true in Clojure:
(if true :truthy :falsey)   ;;=> truthy
(if [] :truthy :falsey)     ;;=> truthy
(if () :truthy :falsey)     ;;=> truthy
(if {} :truthy :falsey)     ;;=> truthy
(if #{} :truthy :falsey)    ;;=> truthy
(if "" :truthy :falsey)     ;;=> truthy
(if 0 :truthy :falsey)      ;;=> truthy

(if nil :truthy :falsey)    ;;=> falsey
(if false :truthy :falsey)  ;;=> falsey

;; This may feel uncomfortable to you, depending on your background. But because branches in a program's logic are
;; already one of the most likely places for complexity and bugs, Clojure has opted for a simple rule. There's no need
;; to check a class's definition to see if it acts like "false" when you think it should (as is sometimes required in
;; Python, for example). Every object is "true" all the time, unless it's nil or false.
