;; Don't create Boolean objects
;; ---------------------------------------------------------------------------------------------------------------------
;; It's possible to create an object that looks a lot like, but isn't actually, false. Java has left a land mine for you
;; here, so take a moment to look at it so that you can step past it gingerly and get on with your life:
(def evil-false (Boolean. "false")) ;; NEVER do this

;; This creates a new instance of Boolean -- and that's already wrong! Because there are only two possible values of
;; Boolean, an instance of each has already been made for us -- they're named true and false. But here we've gone and
;; done it anyway, created a new instance of Boolean and stored it in a var named evil-false. It looks like false:
evil-false
;;=> false

;; Sometimes it even acts like false:
(= false evil-false)
;;=> true

;; But once it gains your trust, it'll show you just how wicked it is by acting like true:
(if evil-false :truthy :falsey)
;;=> :truthy

;; Java's own documentation warns against the creation of this evil thing, and now you've been warned again. If you want
;; to parse a string, use the Boolean class's static valueOf method instead of its constructor. This is the right way:
(if (Boolean/valueOf "false") :truthy :falsey)
;;=> :falsey

