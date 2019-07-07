(ns gui.core
  (:require
   [gui.config :as config]
   [gui.view :as view]
   [gui.dao :as dao]
   [gui.dao-stub :as dp-stub]
   [gui.dao-jira :as dp-jira]
   [gui.dao-github :as dp-github]
   )
  (:import [javafx.application Platform])
  (:gen-class))

(defn get-provider! [s opts]
  (case s
    :jira (dp-jira/provider! opts)
    :github (dp-github/provider! opts)
    :stub (dp-stub/provider! opts)
    (dp-stub/provider! opts)))

(defn main
  "Provider should be the implementation for fetching tickets."
  [& args]
  (config/set-conf!)
  (let [domain (config/get-domain)
        provider (config/get-provider)]
    (prn "Found domain: " domain)
    (prn "Found provider: " provider)
    (gui.dao/set-provider!
     (get-provider! provider {:domain domain
                              :auth (config/get-auth)}))
    (view/main)))

(defn -main [& args]
  (Platform/setImplicitExit true)
  (apply main args))
