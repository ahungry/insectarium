(ns gui.dao
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [expound.alpha :as e]
   [gui.config :as config]
   [gui.dao-stub :as dp-stub]
   [gui.dao-github :as dp-github]
   [gui.dao-jira :as dp-jira]
   ))

(defn kw->ns [k]
  (case k
    :jira 'gui.dao-jira
    :github 'gui.dao-github
    :stub 'gui.dao-stub))

;; Define the specs
(s/def ::provider keyword?)
(s/def ::author string?)
(s/def ::date-created string?)
(s/def ::description string?)
(s/def ::email string?)
(s/def ::id string?)
(s/def ::resolution string?)
(s/def ::status string?)
(s/def ::title string?)

(s/def ::ticket
  (s/keys :req-un [::provider
                   ::author
                   ::date-created
                   ::description
                   ::email
                   ::id
                   ::resolution
                   ::status
                   ::title]))

(s/def ::tickets (s/coll-of ::ticket :into []))

(defn get-browser-url [provider query]
  (let [f (ns-resolve (kw->ns provider) 'get-browser-url)]
    (f query)))

(defn assert-spec
  "Weakly assert a spec (as in, we just print some debug that it failed)."
  [sp m]
  (if (s/valid? sp m)
    m
    (do
      (e/expound sp m)
      m)))

(def assert-ticket (partial assert-spec ::ticket))
(def assert-tickets (partial assert-spec ::tickets))

(defn get-ticket [provider query]
  (let [f (ns-resolve (kw->ns provider) 'get-ticket)]
    (assert-ticket (f query))))

(defn get-tickets-for-provider [query provider]
  (let [f (ns-resolve (kw->ns provider) 'get-tickets)]
    ;; (assert-tickets (f query))
    (f query)
    ))

;; TODO: Run these in parallel
(defn get-tickets [query]
  (reduce concat
          (map (partial get-tickets-for-provider query)
               (config/get-providers))))

(defn set-provider! [provider]
  (let [f (ns-resolve (kw->ns provider) 'config->opts!)]
    (f)))

(defn set-providers! []
  (config/set-conf!)
  (doall (map set-provider! (config/get-providers))))
