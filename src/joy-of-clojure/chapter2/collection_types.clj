;; Lists
;; ---------------------------------------------------------------------------------------------------------------------
;; Lists are the classic collection type in Lisp (the name comes from list processing, after all) languages,
;; and Clojure is no exception. Literal lists are written with parentheses:
(yankee hotel foxtrot)

;; When a list is evaluated, the first item of the list -- yankee in this case -- is resolved to a function, macro,
;; or special operator. If yankee is a function, the remaining items in the list are evaluated in order, and the
;; results are passed to yankee as its parameters.
;; NOTE A form is any Clojure object meant to be evaluated, including but not limited to lists, vectors, maps, numbers,
;; keywords, and symbols. A special form is a form with special syntax or special evaluation rules that typically aren't
;; implemented using the base Clojure forms. An example of a special form is the . (dot) operator used for Java
;; interoperability purposes. If yankee is a macro or special operator, the remaining items in the list aren't
;; necessarily evaluated, but are processed as defined by the macro or operator.

;; Lists can contain items of any type, including other collections. Here are some more examples:
(1 2 3 4)
()
(:fred ethel)
(1 2 (a b c) 4 5)

;; Note that unlike in some Lisps, the empty list in Clojure, written as (), isn't the same as nil.


;; Vectors
;; ---------------------------------------------------------------------------------------------------------------------
;;  Like lists, vectors store a series of values. Several differences are described in section 5.4, but for now only two
;; are important. First, vectors have a literal syntax using square brackets:
[1 2 :a :b :c]

;; The other important difference is that vectors evaluate each item in order. No function or macro call is performed on
;; the vector itself, although if a list appears within the vector, that list is evaluated following the normal rules
;; for a list. Like lists, vectors are type heterogeneous; and as you might guess,
;; the empty vector [] isn't the same as nil.


;; Maps
;; ---------------------------------------------------------------------------------------------------------------------
;; Maps store unique keys and one value per key -- similar to what some languages and libraries call dictionaries
;; or hashes. Clojure has several types of maps with different properties, but don't worry about that for now. Maps can
;; be written using a literal syntax with alternating keys and values inside curly braces. Commas are frequently used
;; between pairs, but they're whitespace as they are everywhere else in Clojure:
{1 "one", 2 "two", 3 "three"}

;; As with vectors, every item in a map literal (each key and each value) is evaluated before the result is stored
;; in the map. Unlike with vectors, the order in which they're evaluated isn't guaranteed. Maps can have items of any
;; type for both keys and values, and the empty map {} isn't the same as nil.


;; Sets
;; ---------------------------------------------------------------------------------------------------------------------
;; Sets store zero or more unique items. They're written using curly braces with a leading hash:
#{1 2 "three" :four 0x5}

;; Again, the empty set #{} isn't the same as nil.
