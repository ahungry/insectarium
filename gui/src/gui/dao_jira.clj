(ns gui.dao-jira
  (:require
   [clj-http.client :as client]
   [cheshire.core :as cheshire]))

(def *opts (atom {}))

(defn set-domain [s] (swap! *opts assoc-in [:domain] s))
(defn get-domain [] (:domain @*opts))

(defn get-url [url]
  (str (get-domain) "/rest/api/2" url))

(defn get-auth-token
  "The user can maintain their own token, I'm not doing auth, sorry.

  It should look like:

  cloud.session.token=eyJ....mdQ"
  []
  (slurp "/tmp/token.txt"))

(defn get-headers []
  {:Content-Type "application/json"
   :Cookie (get-auth-token)})


(defn http-get-ticket [id]
  (let [url (get-url (str "/issue/" id))]
    (prn url)
    (->
     (client/get
      url
      {:headers (get-headers)
       })
     :body
     (cheshire/parse-string true)
     )))

;; TODO: I thought there was a way to have clj-http auto-parse body.
(defn http-get-tickets [jql]
  (->
   (client/post
    (get-url "/search")
    {:headers (get-headers)
     :body (cheshire/generate-string
            {:maxResults 2
             :jql jql})
     ;; :body {:maxResults 3
     ;;        :jql jql}
     })
   :body
   (cheshire/parse-string true)
   :issues))

(defn jira-comment->comment [m]
  {:author (some-> m :author :displayName)
   :email (some-> m :author :emailAddress)
   :description (some-> m :body)})

(defn jira->ticket [m]
  {:description (some-> m :fields :description)
   :author (some-> m :fields :creator :displayName)
   :email (some-> m :fields :creator :emailAddress)
   :date-created (some-> m :fields :created)
   :resolution (some-> m :fields :resolution)
   :status (some-> m :fields :status :name)
   :title (some-> m :fields :summary)
   :id (some-> m :key)
   :comments (some->> m :fields :comment :comments (map jira-comment->comment))})

;; TODO: Pull the jql from the state map
(defn get-tickets [_jql]
  (prn "Fetching tickets...")
  (->>
   (http-get-tickets "assignee = currentUser()")
   (into [])
   (map jira->ticket)))

(defn -get-ticket [id]
  (->> (http-get-ticket id)
       jira->ticket))

(def get-ticket (memoize -get-ticket))

(defn provider! [opts]
  (reset! *opts opts)
  {:get-auth-token get-auth-token
   :get-ticket get-ticket
   :get-tickets get-tickets})
