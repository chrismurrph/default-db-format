(ns default-db-format.user-can-denormalize
  (:require [fulcro.client.primitives :as prim]
            [fulcro.client.mutations :as m]
            [fulcro.client.dom :as dom]
            [fulcro.client.util :as fu]
            [fulcro.client.cards :refer [defcard-fulcro fulcro-application]]
            [general.card-helpers :as card-helpers]
            [fulcro.client.primitives :as prim :refer [defui defsc InitialAppState initial-state]]
            [fulcro-css.css :as css]
            [default-db-format.ui.domain :as ui-domain]
            [default-db-format.core :as db-format]
            [default-db-format.watcher :as watcher]
            [cljs.pprint :refer [pprint]]
            [fulcro.util :refer [unique-key]]
            [devcards.core]))

(def excluded-keys #{:fulcro.client.routing/routing-tree
                     :fulcro/ready-to-load
                     :fulcro/loads-in-progress
                     :fulcro/server-error
                     :fulcro.ui.forms/form
                     :ui/react-key
                     :ui/locale
                     :ui/loading-data
                     :root/top-router
                     :root/components
                     ;Easier for every if we ignore top level keys that are not seq
                     ;:general.card-helpers/app-id
                     })
(def okay-val-maps #{[:debug-from]})
(def okay-val-vectors #{[:report/balance-sheet :report/big-items-first :report/profit-and-loss :report/trial-balance]})
(def check-config {:excluded-keys      excluded-keys
                   :okay-value-maps    okay-val-maps
                   :okay-value-vectors okay-val-vectors
                   :by-id-kw           "by-id"
                   :routing-ns         "routed"})

(def scss (css/get-classnames ui-domain/CSS))

;;
;; noisey? will show success.
;;
(defn check-default-db [noisey? state]
  (assert (map? state))
  (let [version db-format/version
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        check-result (db-format/check check-config state)
        ok? (db-format/ok? check-result)]
    (when (and noisey? ok?)
      (println (str "GOOD: state fully " msg-boiler)))
    (when (not ok?)
      (println (str "BAD: state not fully " msg-boiler))
      (pprint check-result)                                 ;; <- check-result is a summary of state, so print 'one or *the other*'
      ;(pprint state))      ;; <- *the other*
      (db-format/show-hud check-result))))                  ;; <- must be last, displays check-result in browser

(defsc Baby
       [this props _]
       {:ident [:baby/by-id :db/id]
        :query [:db/id :baby/first-name]}
       (dom/div nil "Blab blab"))

(m/defmutation denormalize
  "Put a thing"
  [{:keys []}]
  (action [{:keys [state]}]
          (swap! state assoc-in [:thing/by-id 1] {:label    "I'm not at an id, so I'm not normalized"
                                                  :bad-join {:db/id     1
                                                             :some/text "Surely I s/be in the tables"}})
          ))

(m/defmutation normalize
  "Remove a thing"
  [{:keys []}]
  (action [{:keys [state]}]
          (swap! state dissoc :thing/by-id)
          ))

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
        :css-include [ui-domain/CSS]}
       (let [title (str "Good afternoon, my name is " first-name " with " (count babies) " shark babies")]
         (dom/div nil
                  (dom/div #js {:className (:display-name scss)} title)
                  (dom/div nil
                           (dom/button #js {:onClick #(prim/transact! this [`(denormalize)])}
                                       "Cause state chaos!")
                           (dom/button #js {:onClick #(prim/transact! this [`(normalize)])}
                                       "Restore order..."))
                  (dom/div nil
                           (dom/button #js {:onClick #(if-let [rec (get-reconciler)]
                                                        (fu/force-render rec)
                                                        (println "No reconciler"))}
                                       "Force root render")))))

(defui ^:once AdultRoot
       static prim/InitialAppState
       (initial-state [_ params] {:default-db-format.tool/app-id ::adult
                                  :ui/react-key                  (random-uuid)
                                  :ui/root                       (prim/get-initial-state Adult params)})

       static prim/IQuery
       (query [_] [:ui/react-key
                   {:ui/root (prim/get-query Adult)}])

       static css/CSS
       (local-rules [_] [])
       (include-children [_] [Adult])

       Object
       (render [this]
               (let [rec (some-> (get-reconciler) deref)
                     {:ui/keys [react-key root]} (prim/props this)
                     adult-component (prim/factory Adult)]
                 ;(println "Have app?" (boolean say-hello-fulcro-app))
                 (dom/div #js {:key react-key}
                          ;(when rec (check-default-db true rec))
                          (adult-component root)))))

(def initial-babies [{:db/id 1 :baby/first-name "Baby Shark 1"}
                     {:db/id 2 :baby/first-name "Baby Shark 2"}])
(defcard-fulcro say-hello
                AdultRoot
                (card-helpers/init-state-atom AdultRoot
                                              {:babies-in initial-babies})
                {:inspect-data true})

(css/upsert-css "adult" AdultRoot)
