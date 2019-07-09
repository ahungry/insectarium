(ns gui.core
  (:require
   [gui.config :as config]
   [gui.view :as view]
   [gui.dao :as dao]
   )
  (:import [javafx.application Platform])
  (:gen-class))

(defn main
  "Provider should be the implementation for fetching tickets."
  [& [provider]]
  (dao/set-provider-from-config! provider)
  (view/main))

(defn -main [& args]
  (Platform/setImplicitExit true)
  (apply main args))
