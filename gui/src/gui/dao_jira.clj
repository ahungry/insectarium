(ns gui.dao-jira)

(defn get-auth-token
  "The user can maintain their own token, I'm not doing auth, sorry.

  It should look like:

  cloud.session.token=eyJ....mdQ"
  []
  (slurp "/tmp/token.txt"))

(defn get-ticket []
  {:title "Some fake ticket"
   :description "Fix the bug"
   :id "xx11"})

(defn get-tickets [_]
  [(get-ticket)
   (get-ticket)])

(defn provider []
  {:get-auth-token get-auth-token
   :get-ticket get-ticket
   :get-tickets get-tickets})
