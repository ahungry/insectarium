(ns gui.dao
  (:require
   [gui.dao-stub :as dp-stub]))

(def *provider (atom (dp-stub/provider! {})))

(defn set-provider! [x]
  (reset! *provider x))

(defn get-browser-url [_]
  ((:get-browser-url @*provider) _))

(defn get-ticket [_]
  ((:get-ticket @*provider) _))

(defn get-tickets [_]
  ((:get-tickets @*provider) _))
