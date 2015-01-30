;; Auto-gensym
;; ---------------------------------------------------------------------------------------------------------------------
;; Keywords are self-evaluating types that are prefixed by one or more colons, as shown next:
:a-keyword
;;=> :a-keyword
::also-a-keyword
;;=> :user/also-a-keyword

;; Knowing how to type a keyword is important, but the purpose of Clojure keywords can sometimes lead to confusion for
;; first-time Clojure programmers, because their analogue isn't often found in other languages. This section attempts
;; to alleviate the confusion and provides some tips for how keywords are typically used.

;; Keywords always refer to themselves, whereas symbols don't. What this means is that the keyword :magma always has the
;; value :magma, whereas the symbol `ruins` may refer to any legal Clojure value or reference. In Clojure code in the
;; wild, keywords are almost always used as map keys:
(def population {:zombies 2700, :humans 9})

(get population :zombies)
;;=> 2700

(println (/ (get population :zombies)
            (get population :humans))
         "zombies per capita")
;;=> 300 zombies per capita

;; The use of the get function does what you might expect. That is, get takes a collection and attempts to look up its
;; keyed value. But there's a special property of keywords that makes this lookup more concise.

;; So far, we've somewhat oversimplified the capabilities of keywords. Although they're self-evaluating, they're also
;; sometimes evaluated as functions. When a keyword appears in the function position, it's treated as a function.
;; Therefore, another important reason to use keywords as map keys is that they're also functions that take a map as an
;; argument to perform lookups of themselves:
(:zombies population)
;;=> 2700

(println (/ (:zombies population)
            (:humans population))
         "zombies per capita")
;;=> 300 zombies per capita

;; Using keywords as map-lookup functions leads to much more concise code.

;; Often, Clojure code uses keywords as enumeration values, such as :small, :medium, and :large. This provides a nice
;; visual delineation in the source code.

;; Because keywords are used often as enumerations, they're ideal candidates for dispatch values for multimethods,
;; which we'll explore in more detail later on.

;; Another common use for keywords is to provide a directive to a function, multimethod, or macro. A simple way to
;; illustrate this is to imagine a simple function pour, shown below, which takes two numbers and returns a lazy
;; sequence of the range of those numbers. But there's also a mode for this function that takes a keyword :toujours,
;; which instead returns an infinite lazy range starting with the first number and continuing "forever."
(defn pour [lb ub]
  (cond
    (= ub :toujours) (iterate inc lb)
    :else (range lb ub)))

;; When called with upper and lower bounds, it returns a range:
(pour 1 10)
;;=> (1 2 3 4 5 6 7 8 9)

;; When called with a keyword argument, it iterates forever:
(pour 1 :toujours)
;;=> ... runs forever

;; An illustrative bonus with pour is that the macro cond uses a directive :else to mark the default conditional case.
;; In this case, cond uses the fact that the keyword :else is truthy; any keyword (or truthy value) would work just as
;; well.

;; Keywords don't belong to any specific namespace, although they may appear to if you start them with two colons rather
;; than only one:
::not-in-ns
;;=> :user/not-in-ns

;; When Clojure sees a double colon, it assumes that the programmer wants a qualified, or prefixed keyword. Because this
;; example doesn't specify an exact prefix, Clojure uses the current namespace name -- in this case, user -- to
;; automatically qualify the keyword. But the prefix portion of the keyword marked as :user/ only looks like it's
;; denoting a namespace, when in fact it's a prefix gathered from the current namespace by the Clojure reader. This may
;; seem like a distinction without a difference -- we'll show how the prefix is arbitrary in relation to existing
;; namespaces. First, you create a new namespace and manually create a prefixed keyword:
(ns another)
:user/in-another
;;=> :user/in-another

;; This example creates a namespace another and a keyword :user/in-another that appears to belong to the preexisting
;; user namespace but in fact is only prefixed to look that way. The prefix on a keyword is arbitrary and in no way
;; associates it with a given namespace as far as Clojure is concerned. You can even create a keyword with a prefix
;; that's not named the same as any existing namespace:
:haunted/name
;;=> :haunted/name

;; You create a keyword :haunted/name, showing that the prefix doesn't have to be an existing namespace name. But the
;; fact that keywords aren't members of any given namespace doesn't mean namespace-qualifying them is pointless.
;; Instead, it's often clearer to do so, especially when a namespace aggregates a specific functionality and its
;; keywords are meaningful in that context.

;; Even though qualified keywords can have any arbitrary prefix, sometimes it's useful to use namespaces to provide
;; special information for keywords. In a namespace named crypto, the keywords ::rsa and ::blowfish make sense as being
;; namespace qualified. Similarly, if you create a namespace aquarium, then using ::blowfish in it is contextually
;; meaningful. Likewise, when adding metadata to structures, you should consider using qualified keywords as keys and
;; directives if their intention is domain oriented. Consider the following code:
(defn do-blowfish [directive]
  (case directive
    :aquarium/blowfish (println "feed the fish")
    :crypto/blowfish   (println "encode the message")
    :blowfish          (println "not sure what to do")))

(ns crypto)
(user/do-blowfish :blowfish)
;;=> not sure what to do

(user/do-blowfish ::blowfish)
;;=> encode the message

(ns aquarium)
(user/do-blowfish :blowfish)
;;=> not sure what to do

(user/do-blowfish ::blowfish)
;;=> feed the fish

;; When switching to different namespaces using ns, you can use the namespace-qualified keyword syntax to ensure that
;; the correct domain-specific code path is executed. Namespace qualification is especially important when you're
;; creating ad-hoc hierarchies and defining multimethods.

;; Symbols in Clojure are roughly analogous to identifiers in many other languages -- words that refer to other things.
;; In a nutshell, symbols are primarily used to provide a name for a given value. But in Clojure, symbols can also be
;; referred to directly, by using the symbol or quote function or the ' special operator. Symbols tend to be discrete
;; entities from one lexical contour (or scope) to another, and often even in a single contour. Unlike keywords, symbols
;; aren't unique based solely on name alone, as you can see in the following:
(identical? 'goat 'goat)
;;=> false

;; identical? returns false in this example because each goat symbol is a discrete object that only happens to share a
;; name and therefore a symbolic representation. But that name is the basis for symbol equality:
(= 'goat 'goat)
;;=> true

;; The identical? function in Clojure only ever returns true when the symbols are the same object:
(let [x 'goat, y x]
  (identical? x y))
;;=> true

;; In the preceding example, x is also a symbol; but when evaluated in the (identical? x y) form, it returns the symbol
;; goat, which is being stored on the runtime call stack. The question arises: why not make two identically named
;; symbols the same object? The answer lies in metadata.