(ns gui.dao
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [gui.dao-stub :as dp-stub]))

;; Define the specs
(s/def ::author string?)
(s/def ::date-created string?)
(s/def ::description string?)
(s/def ::email string?)
(s/def ::id string?)
(s/def ::resolution string?)
(s/def ::status string?)
(s/def ::title string?)

(s/def ::ticket
  (s/keys :req-un [::author
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

(defn get-browser-url [_]
  ((:get-browser-url @*provider) _))

(defn assert-spec
  "Weakly assert a spec (as in, we just print some debug that it failed)."
  [sp m]
  (if (s/valid? sp m)
    m
    (do
      (prn (str (s/explain sp m)))
      m)))

(def assert-ticket (partial assert-spec ::ticket))
(def assert-tickets (partial assert-spec ::tickets))

(defn get-ticket [_]
  (assert-ticket ((:get-ticket @*provider) _)))

(defn get-tickets [_]
  (assert-tickets ((:get-tickets @*provider) _)))

;; (stest/instrument)
