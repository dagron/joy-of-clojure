;; Creating mappings with :refer
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure also provides a :refer directive that works almost exactly like the option of the same name in the :require
;; directive, except that it only creates mappings for libraries that have already been loaded:
(ns joy.yet-another
  (:refer joy.ch2))
(report-ns)
;;=> "The current namespace is joy.yet-another"

;; Using :refer this way creates a mapping from the name report-ns to the actual function located in the namespace
;; joy.ch2 so that the function can be called normally. You can also set an alias for the same function using the
;; :rename keyword taking a map, as shown here:
(ns joy.yet-another
  (:refer clojure.set :rename {union onion}))

(onion #{1 2} #{4 5})
;;=> #{1 2 4 5}

;; Note that :rename also works with the :require directive.
