(ns gui.core
  (:require
   [gui.view :as view]
   [gui.dao :as dao]
   [gui.dao-stub :as dp-stub]
   [gui.dao-jira :as dp-jira]
   )
  (:gen-class))

;; TODO: Parse user style CLI arg for provider (string to a real provider)
(defn -main
  "Provider should be the implementation for fetching tickets."
  [& args]
  (prn args)
  (let [provider (or (first args)
                     (dp-stub/provider))]
    (println "Hello, World!")
    (prn provider)
    (gui.dao/set-provider provider)
    ;; (gui.dao/set-provider (dp-jira/provider))
    (view/main)))
