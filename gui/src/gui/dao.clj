(ns gui.dao
  (:require
   [gui.dao-stub :as dp-stub]))

(def provider (atom (dp-stub/provider)))

(defn set-provider [x]
  (reset! provider x))

(defn get-ticket [_]
  ((:get-ticket @provider) _))

(defn get-tickets [_]
  ((:get-tickets @provider) _))
