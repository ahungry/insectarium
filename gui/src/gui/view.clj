(ns gui.view
  (:require
   [cljfx.api :as fx])
  (:import
   [javafx.scene.input KeyCode KeyEvent]
   [javafx.scene.paint Color]
   [javafx.scene.canvas Canvas]))

(def *state (atom {:stub nil}))

(defn event-handler [event]
  (case (:event/type event)
    ::stub (swap! *state assoc-in [:stub] (:fx/event event))))

(defn text-input [{:keys [text]}]
  {:fx/type :text-area
   :style {:-fx-font-family "monospace"}
   :text text
   :on-text-changed {:event/type ::stub}})

(defn root [{:keys [stub]}]
  {:fx/type :stage
   :showing true
   :title "insectarium"
   :width 800
   :height 600
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :alignment :center
                  :children
                  [
                   {:fx/type text-input
                    :text stub}]}}})

(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)
   :opts {:fx.opt/map-event-handler event-handler}))

(defn main [& args]
  (fx/mount-renderer *state renderer))
