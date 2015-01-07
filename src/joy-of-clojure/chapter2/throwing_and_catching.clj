;; Throwing and catching
;; ---------------------------------------------------------------------------------------------------------------------
;; We'll now talk briefly about Clojure's facilities for handling exceptions. Like Java, Clojure provides a couple of
;; forms for throwing and catching runtime exceptions: throw and catch, respectively. Although throw and catch map
;; almost directly down to Java and JavaScript, they're considered the standard way of dealing with error handling.
;; In other words, even in the absence of interoperability, most Clojure code uses throw and catch to perform
;; error handling.

;; The mechanism to throw an exception is fairly straightforward:
(throw (Exception. "I done throwed"))
;;=> java.lang.Exception: I done throwed ...

;; The syntax for catching exceptions in Clojure is similar to that of Java:
(defn throw-catch [f]
  [(try
     (f)
     (catch ArithmeticException e "No dividing by zero!")
     (catch Exception e (str "You are so bad " (.getMessage e)))
     (finally (println "returning... ")))])
(throw-catch #(/ 10 5))
;; returning...
;;=> [2]

(throw-catch #(/ 10 0))
;; returning...
;;=> ["No dividing by zero!"]

(throw-catch #(throw (Exception. "Crybaby")))
;; returning...
;;=> ["You are so bad Crybaby"]

;; The major difference between how Java and Clojure handle exceptions is that Clojure doesn't adhere to
;; checked-exception requirements. The ClojureScript catch form looks similar except for the need to use js to catch
;; core error types, as shown next:
(try
  (throw (Error. "I done throwed in CLJS"))
  (catch js/Error err "I done catched in CLJS"))
;;=> "I done catched in CLJS"

