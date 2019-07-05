(ns gui.dao-stub)

(def *opts (atom {}))

(defn get-comment []
  {:author "Jon Smith"
   :email "m@ahungry.com"
   :description "blabla some fake comment"})

(defn get-ticket [{:keys [title id description] :as m}]
  {:title (or title "Some title here")
   :description (or description "some description here")
   :id (or id (str m))
   :author "Jon Smith"
   :email "m@ahungry.com"
   :date-created "today"
   :resolution "Won't Fix"
   :status "Done"
   :comments [(get-comment)]})

(defn get-tickets [_]
  [(get-ticket {:title "my first ticket" :description "Do the work" :id "XX-123"})
   (get-ticket {:title "my second ticket" :description "Do the work faster" :id "XX-124"})
   (get-ticket {:title "project overdue" :description "Oh noes" :id "URG-124"})])

(defn provider! [opts]
  (reset! *opts opts)
  {:get-ticket get-ticket
   :get-tickets get-tickets})
