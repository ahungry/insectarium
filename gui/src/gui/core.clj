(ns gui.core
  (:require
   [gui.view :as view]
   [gui.dao :as dao]
   [gui.dao-stub :as dp-stub]
   [gui.dao-jira :as dp-jira]
   )
  (:import [javafx.application Platform])
  (:gen-class))

(defn get-provider! [s opts]
  (case s
    "jira" (dp-jira/provider! opts)
    "stub" (dp-stub/provider! opts)
    (dp-stub/provider! opts)))

;; TODO: Parse user style CLI arg for provider (string to a real provider)
(defn main
  "Provider should be the implementation for fetching tickets."
  [& args]
  (let [provider (or (first args) "stub")
        domain (or (second args) "http://example.com")]
    (prn "Found provider: " provider)
    (gui.dao/set-provider! (get-provider! provider {:domain domain}))
    ;; (gui.dao/set-provider (dp-jira/provider))
    (view/main)))

(defn -main [& args]
  (Platform/setImplicitExit true)
  (apply main args))
