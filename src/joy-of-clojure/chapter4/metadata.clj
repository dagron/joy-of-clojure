;; Metadata
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure lets you attach metadata to various objects, but for now we'll focus on attaching metadata to symbols. The
;; with-meta function takes an object and a map and returns another object of the same type with the metadata attached.
;; Equally named symbols often aren't the same instance because each can have its own unique metadata:
(let [x (with-meta 'goat {:ornery true})
      y (with-meta 'goat {:ornery false})]
  [(= x y)
   (identical? x y)
   (meta x)
   (meta y)])
;;=> [true false {:ornery true} {:ornery false}]

;; The two locals x and y both hold an equal symbol 'goat, but they're different instances, each containing separate
;; metadata maps obtained with the meta function. So you see, symbol equality depends on neither metadata nor identity.
;; This equality semantic isn't limited to symbols but is pervasive in Clojure, as we'll demonstrate throughout this
;; repository. You'll find that keywords can't hold metadata because any equally named keyword is the same object.

