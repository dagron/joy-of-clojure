;; Loading and creating mappings with :refer
;; ---------------------------------------------------------------------------------------------------------------------
;; Sometimes you'll want to create mappings from vars in another namespace to names in your own, in order to avoid
;; calling each function or macro with the qualifying namespace symbol. To create these unqualified mappings, Clojure
;; provides the :refer option of the :require directive:
(ns joy.use-ex
  (:require [clojure.string :refer (capitalize)]))
(map capitalize ["kilgore" "trout"])
;;=> ("Kilgore" "Trout")

;; The :refer option indicates that only the function capitalize should be mapped in the namespace joy.use-ex. Although
;; you can bring in all public vars from the specified namespace by using :refer :all (or the older :use directive),
;; we don't generally recommend these. Explicitly specifying the vars you'd like to refer is good practice in Clojure,
;; because it avoids creating unnecessary names in a namespace. Unnecessary names increase the odds of name clashes and
;; make it more difficult for people reading your code to discover the definitions of the vars you use.
