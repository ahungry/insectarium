(ns gui.dao-jira
  (:require
   [gui.net :as net]
   [gui.config :as config]
   ;; [clj-http.client :as client]
   [slingshot.slingshot :as ss]
   [cheshire.core :as cheshire])
  (:use [slingshot.slingshot :only [throw+ try+]]))

(def *opts (atom {}))

(defn config->opts!
  "Pull in the active config settings and put them in the provider opts."
  []
  (reset! *opts
          {:domain (config/get-domain :jira)
           :auth (config/get-auth :jira)}))

(defn set-domain [s] (swap! *opts assoc-in [:domain] s))
(defn get-domain [] (:domain @*opts))

(defn get-browser-url [ticket-id]
  (str (get-domain) "/browse/" ticket-id))

(defn get-url [url]
  (str (get-domain) "/rest/api/2" url))

(defn get-auth-token [] (-> @*opts :auth :cookie))
(defn get-auth-method [] (-> @*opts :auth :method))
(defn basic-auth? [] (= :basic  (get-auth-method)))
(defn cookie-auth? [] (= :cookie  (get-auth-method)))

(defn maybe-cookie-auth [m]
  (if (cookie-auth?)
    (conj m {:Cookie (get-auth-token)})
    m))

(defn get-basic-auth [{:keys [username token-or-pass]}]
  (str username ":" token-or-pass))

(defn maybe-basic-auth [m]
  (if (basic-auth?)
    (conj m {:basic-auth (get-basic-auth (:auth @*opts))})
    m))

(defn get-headers []
  (maybe-cookie-auth
   {:Content-Type "application/json"
    :Accept "application/json"}))

(defn jira-error->ticket [m]
  {:key "ERROR!!!"
   :fields {
            :summary (str m)
            :description (str m)}})

(defn http-get-ticket [id]
  (ss/try+
   (let [url (get-url (str "/issue/" id))]
     (net/get-json
      url
      (maybe-basic-auth {:headers (get-headers)})
      ))
   (catch [:status 404] {:keys [body]}
     (prn body)
     (jira-error->ticket body))))

(defn http-get-tickets [jql]
  (ss/try+
   (->
    (net/post-json
     (get-url "/search")
     (maybe-basic-auth
      {:headers (get-headers)
       :body (cheshire/generate-string
              {:maxResults 20
               :jql jql})})
     )
    :issues)
   (catch [:status 400] {:keys [body]}
     ;; Simulate a single 'issue' that lets the user know of the botched JQL.
     (prn body)
     [(jira-error->ticket body)])))

(defn jira-comment->comment [m]
  {:author (some-> m :author :displayName)
   :email (some-> m :author :emailAddress)
   :date-created (some-> m :created)
   :description (some-> m :body)})

(defn jira->ticket [m]
  {:provider :jira
   :description (some-> m :fields :description)
   :author (some-> m :fields :creator :displayName)
   :email (some-> m :fields :creator :emailAddress)
   :date-created (some-> m :fields :created)
   :resolution (some-> m :fields :resolution)
   :status (some-> m :fields :status :name)
   :title (some-> m :fields :summary)
   :id (some-> m :key)
   :comments (some->> m :fields :comment :comments (map jira-comment->comment))})

(defn -get-tickets [jql]
  (if (and (= (type jql) java.lang.String)
           (> (count jql) 0))
    (do
      (prn "Fetching tickets with this JQL: " jql)
      (->>
       (http-get-tickets jql)
       (into [])
       (map jira->ticket)))
    []))

(def get-tickets (memoize -get-tickets))

(defn -get-ticket [id]
  (->> (http-get-ticket id)
       jira->ticket))

(def get-ticket (memoize -get-ticket))
