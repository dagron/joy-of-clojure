;; Regular Expressions
;; ---------------------------------------------------------------------------------------------------------------------
;; Some people, when confronted with a problem, think "I know, I'll use regular expressions." Now they have two problems.
;; —Jamie Zawinski, Usenet post, 12 August 1997, alt.religion.emacs.

;; Regular expressions are a powerful and compact way to find specific patterns in text strings. Although we sympathize
;; with Zawinski's attitude and appreciate his wit, sometimes regular expressions are a useful tool to have on hand.
;; The full capabilities of regular expressions (or regexes) are well beyond the scope of this section, but we'll look
;; at some of the ways Clojure uses Java's regex capabilities.

;; Java's regular-expression engine is reasonably powerful, supporting Unicode and features such as reluctant
;; quantifiers and look-around clauses. Clojure doesn't try to reinvent the wheel and instead provides special syntax
;; for literal Java regex patterns plus a few functions to help Java's regex capabilities fit better with the rest of
;; Clojure.

;; A literal regular expression in Clojure looks like this:
#"an example pattern"

;; This produces a compiled regex object that can be used either directly with Java interop method calls or with any of
;; the Clojure regex functions described later:
(class #"example")
;;=> java.util.regex.Pattern

;; Although the pattern is surrounded with double quotes like string literals, the way things are escaped within the
;; quotes isn't the same. If you've written regexes in Java, you know that any backslashes intended for consumption by
;; the regex compiler must be doubled, as shown in the following compile call. This isn't necessary in Clojure regex
;; literals, as shown by the undoubled return value:
(java.util.regex.Pattern/compile "\\d")
;;=> #"\d"

;; In short, the only rules you need to know for embedding unusual literal characters or predefined character classes
;; are listed in the Javadoc for Pattern.

;; Regular expressions accept option flags, shown in the table below, that can make a pattern case-insensitive or enable
;; multiline mode. Clojure's regex literals starting with (?<flag>) set the mode for the rest of the pattern. For
;; example, the pattern #"(?i)yo" matches the strings “yo”, “yO”, “Yo”, and “YO”.

;; Flags that can be used in Clojure regular-expression patterns, along with their long name and a description of what
;; they do. See Java's documentation for the java.util.regex.Pattern class for more details.

;; Flag   Flag Name         Description
;; ---------------------------------------------------------------------------------------------------------------------
;; d      UNIX_LINES        ., ^, and $ match only the Unix line terminator '\n'.
;; i      CASE_INSENSITIVE  ASCII characters are matched without regard to uppercase or lower-case.
;; x      COMMENTS          Whitespace and comments in the pattern are ignored.
;; m      MULTILINE         ^ and $ match near line terminators instead of only at the beginning or end of the entire
;;                          input string.
;; s      DOTALL            . matches any character including the line terminator.
;; u      UNICODE_CASE      Causes the i flag to use Unicode case insensitivity instead of ASCII.

;; The re-seq function is Clojure's regex workhorse. It returns a lazy seq of all matches in a string, which means it
;; can be used to efficiently test whether a string matches or to find all matches in a string or a mapped file:
(re-seq #"\w+" "one-two/three")
;;=> ("one" "two" "three")

;; The preceding regular expression has no capturing groups, so each match in the returned seq is a string. A capturing
;; group (subsegments that are accessible via the returned match object) in the regex causes each returned item to be a
;; vector:
(re-seq #"\w*(\w)" "one-two/three")
(["one" "e"] ["two" "o"] ["three" "e"])

;; Now that we've looked at some nice functions you can use, we'll talk about one object you shouldn't.
;; Java's regular-expression engine includes a Matcher object that mutates in a non-thread-safe way as it walks through
;; a string finding matches. This object is exposed by Clojure via the re-matcher function and can be used as an
;; argument to re-groups and the single-parameter form of re-find. We highly recommend avoiding all of these unless
;; you're certain you know what you're doing. These dangerous functions are used internally by the implementations of
;; some of the recommended functions described earlier, but in each case they're careful to disallow access to the
;; Matcher object they use. Use matchers at your own risk, or better yet don't use them directly at all.
