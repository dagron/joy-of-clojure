;; Using macros to change forms
;; ---------------------------------------------------------------------------------------------------------------------
;; One way to design macros is to start by writing out example code that you wish worked -- code that has the minimal
;; distance between what you must specify and the specific application domain in which you're working. Then, with the
;; goal of making this code work, you begin writing macros and functions to fill in the missing pieces.
;; For example, when designing software systems, it's often useful to identify the 'things" that make up your given
;; application domain, including their logical groupings. The level of abstraction at this point in the design is best
;; kept high and shouldn't include details about implementation. Imagine that you want to describe a simple domain of
;; the ongoing struggle between humans and monsters:

;; * Man versus monster
;;   * People
;;     * Men (humans)
;;       - Name
;;       - Have beards?
;;   * Monsters
;;     * Chupacabra
;;       - Eats goats?

;; Although this is a simple format, it needs work to be programmatically useful. Therefore, the goal of this section is
;; to write macros that perform the steps to get from this simple representation to the one more conducive to
;; processing. The outline form can be rendered in a Clojure form, assuming the existence of some macros and functions
;; you've yet to define, like this:
(domain man-vs-monster
  (grouping people
    (Human "A stock human")
    (Man (isa Human)
         "A man, baby!"
         [name]
         [has-beard?]))
  (grouping monsters
    (Chupacabra
      "A fierce, yet elusive creature"
      [eats-goats?])))

;; One possible structure underlying this sample format is a tree composed of individual generic nodes, each taking a
;; form similar to the following:
{:tag <node form>,                                          ; Domain grouping, and so on
 :attrs {},                                                 ; For example, :name people
 :content [<nodes>]}                                        ; For example, properties

;; You'd never say this is a beautiful format, but it does present practical advantages over the original format -- it's
;; a tree, it's composed of simple types, it's regular, and it's recognizable to some existing libraries.

;; Clojure aphorism
;; ----------------
;; Clojure is a design language where the conceptual model is also Clojure.

;; Start with the outer-level element, domain:
(defmacro domain [name & body]
  `{:tag :domain,
    :attrs {:name (str '~name)},
    :content [~@body]})

;; The body of domain is fairly straightforward in that it sets the domain-level tree node and splices the body of the
;; macro into the :content slot. After domain expands, you'd expect its body to be composed of a number of grouping
;; forms, which are then handled by an aptly named macro:
(declare handle-things)

(defmacro grouping [name & body]
  `{:tag :grouping,
    :attrs {:name (str '~name)},
    :content [~@(handle-things body)]})

;; Similar to domain, grouping expands into a node with its body spliced into the :content slot. In its body, the
;; grouping macro uses a form named handle-things that hasn't been written yet, so you have to use declare to avoid a
;; compilation error. But grouping differs from domain in that it splices in the result of the call to a function,
;; handle-things:
(declare grok-attrs grok-props)

(defn handle-things [things]
  (for [t things]
    {:tag :thing,
     :attrs (grok-attrs (take-while (comp not vector?) t))
     :content (if-let [c (grok-props (drop-while (comp not vector?) t))]
                [c]
                [])}))

;; Because the body of a thing is fairly simple and regular, you can simplify the implementation of handle-things by
;; again splitting it into two functions. The first function, grok-attrs, handles everything in the body of a thing
;; that's not a vector, and grok-props handles properties that are. In both cases, these leaf-level functions return
;; specifically formed maps:
(defn grok-attrs [attrs]
  (into {:name (str (first attrs))}
        (for [a (rest attrs)]
          (cond
            (list? a) [:isa (str (second a))]
            (string? a) [:comment a]))))

;; The implementation of grok-attrs may seem overly complex, especially given that the sample domain model DSL only
;; allows for a comment attribute and an optional isa specification (as shown in the sample layout in the beginning of
;; this section). But by laying it out this way, you can easily expand the function to handle a richer set of
;; attributes later. Likewise with grok-props, this more complicated function pulls apart the vector representing a
;; property so it's more conducive to expansion:
(defn grok-props [props]
  (when props
    {:tag :properties, :attrs nil,
     :content (apply vector (for [p props]
                 {:tag :property,
                  :attrs {:name (str first p)},
                  :content nil}))}))

;; Now that you've created the pieces, take a look at the new DSL in action:
(def d
  (domain man-vs-monster
    (grouping people                                        ; Group of people
      (Human "A stock human")                               ; One kind of person

      (Man (isa Human)                                      ; Another kind of person
        "A man, baby"
        [name]
        [has-beard?]))
    (grouping monsters                                      ; Group of monsters
      (Chupacabra                                           ; One kind of monsters
        "A fierce, yet elusive creature"
        [eats-goats?]))))

;; You can navigate this structure as follows:
(:tag d)
;;=> :domain

(:tag (first (:content d)))
;;=> :grouping

;; Maybe that's enough to prove to you that you've constructed the promised tree, but probably not. Therefore, you can
;; pass a tree into a function that expects one of that form8 and see what comes out on the other end:
(use '[clojure.xml :as xml])
(xml/emit d)

;; Performing this function call prints out the corresponding XML representation, minus the pretty printing:
;; <?xml version='1.0' encoding='UTF-8'?>
;; <domain name='man-vs-monster'>
;;   <grouping name='people'>
;;     <thing name='Human' comment='A stock human'>
;;       <properties>
;;       </properties>
;;     </thing>
;;     <thing isa='Human' name='Man' comment='A man, baby'>
;;       <properties>
;;         <property name='clojure.core$first@4cd7ec6a[name]'/>
;;         <property name='clojure.core$first@4cd7ec6a[has-beard?]'/>
;;       </properties>
;;     </thing>
;;   </grouping>
;;   <grouping name='monsters'>
;;     <thing name='Chupacabra' comment='A fierce, yet elusive creature'>
;;       <properties>
;;         <property name='clojure.core$first@4cd7ec6a[eats-goats?]'/>
;;       </properties>
;;     </thing>
;;   </grouping>
;; </domain>

;; Our approach was to define a single macro entry point domain, intended to build the top-level layers of the output
;; data structure and instead pass the remainder on to auxiliary functions for further processing. In this way, the body
;; of the macro expands into a series of function calls, each taking some subset of the remaining structure and
;; returning some result that's spliced into the final result. This functional composition approach is fairly common
;; when defining macros. The entirety of the domain description could have been written in one monolithic macro, but by
;; splitting the responsibilities, you can more easily extend the representations for the constituent parts.
;; Macros take data and return data, always. It so happens that in Clojure, code is data and data is code.



