(ns gui.dao-github
  (:require
   [gui.net :as net]
   [gui.config :as config]
   [clj-http.client :as client]
   [slingshot.slingshot :as ss]
   [cheshire.core :as cheshire])
  (:use [slingshot.slingshot :only [throw+ try+]]))

(def *opts (atom {}))

(defn config->opts!
  "Pull in the active config settings and put them in the provider opts."
  []
  (reset! *opts
          {:domain (config/get-domain)
           :auth (config/get-auth)}))

(defn set-domain [s] (swap! *opts assoc-in [:domain] s))
(defn get-domain [] (:domain @*opts))

(defn get-browser-url [ticket-id]
  ticket-id)

(defn get-url [url]
  (str (get-domain) url))

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

;; The :as :json option does not seem to work with github response :shrug:
(defn http-get-tickets []
  (-> (client/get
       (get-url "/issues?filter=all")
       (maybe-basic-auth {:headers (get-headers)}))
      :body
      (cheshire/parse-string true)))

(defn ticket-id->issue-slug
  "Given some URL like https://github.com/owner/repo/issues/number will
  convert into the appropriate slots."
  [s]
  (let [xs (clojure.string/split s #"/")]
    (zipmap [:id :_ :repo :owner] (reverse xs))))

(defn github-error->ticket [m]
  {:key "ERROR!!!"
   :fields {
            :summary (str m)
            :description (str m)}})

(defn http-get-ticket [ticket-id]
  (ss/try+
   (let [{:keys [owner repo id]} (ticket-id->issue-slug ticket-id)
         url (get-url (format "/repos/%s/%s/issues/%s" owner repo id))]
     (-> (client/get
          url
          (maybe-basic-auth {:headers (get-headers)}))
         :body
         (cheshire/parse-string true)))
   (catch [:status 404] {:keys [body]}
     (prn body)
     (github-error->ticket body))))

(defn github->ticket [m]
  {:description (some-> m :body)
   :author (some-> m :user :login)
   :email (some-> m :user :html_url)
   :date-created (some-> m :updated_at)
   :resolution nil
   :status (some-> m :state)
   :title (some-> m :title)
   :id (some-> m :html_url)
   :comments []})

(defn -get-tickets [_]
  (->> (http-get-tickets)
       (map github->ticket)))

(def get-tickets (memoize -get-tickets))

(defn get-ticket [id]
  (->
   (http-get-ticket id)
   github->ticket))

(defn provider! [opts]
  (reset! *opts opts)
  {:get-auth-token get-auth-token
   :get-browser-url get-browser-url
   :get-ticket get-ticket
   :get-tickets get-tickets})
