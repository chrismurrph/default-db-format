(ns default-db-format.baby-sharks
  (:require [fulcro.client.primitives :as prim]
            [fulcro.client.mutations :as m]
            [fulcro.client.dom :as dom]
            [fulcro.client.util :as fu]
            [fulcro.client.cards :refer [defcard-fulcro fulcro-application]]
            [fulcro.client.primitives :as prim :refer [defui defsc InitialAppState initial-state]]
            [fulcro-css.css :as css]
    ;; Don't delete
            [default-db-format.ui.domain :as ui.domain]
            [cljs.pprint :refer [pprint]]
            [fulcro.util :refer [unique-key]]
            [devcards.core]
            [default-db-format.dev :as dev]))

(def global-css (css/get-classnames ui.domain/CSS))

(defsc Baby
       [this props _]
       {:ident [:baby/id :db/id]
        :query [:db/id :baby/first-name]}
       (dom/div nil "Blab blab"))

(def big-map {:db/id     1
              :some/text "Surely I s/be in the tables"
              :a/b       "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
              :b/b       "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})

(m/defmutation map-at-join
  [{:keys []}]
  (action [{:keys [state]}]
          (swap! state assoc-in [:thing/by-id 1]
                 {:label                      "I'm not at an id, which sometimes happens"
                  :some-kind-of-long/bad-join big-map})))

(m/defmutation map-at-link
  [{:keys []}]
  (action [{:keys [state]}]
          ;; If didn't namespace this it wouldn't be caught
          (swap! state assoc :here-is/some-link {:a "b"})))

(defn x-1 []
  (dev/summarize big-map))

(m/defmutation normalize
  "Remove those bad things"
  [{:keys []}]
  (action [{:keys [state]}]
          (swap! state dissoc :thing/by-id :here-is/some-link)))

(m/defmutation state-becomes-empty
  [{:keys []}]
  (action [{:keys [state]}]
          (reset! state {})))

(declare say-hello-fulcro-app)

(defn get-state []
  @(prim/app-state (-> say-hello-fulcro-app deref :reconciler)))

(def dump get-state)

(defn get-reconciler []
  (some-> say-hello-fulcro-app deref :reconciler))

(defsc Adult
       [this {:adult/keys [first-name babies]} _]
       {:initial-state
                     (fn [{:keys [babies-in]}]
                       {:db/id 1 :adult/first-name "Mama Shark" :adult/babies babies-in})
        :ident       [:adult/by-id :db/id]
        :query       [:db/id :adult/first-name {:adult/babies (prim/get-query Baby)}]
        :css-include [ui.domain/CSS]}
       (let [title (str "Good afternoon, my name is " first-name " with " (count babies) " shark babies")]
         (dom/div nil
                  (dom/div #js {:className (:display-name global-css)} title)
                  (dom/div nil
                           (dom/button #js {:onClick #(prim/transact! this [`(map-at-join)])}
                                       "Give a field-join a map")
                           (dom/button #js {:onClick #(prim/transact! this [`(normalize)])}
                                       "Restore order..."))
                  (dom/div nil
                           (dom/button #js {:onClick #(prim/transact! this [`(map-at-link)])}
                                       "Give a root-join a map")
                           (dom/button #js {:onClick #(prim/transact! this [`(normalize)])}
                                       "Restore order..."))
                  (dom/div nil
                           ;; Making state a vector actually crashes Fulcro
                           (dom/button #js {:onClick #(prim/transact! this [`(state-becomes-empty)])}
                                       "Empty state (refresh page after)"))
                  (dom/div nil
                           (dom/button #js {:onClick #(if-let [rec (get-reconciler)]
                                                        (fu/force-render rec)
                                                        (println "No reconciler"))}
                                       "Force root render (just for fun)")))))

(defui ^:once AdultRoot
       static prim/InitialAppState
       (initial-state [_ params] {:ui/react-key (random-uuid)
                                  :ui/root      (prim/get-initial-state Adult params)})

       static prim/IQuery
       (query [_] [:ui/react-key
                   {:ui/root (prim/get-query Adult)}])

       static css/CSS
       (local-rules [_] [])
       (include-children [_] [Adult])

       Object
       (render [this]
               (let [{:ui/keys [react-key root]} (prim/props this)
                     adult-component (prim/factory Adult)]
                 (dom/div #js {:key react-key}
                          (adult-component root)))))

(def initial-babies [{:db/id 1 :baby/first-name "Baby Shark 1"}
                     {:db/id 2 :baby/first-name "Baby Shark 2"}])
(defcard-fulcro say-hello
                AdultRoot
                (dev/init-state-atom AdultRoot
                                     {:babies-in initial-babies})
                {:inspect-data true})

(css/upsert-css "adult" AdultRoot)
