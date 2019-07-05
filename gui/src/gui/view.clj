(ns gui.view
  (:require
   [cljfx.api :as fx]
   [gui.dao :as dao])
  (:import
   [javafx.scene.input KeyCode KeyEvent]
   [javafx.scene.paint Color]
   [javafx.scene.canvas Canvas]))

(def *state (atom {:stub "assignee = currentUser()"
                   :ticket-tabs []
                   :ticket nil
                   :tickets []}))

(defn set-ticket [ticket]
  (swap! *state assoc-in [:ticket] ticket))

(defn set-tickets [tickets]
  (swap! *state assoc-in [:tickets] tickets))

(defn set-ticket-from-state [{:keys [stub]}]
  (prn "Set preview pane from state: " stub)
  (future (set-ticket (dao/get-ticket {:stub stub}))))

(defn set-tickets-from-state [{:keys [stub]}]
  (future (set-tickets (dao/get-tickets stub))))

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

(defn remove-tab [event]
  (let [ticket-id (-> (:fx/event event) .getSource .getText)]
    (swap! *state update-in [:ticket-tabs] (fn [xs] (filter #(not (= ticket-id %)) xs)))))

(defn close-tab-key? [^KeyEvent key-event]
  (and (= (.getCode key-event) KeyCode/W)
       (.isControlDown key-event)))

(defn remove-active-tab []
  (prn "Yea, do it!"))

(defn key-handler [event]
  (let [^KeyEvent key-event (:fx/event event)]
    (when (close-tab-key? key-event)
      (remove-active-tab))))

(defn event-handler [event]
  (case (:event/type event)
    ::press (key-handler event)
    ::remove-tab (remove-tab event)
    ::open-ticket (add-ticket-tab @*state)
    ::search (set-tickets-from-state @*state)
    ::set-ticket-id (swap-and-no-set [:ticket] event)
    ::set-ticket (set-ticket-from-state @*state)
    ::stub (swap-and-no-set [:stub] event)
    (prn "Unhandled event: " event)))

(defn text-input [{:keys [label text event-type]}]
  {:fx/type :v-box
   :min-height 300
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
   :padding 5
   :style {:-fx-background-color "slategray"
           :-fx-text-fill "#ffffff"}
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

(defn text-input-slim [{:keys [label text]}]
  {:fx/type :h-box
   :children
   [{:fx/type :label :text label}
    {:fx/type :text-field :text text :min-width 600}]})

(defn render-comment [{:keys [author email description date-created] :as m}]
  {:fx/type :v-box
   :padding 3
   :children
   [
    {:fx/type :h-box
     :padding 5
     :style {:-fx-font-family "monospace"}
     :children
     [
      {:fx/type :label :padding 3 :text (str "by: " author)}
      {:fx/type :label :padding 3 :text (str "<" email ">")}
      {:fx/type :label :padding 3 :text (str " at " date-created)}
      ]}
    {:fx/type :text-area
     :padding 10
     :style {
             ;; https://openjfx.io/javadoc/12/javafx.graphics/javafx/scene/doc-files/cssref.html
             :-fx-font-family "sans-serif"
             :-fx-font-size "12px"
             :-fx-text-fill "#333333"
             :-fx-background-color "#eeeeee"
             }
     :text (str description)}]})

;; TODO: Ensure we do not do all this, unless its the tab in focus.
(defn render-ticket-tab
  "We should probably be ok without future/promise here, as the
  get-ticket call should be memoized, and it should have already
  kicked off when the user chose it in the list view (to pop it up in
  the preview pane)."
  [ticket-id]
  (let [ticket (dao/get-ticket ticket-id)]
    {:fx/type :tab :text (str ticket-id)
     :on-closed {:event/type ::remove-tab}
     :content
     {:fx/type :v-box
      :children
      [
       {:fx/type :label :text (str (:id ticket))}
       {:fx/type text-input-slim :label "Title:" :text (:title ticket)}
       {:fx/type text-input :label "Description:" :text (:description ticket)}
       {:fx/type :label :text "Comments:"}
       {:fx/type :scroll-pane
        :fit-to-width true
        :content
        {:fx/type :v-box :children (map render-comment (:comments ticket))}}
       ]}}))

(defn render-ticket-tabs [main-children-map ticket-tabs]
  (->>
   (concat
    [{:fx/type :tab :text "Main" :closable false
      :style {:-fx-background-color "#ffffff"
              :-fx-font "16px monospace"}
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
                  :style {
                          ;; :-fx-font-family "monospace"
                          :-fx-background-color "beige"
                          }
                  :on-key-pressed {:event/type ::press}
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
                       {:fx/type :h-box
                        :alignment :center
                        :padding 30
                        :children
                        [{:fx/type ticket-button}
                         {:fx/type ticket-list :tickets tickets}]}
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
