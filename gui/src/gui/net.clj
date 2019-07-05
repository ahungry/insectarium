(ns gui.net
  (:require
   [clj-http.client :as client]
   [cheshire.core :as cheshire]))

(defn xas-json
  "Wrapper around client call to ensure consistent parsing."
  [f]
  (fn [& r]
    (-> (apply f r)
        :body
        (cheshire/parse-string true))))

(defn as-json [f]
  (fn [url opts]
    (->
     (f url (conj {:as :json
                   :coerce :always}
                  opts))
     :body)))

(def get-json (as-json client/get))
(def post-json (as-json client/post))
