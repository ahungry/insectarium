(ns gui.view
  (:require
   [clojure.java.shell]
   [cljfx.api :as fx]
   [gui.dao :as dao]
   [gui.config :as config]
   [gui.util :as util])
  (:import
   [javafx.scene.input KeyCode KeyEvent]
   [javafx.scene.paint Color]
   [javafx.scene.canvas Canvas]))

(defn maybe-get-first-provider []
  (if (> (count (config/get-providers)) 0)
    (-> (config/get-providers) first str (subs 1))
    "stub"))

;; TODO: Set initial stub value from the provider
(def state-defaults
  {:stub "assignee = currentUser()
AND resolution IS EMPTY
ORDER BY priority, createdDate DESC"
   :direct-ticket-id nil
   :direct-ticket-provider (maybe-get-first-provider)
   :fast-filter ""
   :active-tab "Main"
   :ticket-tabs []
   :ticket nil
   :tickets-filtered []
   :tickets []})

(def *state (atom state-defaults))

(defn state-defaults! []
  (reset! *state state-defaults))

(defn get-active-tab [] (:active-tab @*state))

(defn set-ticket [ticket]
  (swap! *state assoc-in [:ticket] ticket))

(defn set-tickets [tickets]
  (swap! *state assoc-in [:tickets-filtered] tickets)
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

(defn add-ticket-tab-by-id [ticket-id provider]
  (when ticket-id
    (swap! *state update-in [:ticket-tabs] conj {:id ticket-id
                                                 :provider provider})))

(defn add-ticket-tab
  "We do not have a good way to communicate from the generic View button
  to find the active list item, so we can instead rely on the last state setting
  for the current selected ticket in the list view."
  [{:keys [ticket]}]
  (let [ticket-id (:id ticket)
        ticket-provider (:provider ticket)]
    (add-ticket-tab-by-id ticket-id ticket-provider)))

(defn add-ticket-tab-all
  "Try to open a new tab for every ticket in the user list of items."
  []
  (doall (map (fn [{:keys [id provider]}] (add-ticket-tab-by-id id provider))
              (:tickets-filtered @*state))))

(defn add-browser-tab-by-id [ticket-id provider]
  (when ticket-id
    (clojure.java.shell/sh "firefox" (dao/get-browser-url provider ticket-id))))

(defn add-browser-tab [{:keys [ticket]}]
  (let [ticket-id (:id ticket)
        provider (:provider ticket)]
    (add-browser-tab-by-id ticket-id provider)))

(defn get-ticket-tabs [] (-> @*state :ticket-tabs))

(defn remove-tab-by-id [ticket-id]
  (swap! *state update-in [:ticket-tabs] (fn [xs] (filter #(not (= ticket-id (:id %))) xs))))

(defn remove-tab [event]
  (let [ticket-id (-> (:fx/event event) .getSource .getText)]
    (remove-tab-by-id ticket-id)))

(defn close-ticket-tab-all []
  (let [ticket-ids (map :id (get-ticket-tabs))]
    (doall (map remove-tab-by-id ticket-ids))))

(defn close-tab-key? [^KeyEvent key-event]
  (and (= (.getCode key-event) KeyCode/W)
       (.isControlDown key-event)))

(defn remove-active-tab []
  (prn "Try to remove the tab...")
  (remove-tab-by-id (:active-tab @*state)))

(defn key-handler [event]
  (let [^KeyEvent key-event (:fx/event event)]
    (when (close-tab-key? key-event)
      (remove-active-tab))))

(defn enter-key? [^KeyEvent key-event]
  (= (.getCode key-event) KeyCode/ENTER))

(defn key-handler-text-input-slim [event]
  (let [^KeyEvent key-event (:fx/event event)]
    (when (enter-key? key-event)
      (add-ticket-tab-by-id
       (:direct-ticket-id @*state)
       (keyword (:direct-ticket-provider @*state))))))

(defn set-active-tab [event]
  (prn event)
  (prn (:id event))
  (swap! *state assoc-in [:active-tab] (:id event)))

(defn get-fast-filter []
  (:fast-filter @*state))

(defn get-fast-filtered-tickets [tickets]
  (filter
   #(util/all-matching?
     (get-fast-filter)
     (str (:description %) (:provider %) (:id %) (:title %)))
   tickets))

(defn swap-and-set-fast-filter [event]
  (swap-and-no-set [:fast-filter] event)
  (swap! *state assoc-in [:tickets-filtered]
         (get-fast-filtered-tickets (:tickets @*state))))

(defn event-handler [event]
  (case (:event/type event)
    ::set-direct-ticket-id (swap-and-no-set [:direct-ticket-id] event)
    ::set-direct-ticket-provider (swap-and-no-set [:direct-ticket-provider] event)
    ::text-input-slim-key (key-handler-text-input-slim event)
    ::set-fast-filter (swap-and-set-fast-filter event)
    ::selected-tab (set-active-tab event)
    ::press (key-handler event)
    ::remove-tab (remove-tab event)
    ::open-ticket (add-ticket-tab @*state)
    ::close-ticket-all (close-ticket-tab-all)
    ::open-ticket-all (add-ticket-tab-all)
    ::open-browser (add-browser-tab @*state)
    ::search (set-tickets-from-state @*state)
    ::set-ticket-id (swap-and-no-set [:ticket] event)
    ::set-ticket (set-ticket-from-state @*state)
    ::stub (swap-and-no-set [:stub] event)
    (prn "Unhandled event")
    ;; (prn "Unhandled event: " event)
    ))

(defn text-input [{:keys [label text event-type]}]
  {:fx/type :v-box
   :spacing 5
   :padding 5
   :children
   [{:fx/type :label :text label}
    {:fx/type :text-area
     :wrap-text true
     :style {:-fx-font-family "monospace"}
     :text text
     :on-text-changed {:event/type event-type}}]})

(defn base-button [{:keys [text event-type padding]}]
  {:fx/type :button
   :text text
   :padding padding
   ;; :style {:-fx-background-color "slategray"
   ;;         :-fx-text-fill "#ffffff"}
   :on-action {:event/type event-type}})

(defn button [{:keys [text event-type]}]
  (base-button {:text text
                :event-type event-type
                :padding 5}))

(defn big-button [{:keys [text event-type]}]
  (base-button {:text text
                :event-type event-type
                :padding 15}))

(defn ticket-button-close-all [_]
  (button {:text "Close all open tabs "
           :event-type ::close-ticket-all}))

(defn ticket-button-all [{:keys [text tickets]} ]
  (button {:text (str "Open all " (count tickets) " tickets in Tabs " text)
           :event-type ::open-ticket-all}))

(defn ticket-button [{:keys [text]} ]
  (button {:text (str "Open in Tab " text) :event-type ::open-ticket}))

(defn browser-button [{:keys [text]} ]
  (button {:text (str "Open in Browser " text) :event-type ::open-browser}))

(defn search-button [& r]
  (big-button {:text "Search" :event-type ::search}))

(defn ticket-list [{:keys [tickets-filtered]}]
  {:fx/type :list-view
   :max-height 150
   :min-width 800
   :on-selected-item-changed {:event/type ::set-ticket-id}
   :cell-factory
   (fn [{:keys [status provider id title]}]
     {:text (format "[%s] %s (%s) {%s}" status title id provider)})
   :items tickets-filtered})

(defn text-input-slim [{:keys [label text event-type]}]
  {:fx/type :h-box
   :children
   [{:fx/type :label :text label}
    {:fx/type :text-field
     :on-text-changed {:event/type event-type}
     :on-key-pressed {:event/type ::text-input-slim-key}
     :text text}]})

(defn render-comment [{:keys [author email description date-created] :as _m}]
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
     :wrap-text true
     :padding 10
     :style {
             ;; https://openjfx.io/javadoc/12/javafx.graphics/javafx/scene/doc-files/cssref.html
             :-fx-font-family "sans-serif"
             :-fx-font-size "12px"
             :-fx-text-fill "#333333"
             :-fx-background-color "#eeeeee"
             }
     :text (str description)}]})

(defn get-ticket-tab-children [{:keys [id description comments title]}]
  [
   {:fx/type :label :text (str id)}
   {:fx/type text-input-slim
    :event-type ::set-direct-ticket-id
    :label "Title:"
    :text title}
   {:fx/type text-input :label "Description:" :text description}
   {:fx/type :label :text "Comments:"}
   {:fx/type :scroll-pane
    :fit-to-width true
    :content
    {:fx/type :v-box :children (map render-comment comments)}}
   ])

(defn get-ticket-from-state [ticket-id provider]
  (let [ticket-key (keyword ticket-id)
        ticket (ticket-key @*state)]
    (if ticket
      ticket
      (do
        (future
          (swap! *state assoc-in [ticket-key] (dao/get-ticket provider ticket-id)))
        {:comments []}))))

(defn render-ticket-tab
  "We should probably be ok without future/promise here, as the
  get-ticket call should be memoized, and it should have already
  kicked off when the user chose it in the list view (to pop it up in
  the preview pane)."
  [{:keys [id provider]}]
  (let [ticket (get-ticket-from-state id provider)]
    {:fx/type :tab :text (format "%s %s" provider id)
     :on-closed {:event/type ::remove-tab}
     :on-selection-changed {:event/type ::selected-tab
                            :provider provider
                            :id id}
     :content
     {:fx/type :v-box
      :children
      (get-ticket-tab-children ticket)
      }}))

(defn render-ticket-tabs [main-children-map ticket-tabs]
  (->>
   (concat
    [{:fx/type :tab :text "Main" :closable false
      :style {:-fx-background-color "#ffffff"
              :-fx-font "16px monospace"}
      :content main-children-map}]
    (map render-ticket-tab ticket-tabs))
   (into [])))

(defn main-tab
  "The Main tab area (as opposed to the tabbed ticket view)."
  [{:keys [direct-ticket-id direct-ticket-provider fast-filter stub tickets-filtered tickets ticket]}]
  {:fx/type :v-box
   :children
   [
    {:fx/type text-input :label "Query params" :text stub :event-type ::stub}
    {:fx/type :h-box
     :padding 30
     :children
     [
      {:fx/type :v-box
       :children
       [
        {:fx/type search-button}
        {:fx/type text-input-slim :label "Ticket ID (press Enter to open): "
         :text direct-ticket-id :event-type ::set-direct-ticket-id}
        {:fx/type text-input-slim :label "Provider (press Enter to open): "
         :text direct-ticket-provider :event-type ::set-direct-ticket-provider}
        {:fx/type text-input-slim :label "Fast Filter (narrow list)"
         :text fast-filter :event-type ::set-fast-filter}
        {:fx/type ticket-button}
        {:fx/type ticket-button-all :tickets tickets-filtered}
        {:fx/type ticket-button-close-all :tickets tickets}
        {:fx/type browser-button}
        ]}
      {:fx/type ticket-list :tickets-filtered tickets-filtered}]}
    {:fx/type :label :text (str "Status: " (:status ticket))}
    {:fx/type text-input :label "Ticket Preview" :text (:description ticket)}]})

(defn root [{:keys [ticket-tabs] :as m}]
  {:fx/type :stage
   :showing true
   :title "insectarium"
   :width 1200
   :height 900
   :scene {:fx/type :scene
           :fill :transparent
           :stylesheets #{"styles.css"}
           :root {:fx/type :v-box
                  ;; :alignment :top
                  :style {
                          ;; :-fx-font-family "monospace"
                          :-fx-background-color "beige"
                          }
                  :on-key-pressed {:event/type ::press}
                  :children
                  [
                   {:fx/type :tab-pane
                    :tabs
                    (render-ticket-tabs (main-tab m) ticket-tabs)}
                   ]}}})

(defn renderer []
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)
   :opts {:fx.opt/map-event-handler event-handler}))

(defn main [& args]
  (state-defaults!)
  (future
    (set-tickets (dao/get-tickets (:stub @*state)))
    (renderer))
  (swap-and-set-fast-filter "")
  (fx/mount-renderer *state (renderer)))
