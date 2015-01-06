;; The doto macro
;; ---------------------------------------------------------------------------------------------------------------------
;; When working with Java, it's also common to initialize a fresh instance by calling a set of mutators:

;; // This is Java, not Clojure
;; java.util.HashMap props = new java.util.HashMap();
;; props.put("HOME", "/home/me");        /* More java code. Sorry. */
;; props.put("SRC",  "src");
;; props.put("BIN",  "classes");

;; But using this method is overly verbose and can be streamlined using the doto macro, which takes the form
(doto (java.util.HashMap.)
  (.put "HOME" "/home/me")
  (.put "SRC"  "src")
  (.put "BIN"  "classes"))
;;=> {"HOME" "/home/me", "BIN" "classes", "SRC" "src"}

;; These kinds of Java and Clojure comparisons are useful for understanding Clojure’s interoperability offerings.
