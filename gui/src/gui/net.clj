(ns gui.net
  (:require
   [clj-http.client :as client]
   [cheshire.core :as cheshire]))

(defn as-json [f]
  (fn [url opts]
    (->
     (f url (conj {:as :json
                   :coerce :always}
                  opts))
     :body)))

(def get-json (as-json client/get))
(def post-json (as-json client/post))
