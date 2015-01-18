;; Destructuring with a map
;; ---------------------------------------------------------------------------------------------------------------------
;; Perhaps passing a name as a three-part vector wasn’t a good idea in the first place. It might be better stored in
;; a map:
(def guys-name-map
  {:first "Guy" :middle "Lewis" :last "Steele"})

;; But now you can't use a vector to pick it apart. Instead, you use a map:
(let [{first :first middle :middle last :last} guys-name-map]
  (str last, ", " first " " middle))
;;=> "Steele, Guy Lewis"

;; A couple things about this example may jump out at you. One might be that it still seems repetitive -- we'll get to
;; that in a moment.

;; Another might be that the way the keywords are organized looks unusual. The example has its keywords like :first on
;; the right side of each pair, even though the input map had keywords on the left. There are a couple reasons for that.
;; The first is to help keep the pattern of the name on the left getting the value specified by the thing on the right.
;; So the new local first gets the value looked up in the map by the key :first, just as the whole map gets its value
;; from guys-name-map in the earlier def form.

;; The second reason is because it allows you to conjure up other destructuring features by using forms that would
;; otherwise make no sense. Because the item on the left of each pair will be a new local name, it must be a symbol or
;; possibly a nested destructuring form. But one thing it can't be is a keyword, unless the keyword is a specially
;; supported feature such as :keys, :strs, :syms, :as, and :or.

;; We'll discuss the :keys feature first because it nicely handles the repetitiveness we mentioned earlier. It allows
;; you to rewrite your solution like this:
(let [{:keys [first middle last]} guys-name-map]
  (str last ", " first " " middle))
;;=> "Steele, Guy Lewis"

;; So by using :keys instead of a binding form, you're telling Clojure that the next form will be a vector of names that
;; it should convert to keywords such as :first in order to look up their values in the input map. Similarly, if you'd
;; used :strs, Clojure would be looking for items in the map with string keys such as "first", and :syms would indicate
;; symbol keys.

;; The directives :keys, :strs, :syms and regular named bindings can appear in any combination and in any order. But
;; sometimes you'll want to get at the original map -- in other words, the keys you didn't name individually by any of
;; the methods just described. For that, you want :as, which works just like it does with vector destructuring:
(let [{:keys [title first middle last] :or {title "Mr."}} guys-name-map]
  (println title first middle last))
;;Mr. Guy Lewis Steele
;;=> nil

;; All of these map destructuring features also work on lists, a feature that's primarily used by functions so as to
;; accept keyword arguments:
(defn whole-name [& args]
  (let [{:keys [first middle last]} args]
    (str last ", " first " " middle)))

(whole-name :first "Guy" :middle "Lewis" :last "Steele")
;;=> "Steele, Guy Lewis"

;; Note that when defined this way, whole-name isn't called with a map paramter, but rather with arguments that
;; altnerate between keys and values. Using a map to destructure this list of arguments causes the list to first be
;; poured into a map collection before then being destructured as usual.

;; One final technique worth mentioning is associative destructuring. Using a map to defined a number of destructure
;; bindings isn't limited to maps. You can also destructure a vector by providing a map declaring the local names as
;; indices into them, as shown:
(let [{first-thing 0, last-thing 3} [1 2 3 4]]
  [first-thing, last-thing])
;;=> [1 4]