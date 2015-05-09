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
