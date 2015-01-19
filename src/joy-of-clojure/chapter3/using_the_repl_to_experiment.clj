;; Using the REPL to experiment
;; ---------------------------------------------------------------------------------------------------------------------
;; Most software development projects include a stage where you're not sure what needs to happen next. Perhaps you need
;; to use a library or part of a library you've never touched before. Or perhaps you know what your input to a
;; particular function will be, and what the output should be, but you aren't sure how to get from one to the other.
;; In some programming languages, this can be time-consuming and frustrating; but by using the power of the Clojure
;; REPL, the interactive command prompt, it can be fun.

;; Say someone suggests to you that coloring every pixel of a canvas with the xor of its x and y coordinates might
;; produce an interesting image. It shouldn't be too hard, so you can jump right in. You'll need to perform an operation
;; on every x and y in a pair of ranges. Do you know how range works?
(range 5)
;;=> (0 1 2 3 4)

;; That should do nicely for one coordinate. To nest seqs, for often does the trick. But again, rather than writing code
;; and waiting until you have enough to warrant compiling and testing, you can try it:
(for [x (range 2) y (range 2)] [x y])
;;=> ([0 0] [0 1] [1 0] [1 1])

;; There are the coordinates that will form your input. Now you need to xor them:
(xor 1 2)
;;=> CompilerException java.lang.RuntimeException: Unable to resolve symbol: xor in this context

;; Bother -- no function named xor. Fortunately, Clojure provides find-doc, which searches not just function names but
;; also their doc strings for the given term:
(find-doc "xor")
;; clojure.core/bit-xor
;; ([x y] [x y & more])
;; Bitwise exclusive or
;;=> nil

;; So the function you need is called bit-xor:
(bit-xor 1 2)
;;=> 3

;; Perfect! Next you want to adjust the earlier for form to return bit-xor along with x and y. The easiest way to do
;; this depends on what tool is hosting your REPL. In many, you can press the up-arrow key on your keyboard a couple of
;; times to bring back the earlier for form. You won't want to retype things to make minor adjustments, so take a moment
;; right now to figure out a method you like that will let you make a tweak like this by inserting the bit-xor call:
(for [x (range 2) y (range 2)]
  [x y (bit-xor x y)])
;;=> ([0 0 0] [0 1 1] [1 0 1] [1 1 0])

;; That looks about right. We're about to shift gears to pursue the graphics side of this problem, so tuck away that bit
;; of code in a function so it'll be easy to use later:
(defn xors [max-x max-y]
  (for [x (range max-x) y (range max-y)]
    [x y (bit-xor x y)]))

(xors 2 2)
;;=> ([0 0 0] [0 1 1] [1 0 1] [1 1 0])

;; You might even save that into a .clj file, if you haven't already. It's worth mentioning that saving files with the
;; .clj extension is standard for Clojure source code, whereas .cljs is standard for ClojureScript files.

;; Clojure's REPL isn't just for playing around; it's also great for experimenting with Java libraries. We believe
;; there's no better environment for exploring a Java API than Clojure's REPL. To illustrate, poke around with java.awt,
;; starting with a Frame:
(def frame (java.awt.Frame.))
;=> #'user/frame

;; That should have created a Frame, but no window appeared. Did it work at all?
frame
;;=> #<Frame java.awt.Frame[frame0,0,22,0x0,invalid,hidden,layout=java.awt.BorderLayout,title=,resizable,normal]>

;; Well, you have a Frame object, but perhaps the reason you can't see it is hinted at by the word hidden in the
;; #<Frame...> printout. Perhaps the Frame has a method you need to call to make it visible. One way to find out would
;; be to check the Javadoc of the object, but because you're at the REPL already, let's try something else. You’ve
;; already seen how the for macro works, so maybe you can check a class for which methods it has to see whether one you
;; can use is available:
(for [meth (.getMethods java.awt.Frame) ;; Iterater over class methods
      :let [name (.getName meth)]       ;; Bind a variable name
      :when (re-find #"Vis" name)]      ;; Build a seq of matched names
  name)
;;=> ("setVisible" "isVisible")

;; The for macro provides a way to iterate over a collection, performing some action on each item and collecting the
;; results into a sequence. The preceding example iterates over a sequence of the methods available on the
;; java.awt.Frame class. Whenever Clojure encounters a symbol that looks like a Java class name, it attempts to resolve
;; it as a class. This behavior allows you to then call the getMethods method directly on the class. Next, a :let flag
;; and bindings vector is used, working similarly to the let special form that you use to bind the local method-name to
;; the result of calling the method .getName on each method in turn. The :when is used to limit the elements used in its
;; body to only those that return a truthy value in the expression after the directive. Using these directives lets you
;; iterate through the methods and build a seq of those whose names match a regular expression #"Vis".

;; Your query returns two potential methods, so try each of them:
(.isVisible frame)
;;=> false

;; That's false, as you might have suspected. Will setting it to true make any difference?
(.setVisible frame true)
;;=> nil

;; It did, but it's so tiny! Not to worry, because a Frame class also has a .setSize method you can use:
(.setSize frame (java.awt.Dimension. 200 200))
;;=> nil

;; And up pops a blank window for you to draw on. At this point, we'll guide you through the rest of this section; but
;; keep in mind that Java's official API might be of interest, should you choose to extend the example program.

;; NOTE The Clojure clojure.java.javadoc namespace has a javadoc function to query and view official API documentation:
;; (javadoc frame). This should return a string corresponding to a URL and open a browser window for the right page of
;; documentation. Prior to Clojure 1.2, this function was in clojure.contrib.repl-utils.

;; What you need to draw into your Frame is its graphics context, which can be fetched as shown:
(def gfx (.getGraphics frame))
;;=> #'user/gfx

;; Then, to draw, you can try out the fillRect method of that graphics context. If you're trying this yourself, make
;; sure the blank window is positioned so that it's unobscured while you’re typing into your REPL:
(.fillRect gfx 100 100 50 75)

;; And just like that, you're drawing on the screen interactively. You should see a single black rectangle in the
;; formerly empty window. Exciting, isn't it? You could be a kid playing with turtle graphics for the first time, it's
;; so much fun. But what it needs is a dash of color:
(.setColor gfx (java.awt.Color. 255 128 0))
(.fillRect gfx 100 150 75 50)

;; Now there should be an orange rectangle as well. Perhaps the coloring would make Martha Stewart cry, but you've tried
;; all the basic building blocks you'll need to complete the original task: you have a function that returns a seq of
;; coordinates and their xor values, you have a window you can draw into, and you know how to draw rectangles of
;; different colors. Bear in mind that if you move the frame with the mouse, your beautiful graphics may disappear
;; (depending on your OS and window manager). This is an artifact of this limited experiment and can be avoided using
;; the full Java Swing capabilities.

;; What’s left to do? Use the graphics functions you just saw to draw the xor values:
(doseq [[x y xor] (xors 200 200)]
  (.setColor gfx (java.awt.Color. xor xor xor))
  (.fillRect gfx x y 1 1))

;; The xors function you created earlier generates a seq of vectors, if you remember, where each vector has three
;; elements: the x and y for your coordinates and the xor value that goes with them. The first line here uses
;; destructuring to assign each of those three values to new locals x, y, and xor, respectively.
;; The second line sets the "pen" color to a gray level based on the xor value, and the final line draws a single-pixel
;; rectangle at the current coordinates.

;; But just because you've succeeded doesn't mean you have to quit. You've built up some knowledge and a bit of a
;; toolbox, so why not play with it a little?
