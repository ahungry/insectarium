(ns gui.dao-github
  (:require
   [gui.net :as net]
   [clj-http.client :as client]
   [slingshot.slingshot :as ss]
   [cheshire.core :as cheshire])
  (:use [slingshot.slingshot :only [throw+ try+]]))

(def *opts (atom {
                  :domain "https://api.github.com"
                  }))

(defn set-domain [s] (swap! *opts assoc-in [:domain] s))
(defn get-domain [] (:domain @*opts))

(defn get-browser-url [ticket-id]
  (str (get-domain) "/browse/" ticket-id))

(defn get-url [url]
  (str (get-domain) url))

(defn get-auth-token
  "This should be for basic auth - a user/password in format:

  user:pass

  More secure may be for the system to prompt for the user data and
  just store in a variable."
  []
  (clojure.string/trim (slurp "/tmp/token.txt")))

(defn get-headers []
  {:Content-Type "application/json"
   :Accept "application/json"})

;; The :as :json option does not seem to work with github response :shrug:
(defn http-get-tickets []
  (-> (client/get
       (get-url "/issues")
       {:headers (get-headers)
        :basic-auth (get-auth-token)})
      :body
      (cheshire/parse-string true)))

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

(defn get-ticket []
  (first (get-tickets)))

(defn provider! [opts]
  ;; (reset! *opts opts)
  {:get-auth-token get-auth-token
   :get-browser-url get-browser-url
   :get-ticket get-ticket
   :get-tickets get-tickets})
