;; Unquote-splicing
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure provides a handy feature to solve exactly the problem posed earlier. A variant of unquote called
;; unquote-splicing works similarly to unquote, but a little differently:
(let [x '(2 3)] `(1 ~@x))
;;=> (1 2 3)

;; Note the @ in ~@, which tells Clojure to unpack the sequence x, splicing it into the resulting list rather than
;; inserting it as a nested list.

;; Note that unquote-splicing does the right thing even when the container is a different type:
(let [x '(3 4)] `[1 2 ~@x])
;;=> [1 2 3 4]

(let [x [:c :d]] `(:a :b ~@x))
;;=> (:a :b :c :d)

(let [x {:c 2 :d 3}] `(:a :b ~@x))
;;=> (:a :b [:c 2] [:d 3])

(let [x #{2 3}] `(3 3 ~@x))
;;=> (3 3 2 3)

(let [x '(4 4)] `#{2 3 ~@x})
;;=> #{2 3 4}