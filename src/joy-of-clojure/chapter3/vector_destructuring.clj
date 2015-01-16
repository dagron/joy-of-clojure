;; Destructuring with a vector
;; ---------------------------------------------------------------------------------------------------------------------
;; You've heard that the rolodex project has been overdue, but now every developer assigned to it is out sick. The QA
;; team is ready to go, but one function is still missing, and it's a show-stopper. You're told to drop everything and
;; write the function ASAP.

;; The design? Take a vector of length 3 that represents a person's first, middle, and last names, and return a string
;; that will sort in the normal way, like "Steele, Guy Lewis". What are you waiting for? Why aren't you done yet?!?!
(def guys-whole-name ["Guy" "Lewis" "Steel"])

(str (nth guys-whole-name 2) ", "
     (nth guys-whole-name 0) " "
     (nth guys-whole-name 1))

;; Alas, by the time you've finished typing guys-whole-name for the fourth time, it's too late. The customers have
;; canceled their orders, and the whole department has been downsized.
;; If only you'd known about destructuring.
;; OK, so you're not likely to lose your job because your function is twice as many lines as it needs to be, but still,
;; that's a lot of code repeated in a pretty small function. And using index numbers instead of named locals makes the
;; purpose of the function more obscure than necessary.
;; Destructuring solves both these problems by allowing you to place a collection of names in a binding form where
;; normally you'd put a single name. One kind of binding form is the list of parameters given in a function definition.

;; Let's try that again but use destructuring with let to create more convenient locals for the parts of Guy's name:
(let [[first middle last] guys-whole-name]
     (str last ", " first " " middle))

;; This is the simplest form of destructuring, where you want to pick apart a sequential thing (a vector of strings in
;; this case, although a list or other sequential collection would work as well), giving each item a name.
;; You don't need it here, but you can also use an ampersand in a destructuring vector to indicate that any remaining
;; values of the input should be collected into a (possibly lazy) seq:
(let [[a b c & more] (range 10)]
     (println "a b c are:" a b c)
     (println "more is:" more))
;; a b c are: 0 1 2
;; more is: (3 4 5 6 7 8 9)
;;=> nil

;; Here the locals a, b, and c are created and bound to the first three values of the range. Because the next symbol is
;; an ampersand, the remaining values are made available as a seq bound to more.

;; The final feature of vector destructuring is :as, which can be used to bind a local to the entire collection. It must
;; be placed after the & local, if there is one, at the end of the destructuring vector:
(let [range-vec (vec (range 10))
      [a b c & more :as all] range-vec]
   (println "a b c are:" a b c)
   (println "more is:" more)
   (println "all is:" all))
;; a b c are: 0 1 2
;; more is: (3 4 5 6 7 8 9)
;; all is: [0 1 2 3 4 5 6 7 8 9]
;;=> nil

;; range-vec is a vector in this example, and the directive :as binds the input collection as is, entirely unmolested,
;; so that the vector stays a vector. This is in contrast to &, which binds more to a seq, not a vector.