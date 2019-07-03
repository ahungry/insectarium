(ns gui.view
  (:require
   [cljfx.api :as fx]
   [gui.dao :as dao])
  (:import
   [javafx.scene.input KeyCode KeyEvent]
   [javafx.scene.paint Color]
   [javafx.scene.canvas Canvas]))

(def *state (atom {:stub nil
                   :tickets []}))

(defn set-tickets [tickets]
  (swap! *state assoc-in [:tickets] tickets))

(defn set-tickets-from-state [{:keys [stub]}]
  (set-tickets (dao/get-tickets {:stub stub})))

(defn swap-and-set [xs event]
  (->
   (swap! *state assoc-in xs (:fx/event event))
   set-tickets-from-state))

(defn event-handler [event]
  (case (:event/type event)
    ::set-ticket (swap-and-set [:ticket] event)
    ::stub (swap-and-set [:stub] event)))

(defn text-input [{:keys [text]}]
  {:fx/type :text-area
   :style {:-fx-font-family "monospace"}
   :text text
   :on-text-changed {:event/type ::stub}})

(defn ticket-list [{:keys [tickets]}]
  {:fx/type :list-view
   :on-selected-item-changed {:event/type ::set-ticket}
   :cell-factory
   (fn [{:keys [title]}]
     {:text title})
   :items tickets})

(defn root [{:keys [stub tickets]}]
  {:fx/type :stage
   :showing true
   :title "insectarium"
   :width 800
   :height 600
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :alignment :center
                  :children
                  [{:fx/type text-input :text stub}
                   {:fx/type ticket-list :tickets tickets}]}}})

(defn renderer []
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)
   :opts {:fx.opt/map-event-handler event-handler}))

(defn main [& args]
  (set-tickets-from-state *state)
  (fx/mount-renderer *state (renderer)))
