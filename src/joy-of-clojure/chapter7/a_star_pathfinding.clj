;; Putting it all together: A* pathfinding
;; ---------------------------------------------------------------------------------------------------------------------
;; A* is a best-first pathfinding algorithm that maintains a set of candidate paths through a "world" with the purpose
;; of finding the least-difficult path to some goal. The difficulty (or cost) of a path is garnered by the A* algorithm
;; through the use of a function, which in this example is called total-cost, that builds an estimate of the total cost
;; from a start point to the goal. The application of this cost-estimate function total-cost is used to sort the
;; candidate paths in the order most likely to prove least costly.

;; The world
;; ---------
;; To represent the world, you'll again use a simple 2D matrix representation:
(def world [[  1   1   1   1   1]
            [999 999 999 999   1]
            [  1   1   1   1   1]
            [  1 999 999 999 999]
            [  1   1   1   1   1]])

;; The world structure is made from the values 1 and 999 respectively, corresponding to flat ground and cyclopean
;; mountains. What would you assume is the optimal path from the upper-left corner [0 0] to the lower-right [4 4]?
;; Clearly the optimal (and only) option is the Z-shaped path around the walls. Implementing an A* algorithm should fit
;; the bill, but first, we'll talk a bit about how to do so.

;; Neighbors
;; ---------
;; For any given spot in the world, you need a way to calculate possible next steps. You can do this using brute force
;; for small worlds, but a more general function is preferable. It turns out that if you restrict the possible moves to
;; north, south, east, and west, then any given move is +/-1 along the x or y axis. Taking advantage of this fact, you
;; can use the neighbors function from our section on vectors, shown again here:
(defn neighbors
  ([size yx]
   (neighbors [[-1 0] [1 0] [0 -1] [0 1]]                  ; define neighbors to be 1 spot away, crosswise
              size
              yx))
  ([deltas size yx]
   (filter (fn [new-yx]                                    ; remove illegal coordinates
             (every? #(< -1 % size) new-yx))
           (map #(vec (map + yx %))                        ; blindly calculate possible coordinates
                deltas))))

(neighbors 5 [0 0])
;;=> ([1 0] [0 1])

;; From the upper-left point, the only next steps are y=0, x=1 or y=1, x=0. Now that you have that, think about how you
;; might estimate the path cost from any given point. A simple cost estimate turns out to be described as, "From the
;; current point, calculate the expected cost by assuming you can travel to the right edge and then down to the
;; lower-right." An implementation of a function named estimate-cost that estimates the remaining path cost is shown
;; next.

;; Function to estimate the straight-line remaining path cost
(defn estimate-cost [step-cost-est size y x]
  (* step-cost-est
     (- (+ size size) y x 2)))

(estimate-cost 900 5 0 0)
;;=> 7200

(estimate-cost 900 5 4 4)
;;=> 0

;; From the y-x point [0 0], the cost of traveling 5 right and 5 down given an estimated single-step cost step-cost-est
;; is 7200. This is a pretty straightforward estimate based on a straight-line path. Likewise, starting at the goal
;; state [4 4] would cost nothing. Still needed is a function to calculate the cost of the path so far, named path-cost,
;; which is provided in the following listing.

;; Function to calculate the cost of the path traversed so far
(defn path-cost [node-cost cheapest-nbr]
  (+ node-cost
     (or (:cost cheapest-nbr) 0)))                          ; Add cheapest neighbor cost, else 0

(path-cost 900 {:cost 1})
;;=> 901

;; Now that you've created an estimated-cost function and a current-cost function, you can implement a simple total-cost
;; function.

;; Function to calculate the estimated total cost of the path
(defn total-cost [newcost step-cost-est size y x]
  (+ newcost
     (estimate-cost step-cost-est size y x)))

(total-cost 0 900 5 0 0)
;;=> 7200

(total-cost 1000 900 5 3 4)
;;=> 1900

(total-cost (path-cost 900 {:cost 1}) 900 5 3 4)
;;=> 1801

;; The second example shows that if you're one step away with a current cost of 1000, the total estimated cost is 1900,
;; which is expected. The third example uses the result of path-cost to derive a total step cost, which is emblematic of
;; how the two functions relate in the final A* implementation.

;; Now you have all the heuristics pieces in place. You may think we've simplified the heuristic needs of A*, but in
;; fact this is all that there is to it. The actual implementaiton is complex, and you'll tackle it next.

;; The A* implementation
;; ---------------------
;; Before we show the implementation of A*, you need one more auxiliary function, min-by, which retrieves from a
;; collection the minimum value dictated by some function. The implementation of min-by is naturally a straightforward
;; higher-order function, as shown next.

;; Function to retrieve the minimum value base on a criteria function
(defn min-by [f coll]
  (when (seq coll)
    (reduce (fn [min other]                                 ; Process each element
              (if (> (f min) (f other))                     ; Successively bubble the minimal value out to the return
                other
                min))
            coll)))

(min-by :cost [{:cost 100} {:cost 36} {:cost 9}])
;;=> {:cost 9}

;; This function will come in handy when you want to grab the cheapest path determined by the cost heuristic.
;; We've delayed enough! Let's finally implement the A* algorithm so that you navigate around the world. The following
;; listing shows a tail-recursive solution.

;; Main A* algorithm
(defn astar [start-yx step-est cell-costs]
  (let [size (count cell-costs)]
    (loop [steps 0
           routes (vec (replicate size (vec (replicate size nil))))
           work-todo (sorted-set [0 start-yx])]
      (if (empty? work-todo)                                ; Check done
        [(peek (peek routes)) :steps steps]                 ; Grab the first route
        (let [[_ yx :as work-item] (first work-todo)        ; Get the next work
              rest-work-todo (disj work-todo work-item)     ; Clear from todo
              nbr-yxs (neighbors size yx)                   ; Get neighbors
              cheapest-nbr (min-by :cost                    ; Calculate the least cost
                                   (keep #(get-in routes %)
                                         nbr-yxs))
              newcost (path-cost (get-in cell-costs yx)     ; Calculate the path so far
                                 cheapest-nbr)
              oldcost (:cost (get-in routes yx))]
          (if (and oldcost (>= newcost oldcost))            ; Check if new is worse
            (recur (inc steps) routes rest-work-todo)
            (recur (inc steps)                              ; Place a new path in the routes
                   (assoc-in routes yx
                             {:cost newcost
                              :yxs (conj (:yxs cheapest-nbr [])
                                         yx)})
                   (into rest-work-todo                     ; Add the estimated path to the todo, and recur
                         (map
                           (fn [w]
                             (let [[y x] w]
                               [(total-cost newcost step-est size y x) w]))
                           nbr-yxs)))))))))

;; The main thrust of the astar function occurs when you check that (>= newcost oldcost). Once you've calculated newcost
;; (the cost so far for the cheapest neighbor) and the cost-so-far oldcost, you perform one of two actions. The first
;; action occurs when newcost is greater than or equal to oldcost: you throw away this new path, because it's clearly a
;; worse alternative. The other action is the core functionality corresponding to the constant sorting of work-todo,
;; based on the cost of the path as determined by the heuristic function total-cost. The soul of A* is based on the fact
;; that the potential paths stored in work-todo are always sorted and distinct (through the use of a sorted set) based
;; on the estimated path cost function. Each recursive loop through the astar function maintains the sorted routes based
;; on the current-cost knowledge of the path added to the estimated total cost.
;; The results given by the astar function for the Z-shaped world are shown in the next listing.

;; Running the A* algorithm on Z World
(astar [0 0] 900 world)
;;=> [{:cost 17,
;;     :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4]
;;           [2 3] [2 2] [2 1] [2 0] [3 0] [4 0] [4 1]
;;           [4 2] [4 3] [4 4]]}
;;     :steps 94]

;; By following the y-x indices, you'll notice that the astar function traverses Z World along the path where cost is 1.
;; You can also build another world called Shrubbery World, shown next, that contains a single weakling shrubbery at
;; position [0 3] (represented by the number 2), and see how astar navigates it.

;; Shrubbery World
(astar [0 0]
       900
       [[  1   1   1   2    1]                              ; Shrubbery is 2
        [  1   1   1 999    1]
        [  1   1   1 999    1]
        [  1   1   1 999    1]
        [  1   1   1   1    1]])                            ; Clear path
;;=> [{:cost 9,
;;     :yxs [[0 0] [0 1] [0 2]                              ; Sequence of squares to walk the path
;;           [1 2]
;;           [2 2]
;;           [3 2]
;;           [4 2] [4 3] [4 4]]}
;;     :steps 134

;; When tracing the best path, you see that the astar function prefers the non-shrubbery path. But what would happen if
;; you placed a man-eating bunny along the previously safe path, represented by an ominously large number?

;; Bunny World
(astar [0 0]
       900
       [[  1   1   1   2    1]                              ; Shrubbery looks inviting
        [  1   1   1 999    1]
        [  1   1   1 999    1]
        [  1   1   1 999    1]
        [  1   1   1 666    1]])                            ; Bunny lies in wait
;;=> [{:cost 10,
;;     :yxs [[0 0] [0 1] [0 2] [0 3] [0 4]                  ; Bunny-less path
;;           [1 4]
;;           [2 4]
;;           [3 4]
;;           [4 4]]}
;;     :steps 132]

;; As expected, the astar function picks the shrubbery path (2) instead of the evil-bunny path to reach the final
;; destination.

;; Notes about the A* implementation
;; ---------------------------------
;; Each of the data structures, from the sorted set, to the tail-recursive astar function, to the higher-order function
;; min-by, is functional in nature and therefore extensible as a result. We encourage you to explore the vast array of
;; possible worlds traversable by the A* implementation and see what happens should you change the heuristic functions
;; along the way. Clojure encourages experimentation, and by partitioning the solution this way, we've enabled you to
;; explore different heuristics.
