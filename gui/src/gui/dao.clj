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

(def *provider (atom (dp-stub/provider! {})))

(defn set-provider! [x]
  (reset! *provider x))

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
    (assert-tickets (f query))))

;; TODO: Run these in parallel
(defn get-tickets [query]
  (reduce concat
          (map (partial get-tickets-for-provider query)
               (config/get-providers))))

;; (stest/instrument)
(defn get-provider! [s opts]
  (case s
    :jira (dp-jira/provider! opts)
    :github (dp-github/provider! opts)
    :stub (dp-stub/provider! opts)
    (dp-stub/provider! opts)))

(defn use-provider! [x]
  (let [opts {:domain (config/get-domain)
              :auth (config/get-auth)}]
    (->
     (case x
       :jira (dp-jira/provider! opts)
       :github (dp-github/provider! opts)
       :stub (dp-stub/provider! opts)
       (dp-stub/provider! opts))
     set-provider!)))

(defn set-provider-from-config! [& [provider]]
  (config/set-conf!)
  ;; (use-provider! (or provider (config/get-provider)))
  )
