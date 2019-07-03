(ns gui.core
  (:require
   [gui.view :as view]
   [gui.dao :as dao]
   [gui.dao-stub :as dp-stub]
   [gui.dao-jira :as dp-jira]
   )
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (gui.dao/set-provider (dp-stub/provider))
  (view/main))
