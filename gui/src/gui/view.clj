(ns gui.view
  (:require
   [cljfx.api :as fx]
   [gui.dao :as dao])
  (:import
   [javafx.scene.input KeyCode KeyEvent]
   [javafx.scene.paint Color]
   [javafx.scene.canvas Canvas]))

(def *state (atom {:stub nil
                   :ticket nil
                   :tickets []}))

(defn set-ticket [ticket]
  (swap! *state assoc-in [:ticket] ticket))

(defn set-tickets [tickets]
  (swap! *state assoc-in [:tickets] tickets))

(defn set-ticket-from-state [{:keys [stub]}]
  (set-ticket (dao/get-ticket {:stub stub})))

(defn set-tickets-from-state [{:keys [stub]}]
  (set-tickets (dao/get-tickets {:stub stub})))

(defn swap-and-set [xs event]
  (->
   (swap! *state assoc-in xs (:fx/event event))
   set-tickets-from-state
   set-ticket-from-state))

(defn event-handler [event]
  (case (:event/type event)
    ::search (set-tickets-from-state @*state)
    ::set-ticket-id (swap-and-set [:ticket] event)
    ::set-ticket (set-ticket-from-state @*state)
    ::stub (swap-and-set [:stub] event)))

(defn text-input [{:keys [label text]}]
  {:fx/type :v-box
   :spacing 5
   :padding 5
   :children
   [{:fx/type :label :text label}
    {:fx/type :text-area
     :style {:-fx-font-family "monospace"}
     :text text
     :on-text-changed {:event/type ::stub}}]})

(defn button [{:keys [text event-type]}]
  {:fx/type :button
   :text text
   :on-action {:event/type event-type}})

(defn ticket-button [& r]
  (button {:text "Update" :event-type ::search}))

(defn search-button [& r]
  (button {:text "Search" :event-type ::search}))

(defn ticket-list [{:keys [tickets]}]
  {:fx/type :list-view
   :max-height 150
   :on-selected-item-changed {:event/type ::set-ticket-id}
   :cell-factory
   (fn [{:keys [id title]}]
     {:text (format "%s %s" id title)})
   :items tickets})

(defn root [{:keys [stub ticket tickets]}]
  {:fx/type :stage
   :showing true
   :title "insectarium"
   :width 800
   :height 600
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :alignment :center
                  :children
                  [{:fx/type text-input :label "Query params" :text stub}
                   {:fx/type search-button}
                   {:fx/type ticket-list :tickets tickets}
                   {:fx/type text-input :label "Ticket Details" :text (str ticket)}
                   {:fx/type ticket-button}
                   ]}}})

(defn renderer []
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)
   :opts {:fx.opt/map-event-handler event-handler}))

(defn main [& args]
  (set-tickets-from-state *state)
  (fx/mount-renderer *state (renderer)))
