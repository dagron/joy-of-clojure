;; Putting it all together: a lazy quicksort
;; ---------------------------------------------------------------------------------------------------------------------
;; In a time when the landscape is rife with new programming languages and pregnant with more, it seems inconceivable
;; that the world would need another quicksort implementation. Inconceivable or not, we won't be deterred from adding
;; yet another to the rich ecosystem of pet problems. This implementation of quicksort differs from many in a few key
;; ways. First, you'll implement a lazy, tail-recursive version. Second, you'll split the problem such that it can be
;; executed incrementally. Only the calculations required to obtain the part of a sequence desired will be calculated.
;; This will illustrate the fundamental reason for laziness in Clojure: the avoidance of full realization of interim
;; results.

;; The implementation
;; ------------------
;; Without further ado, we present our quicksort implementation, starting with a simple function named rand-ints that
;; generates a seq of pseudo-random integers sized according to its given argument n with numbers ranging from zero to
;; n, inclusive:
(ns joy.q)

(defn rand-ints [n]
  (take n (repeatedly #(rand-int n))))

;; The rand-ints function works as you might expect:
(rand-ints 10)
;;=> (7 0 3 1 8 5 3 8 9 3)

;; The bulk of this lazy quicksort implementation is as shown in the following listing.

;; Lazy, tail-recursive, incremental quicksort
;; -------------------------------------------
(defn sort-parts [work]
  (lazy-seq
    (loop [[part & parts] work]                             ; Pull apart work
      (if-let [[pivot & xs] (seq part)]
        (let [smaller? #(< % pivot)]                        ; Define pivot comparison fn
          (recur (list*
                   (filter smaller? xs)                     ; Work all < pivot
                   pivot                                    ; Work the pivot itself
                   (remove smaller? xs)                     ; Work all > pivot
                   parts)))                                 ; Concat parts
        (when-let [[x & parts] parts]
          (cons x (sort-parts parts)))))))                  ; Sort the rest if more parts

;; The key detail in the preceding code is that sort-parts works not on a plain sequence of elements but on a carefully
;; constructed list that alternates between lazy seqs and pivots. Every element before each pivot is guaranteed to be
;; less than the pivot, and everything after will be greater, but the sequences between the pivots are as yet unsorted.
;; When qsort is given an input sequence of numbers to sort, it creates a new work list consisting of just that input
;; sequence and passes this work to sort-parts. The loop inside sort-parts pulls apart the work, always assuming that
;; the first item, which it binds to part, is an unsorted sequence. It also assumes that if there is a second item,
;; which will be at the head of parts, then that item is a pivot. It recurs on the sequence at the head of work,
;; splitting out pivots and lazy seqs until the sequence of items less than the most recent pivot is empty, in which
;; case the if-let test is false, and that most recent pivot is returned as the first item in the sorted seq. The rest
;; of the built-up list of work is held by the returned lazy sequence to be passed into sort-parts again when subsequent
;; sorted items are needed.

;; You can see a snapshot of the work list for the function call (qsort [2 1 4 3]) in the figure below, at an
;; intermediate point in its process.

;; The qsort function shown earlier uses a structure like this for its work list when sorting the vector [2143].
;; Note that all the parts described by a standard quicksort implementation are represented here.

;;         Greater than pivot
;;                 |
;;          Pivot  |
;;            |  +--+
;;           \|/ |  |
;;       (1  (2  4  3)
;;       /|\
;;        |
;; Less than pivot

;; You can see a snapshot of the work list for the function call (qsort [2 1 4 3]) in the figure above, at an
;; intermediate point in its process.

;; The figure includes the characteristics of a standard quicksort implementation, which you can finalize with another
;; function that starts sort-parts running properly:
(defn qsort [xs]
  (sort-parts (list xs)))

;; You can run qsort as follows to sort a given sequence:
(qsort [2 1 4 3])
;;=> (1 2 3 4)

(qsort (rand-ints 20))
;;=> (1 2 6 6 7 8 9 10 10 12 13 13 14 15 16 17 17 18 18 19)

;; The implementation of the sort-parts function works to provide an incremental solution for a lazy quicksort. This
;; incremental approach stands in opposition to a monolithic approach defined by its performance of the entire
;; calculation when any segment of the sequence is accessed. For example, grabbing the first element in a lazy sequence
;; returned from qsort performs only the necessary calculations required to get that first item:
(first (qsort (rand-ints 100)))
;;=> 0

;; Of course, the number returned here will likely be different in your REPL, but the underlying structure of the lazy
;; sequence used internally by sort-parts will be similar to that shown in the figure below.

;; Internal structure of qsort. Each filter and remove lazily returns items from its parent sequence only as required.
;; So, to return the first two items of the seq returned by qsort, no remove steps are required from either level A or
;; level B. To generate the sequence (4), a single remove step at level B is needed to eliminate everything less than 3.
;; As more items are forced from the seq returned by qsort, more of the internal filter and remove steps are run.
;;
;;               (5      3      1      7      4      2      8      6)
;;                |      |                                         |
;;                |      +----------------------+------------------+
;;      A         +---------------------------+ |
;;                              Filter        | |    Remove
;;                          +-----------------|-+-----------+
;;                          |                 |             |
;;                +--------------------+      |      +-------------+
;;                |                    |     \|/     |             |
;;               (3      1      4      2)     5     (7      8      6)
;;                |      |             |      |      |      |      |
;;                |      +------+------+      |      |      +---+--+
;;                |    Filter   |   Remove    |      +------+   |
;;                |   +---------+------+      |      Filter |   | Remove
;;                +---|---------+      |      |      +------|---+--+
;;                    |         |      |      |      |      |      |
;;                +------+      |     +-+     |     +-+     |     +-+
;;                |      |     \|/    | |    \|/    | |    \|/    | |
;;               (1      2)     3     (4)     5     (6)     7     (8)
;;                  /|\                                           /|\
;;                   |                                             |
;;          +--------+---------+                          +--------+---------+
;;          |(filter #(< % 3)  |                          |(remove #(< % 7)  |
;;          |  (filter #(< % 5)|                          |  (remove #(< % 5)|
;;          |    xs))          |                          |    xs))          |
;;          +------------------+                          +------------------+

;; The lazy qsort can gather the first element because it takes only some small subset of comparisons to gather the
;; numbers into left-side smaller and right-side larger partitions and sort those smaller pieces only. The
;; characteristic of the quicksort algorithm is especially conducive to laziness, because it's fairly cheap to make and
;; shuffle partitions where those with a smaller magnitude can be shuffled first. What then are the benefits of a lazy,
;; tail-recursive, incremental quicksort? The answer is that you can take sorted portions of a large sequence without
;; having to pay the cost of sorting its entirety, as the following command hints:
(take 10 (qsort (rand-ints 10000)))
;=> (0 0 0 0 1 3 4 6 8 10)

;; On our machines, this command required roughly 11,000 comparisons, which for all intents and purposes is an O(n)
;; operation —- an order of magnitude less than the quicksorts's best case. Bear in mind that as the take value gets
;; closer to the actual number of elements, this difference in asymptotic complexity will shrink. But it's a reasonably
;; efficient way to determine the smallest n values in a large unsorted sequence, especially given that it doesn't sort
;; its elements in place.

