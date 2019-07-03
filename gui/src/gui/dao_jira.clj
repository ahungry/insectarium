(ns gui.dao-jira
  (:require
   [clj-http.client :as client]
   [cheshire.core :as cheshire]))

(defn get-auth-token
  "The user can maintain their own token, I'm not doing auth, sorry.

  It should look like:

  cloud.session.token=eyJ....mdQ"
  []
  (slurp "/tmp/token.txt"))

(defn get-headers []
  {:Content-Type "application/json"
   :Cookie (get-auth-token)})

;; TODO: I thought there was a way to have clj-http auto-parse body.
(defn http-get-tickets [jql]
  (->
   (client/post
    "https://ahungry.atlassian.net/rest/api/2/search"
    {:headers (get-headers)
     :body (cheshire/generate-string
            {:maxResults 3
             :jql jql})
     ;; :body {:maxResults 3
     ;;        :jql jql}
     })
   :body
   (cheshire/parse-string true)
   :issues))

(defn jira->ticket [m]
  {:description (some-> m :fields :description)
   :title (some-> m :fields :summary)
   :id (some-> m :key)})

(defn get-tickets [jql]
  (->>
   (http-get-tickets jql)
   (into [])
   (map jira->ticket)))

(defn get-ticket []
  {:title "Some fake ticket"
   :description "Fix the bug"
   :id "xx11"})

(defn provider []
  {:get-auth-token get-auth-token
   :get-ticket get-ticket
   :get-tickets get-tickets})
