(ns gui.dao)

(defn get-ticket []
  {:title "Some fake ticket"
   :description "Fix the bug"
   :id "xx11"})

(defn get-tickets [_]
  [(get-ticket)
   (get-ticket)])
