;; General utility functions
(ns gui.util)

(defn split-by-space [s]
  (clojure.string/split s #" "))

(defn make-regex [s]
  (re-pattern (str "(?i)" s)))

(defn all-matching?
  "See if all words in needles match the haystack."
  [needles haystack]
  (let [re-words (map make-regex (split-by-space needles))
        matched (filter #(re-find % haystack) re-words)]
    (= (count matched)
       (count re-words))))
