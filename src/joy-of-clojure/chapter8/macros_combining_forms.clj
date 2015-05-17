;; Macros combining forms
;; ---------------------------------------------------------------------------------------------------------------------
;; Macros are often used for combining a number of forms and actions into one consistent view. You saw this behavior in
;; the previous section with the do-until macro, but it's more general. In this section, we'll show how you can use
;; macros to combine a number of tasks in order to simplify an API. Clojure's defn macro is an instance of this type of
;; macro because it aggregates the processes of creating a function, including the following:

;; * Creating the corresponding function object using fn
;; * Checking for and attaching a documentation string
;; * Building the :arglists metadata
;; * Binding the function name to a var
;; * Attaching the collected metadata

;; You could perform all these steps over and over again every time you wanted to create a new function, but thanks to
;; macros you can instead use the more convenient defn form. Regardless of your application domain and its
;; implementation, programming language boilerplate code inevitably occurs and is a fertile place to hide subtle errors.
;; But identifying these repetitive tasks and writing macros to simplify and reduce or eliminate the tedious
;; copy-paste-tweak cycle can work to reduce the incidental complexities inherent in a project. Where macros differ from
;; techniques familiar to proponents of Java's object-oriented style—including hierarchies, frameworks, inversion of
;; control, and the like -- is that they're treated no differently by the language itself. Clojure macros work to mold
;; the language into the problem space rather than force you to mold the problem space into the constructs of the
;; language. There's a specific term for this, domain-specific language, but in Lisp the distinction between DSL and API
;; is thin to the point of transparency.

;; Envision a scenario where you want to be able to define vars that call a function whenever their root bindings
;; change. You could do this using the add-watch function, which lets you attach a watcher to a reference type that's
;; called whenever a change occurs within. The add-watch function takes three arguments: a reference, a watch function
;; key, and a watch function called whenever a change occurs. You could enforce that every time someone wanted to define
;; a new var, they would have to follow these steps:

;; 1 Define the var.
;; 2 Define a function (maybe inline to save a step) that will be the watcher.
;; 3 Call add-watch with the proper values.

;; A meager three steps isn't too cumbersome a task to remember in a handful of uses, but over the course of a large
;; project it's easy to forget and/or morph one of these steps when the need to perform them many times occurs.
;; Therefore, perhaps a better approach is to define a macro to perform all these steps for you, as the following
;; definition does:
(defmacro def-watched [name & value]
  `(do
     (def ~name ~@value)
     (add-watch (var ~name)
                :re-bind
                (fn [~'key ~'r old# new#]
                  (println old# " -> " new#)))))

;; Ignoring symbol resolution and auto-gensym, which we'll cover in upcoming sections, the macro called as
;; (def-watched x 2) expands into roughly the following:
(do (def x 2)
    (add-watch (var x)
               :re-bind
               (fn [key r old new]
                 (println old " -> " new))))

;; The results of def-watched are thus
(def-watched x (* 12 12))
x
;;=> 144

(def x 0)
;; 144 -> 0
;;=> #'user/x

;; Lisp programs in general (and Clojure programs specifically) use macros of this sort to vastly reduce the boilerplate
;; needed to perform common tasks. Throughout this chapter, you'll see macros that combine forms, so there's no need to
;; dwell on the matter here. Instead, we'll move on to a macro domain that does just that, with the added bonus of
;; performing some interesting transformations in the process.
