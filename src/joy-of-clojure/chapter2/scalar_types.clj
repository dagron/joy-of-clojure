;; Integers
;; ---------------------------------------------------------------------------------------------------------------------
;; Here are some example integers in clojure:
42
+9
-107
991778647261948849222819828311491035886734385827028118707676848307166514
;; All the numbers shown, except the last one, are read as primitive Java longs; the last is too big to fit in a long
;; and thus is read as a BigInt (which is printed with a trailing N).
;; The following illustrates the use of decimal, hexadecimal, octal, radix-32, and binary literals, respectively,
;; all representing the same number:

[127 0x7F 0177 32r3V 2r01111111]
;;=> [127 127 127 127 127]
;;  The radix notation supports up to base 36, including both hexadecimal (16r7F) and octal (8r177).
;; When using the higher bases (hexadecimal might spring to mind), you'll notice ASCII letters are needed to supplement
;; the digits 0–9. The fact that there are only 26 usable ASCII characters limits the range of bases to a maximum of 36:
;; 10 numbers between 0 and 9 plus 26 letters.
;; Finally, adding signs to the front of each of the integer literals is also legal.


;; Floating-point Numbers
;; ---------------------------------------------------------------------------------------------------------------------
;; The following numbers are examples of valid floating-point numbers:
1.17
+1.22
-2.
366e7
32e-14
10.7e-3


;; Rationals
;; ---------------------------------------------------------------------------------------------------------------------
;; The following numbers are examples of valid rational numbers:
22/7
-7/22
1028798300297636767687409028872/88829897008789478784
-103/4
;; Something to note about rational numbers in Clojure is that they'll be simplified if they can —-
;; the rational 100/4 will resolve to the integer 25.


;; Symbols
;; ---------------------------------------------------------------------------------------------------------------------
;; Symbols in Clojure are objects in their own right but are often used to represent another value:
(def yucky-pi 22/7)
yucky-pi
;;=> 22/7

;; When a number or a string is evaluated, you get back exactly the same object; but when a symbol is evaluated,
;; you get back whatever value that symbol is referring to in the current context. In other words,
;; symbols are typically used to refer to function parameters, local variables, globals, and Java classes.


;; Keywords
;; ---------------------------------------------------------------------------------------------------------------------
;; Keywords are similar to symbols, except that they always evaluate to themselves. You're likely to see the use of
;; keywords far more in Clojure than symbols. The form of a keyword's literal syntax is as follows:
:chumby
:2
:?
:ThisIsTheNameOfaKeyword
;; Although keywords are prefixed by a colon :, it's only part of the literal syntax and not part of the name itself.


;; Strings
;; ---------------------------------------------------------------------------------------------------------------------
;; trings in Clojure are represented similarly to the way they're used in many program- ming languages. A string is any
;; sequence of characters enclosed within a set of double quotes, including newlines, as shown:
"This is a string"
"This is also a
String"

;; Both will be stored as written, but when printed at the REPL, multiline strings include escapes for the literal
;; newline characters: for example,
"This is also a\n String".


;; Characters
;; ---------------------------------------------------------------------------------------------------------------------
;; Clojure characters are written with a literal syntax prefixed with a backslash and are stored as
;; Java Character objects:
\a       ; The character lowercase a
\A       ; The character uppercase A
\u0042   ; The Unicode character uppercase B
\\       ; The back-slash character \
\u30DE   ; The Unicode katakana character ?
