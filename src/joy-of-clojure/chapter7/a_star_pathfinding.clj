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
