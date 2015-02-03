;; Persistence
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure's collection data types have some unique properties compared to collections in many mainstream languages.
;; Terms such as persistent and sequence come up, and not always in a way that makes their meaning clear. In this
;; section, we'll define their meanings carefully. We'll also briefly examine the topic of algorithmic complexity and
;; Big-O notation as they apply to Clojure collections.

;; The term persistent is particularly problematic because it means something different in other contexts. In the case
;; of Clojure, we believe that a phrase immortalized by Inigo Montoya from the novel and subsequent film The Princess
;; Bride summarizes your likely initial reaction ... "You keep using that word. I do not think it means what you think
;; it means."

;; Although storage to disk may be the more common meaning of persistent today, Clojure uses an older meaning of the
;; word having to do with immutable in-memory collections with specific properties. In particular, a persistent
;; collection in Clojure allows you to preserve historical versions of its state and promises that all versions will
;; have the same update and lookup complexity guarantees. The specific guarantees depend on the collection type, and
;; we'll cover those details along with each kind of collection.

;; Here you can see the difference between a persistent data structure and one that's not by using a Java array:
(def ds (into-array [:willie :barnabas :adam])) ;; into-array can make a Java/JavaScript array out of a vector
(seq ds)
;;=> (:willie :barnabas :adam)

;; This example creates a three-element array of keywords and uses seq to produce an object that displays nicely in the
;; REPL. Any change to the array ds happens in place, thus obliterating any historical version:
(aset ds 1 :quentin) ;; aset sets the value in an array slot
(seq ds)
;;=> (:willie :quentin :adam)

;; But using one of Clojure's persistent data structures paints a different picture:
(def ds [:willie :barnabas :adam])
ds
;;=> [:willie :barnabas :adam]

(def ds1 (replace {:barnabas :quentin} ds))
ds
;;=> [:willie :barnabas :adam]

ds1
;;=> [:willie :quentin :adam]

;; The original vector ds did not change on the replacement of the keyword :barnabas but instead created another vector
;; with the changed value. A natural concern when confronted with this picture of persistence is that a naive
;; implementation would copy the entire collection on each change, leading to slow operations and poor use of memory.
;; Clojure's implementations are instead efficient by sharing structural elements from one version of a persistent
;; structure to another. This may seem magical, but we'll demystify it later. For now it's sufficient to understand that
;; each instance of a collection is immutable and efficient. This fact opens numerous possibilities that wouldn't work
;; for standard mutable collections. One of these is the sequence abstraction.
