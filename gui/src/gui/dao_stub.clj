(ns gui.dao-stub)

(def *opts (atom {}))

(defn get-ticket [_]
  {:title "Some fake ticket"
   :description "Fix the bug"
   :id "xx11"})

(defn get-tickets [_]
  [(get-ticket _)
   (get-ticket _)])

(defn provider! [opts]
  (reset! *opts opts)
  {:get-ticket get-ticket
   :get-tickets get-tickets})
