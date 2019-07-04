(ns gui.view
  (:require
   [cljfx.api :as fx]
   [gui.dao :as dao])
  (:import
   [javafx.scene.input KeyCode KeyEvent]
   [javafx.scene.paint Color]
   [javafx.scene.canvas Canvas]))

(def *state (atom {:stub nil
                   :ticket-tabs []
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

(defn swap-and-no-set [xs event]
  (swap! *state assoc-in xs (:fx/event event)))

(defn swap-and-set [xs event]
  (->
   (swap! *state assoc-in xs (:fx/event event))
   set-tickets-from-state
   set-ticket-from-state))

(defn add-ticket-tab
  "We do not have a good way to communicate from the generic View button
  to find the active list item, so we can instead rely on the last state setting
  for the current selected ticket in the list view."
  [{:keys [ticket]}]
  (let [ticket-id (:id ticket)]
    (when ticket-id
      (swap! *state update-in [:ticket-tabs] conj ticket-id))))

(defn get-ticket-tabs [] (-> @*state :ticket-tabs))

(defn event-handler [event]
  (case (:event/type event)
    ::open-ticket (add-ticket-tab @*state)
    ::search (set-tickets-from-state @*state)
    ::set-ticket-id (swap-and-no-set [:ticket] event)
    ::set-ticket (set-ticket-from-state @*state)
    ::stub (swap-and-no-set [:stub] event)
    (prn "Unhandled event: " event)))

(defn text-input [{:keys [label text event-type]}]
  {:fx/type :v-box
   :spacing 5
   :padding 5
   :children
   [{:fx/type :label :text label}
    {:fx/type :text-area
     :style {:-fx-font-family "monospace"}
     :text text
     :on-text-changed {:event/type event-type}}]})

(defn button [{:keys [text event-type]}]
  {:fx/type :button
   :text text
   :on-action {:event/type event-type}})

(defn ticket-button [{:keys [text]} ]
  (button {:text (str "View " text) :event-type ::open-ticket}))

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

(defn render-ticket-tab [ticket-id]
  {:fx/type :tab :text (str ticket-id)
   :content {:fx/type :label :text "Coming soon"}})

(defn render-ticket-tabs [main-children-map ticket-tabs]
  (->>
   (concat
    [{:fx/type :tab :text "Main" :closable false
      :content main-children-map}]
    (map render-ticket-tab ticket-tabs))
   (into [])))

(defn root [{:keys [stub ticket ticket-tabs tickets]}]
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
                   {:fx/type :tab-pane
                    :tabs
                    (render-ticket-tabs
                     {:fx/type :v-box
                      :children
                      [
                       {:fx/type text-input :label "Query params" :text stub :event-type ::stub}
                       {:fx/type search-button}
                       {:fx/type ticket-list :tickets tickets}
                       {:fx/type ticket-button}
                       {:fx/type text-input :label "Ticket Preview" :text (:description ticket)}]}
                     ticket-tabs)}
                   ]}}})

(defn renderer []
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)
   :opts {:fx.opt/map-event-handler event-handler}))

(defn main [& args]
  (set-tickets-from-state *state)
  (fx/mount-renderer *state (renderer)))
