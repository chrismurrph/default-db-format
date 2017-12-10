(ns default-db-format.user-can-denormalize
  (:require [fulcro.client.primitives :as prim]
            [fulcro.client.mutations :as m]
            [fulcro.client.dom :as dom]
            [fulcro.client.cards :refer [defcard-fulcro fulcro-application]]
            [general.card-helpers :as card-helpers]
            [fulcro.client.primitives :as prim :refer [defui InitialAppState initial-state]]
            [fulcro-css.css :as css]
            [default-db-format.ui.domain :as ui-domain]
            [default-db-format.core :as db-format]
            [default-db-format.watcher :as watcher]
            [cljs.pprint :refer [pprint]]
            [fulcro.util :refer [unique-key]]))

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

(prim/defsc Baby
            [this props _]
            {:ident [:baby/by-id :db/id]
             :query [:db/id :baby/first-name]}
            (dom/div nil "Blab blab"))

(m/defmutation denormalize
               "Put a thing"
               [{:keys []}]
  (action [{:keys [state]}]
          (swap! state assoc-in [:thing/by-id 1] {:label "I'm not at an id, so I'm not normalized"
                                                  :bad-join {:db/id 1
                                                             :some/text "Surely I s/be in the tables"}})
          ))

(m/defmutation normalize
  "Remove a thing"
  [{:keys []}]
  (action [{:keys [state]}]
          (swap! state dissoc :thing/by-id)
          ))

;;
;; Haven't got reconciler, so just do in a mutation
;;
(m/defmutation force-root-render
  [{:keys []}]
  (action [{:keys [state]}]
          (swap! state assoc :ui/react-key (unique-key))
          ))

(prim/defsc Adult
            [this {:adult/keys [first-name babies]} _]
            {:initial-state
                    (fn [{:keys [babies-in]}]
                      {:db/id 1 :adult/first-name "Mama Shark" :adult/babies babies-in})
             :ident [:adult/by-id :db/id]
             :query [:db/id :adult/first-name {:adult/babies (prim/get-query Baby)}]
             :css-include   [ui-domain/CSS]}
            (let [title (str "Good afternoon, my name is " first-name " with " (count babies) " shark babies")]
              (dom/div nil
                       (dom/div #js {:className (:display-name scss)} title)
                       (dom/div nil
                                (dom/button #js {:onClick #(prim/transact! this [`(denormalize)])}
                                            "Cause state chaos!")
                                (dom/button #js {:onClick #(prim/transact! this [`(normalize)])}
                                            "Restore order..."))
                       (dom/div nil
                                (dom/button #js {:onClick #(prim/transact! this [`(force-root-render)])}
                                            "Force root render")))))

(declare say-hello-fulcro-app)

;(def AdultRoot (card-helpers/make-root Adult ::adult))
(def root-atom (atom nil))
(defn get-adult-root []
  (or @root-atom (reset! root-atom
                         (card-helpers/make-root Adult
                                                 ::adult
                                                 say-hello-fulcro-app
                                                 check-default-db))))

(def initial-babies [{:db/id 2 :baby/first-name "Baby Shark 1"}
                     {:db/id 3 :baby/first-name "Baby Shark 2"}])
(defcard-fulcro say-hello
                (get-adult-root)
                (card-helpers/init-state-atom (get-adult-root)
                                              {:babies-in initial-babies})
                {:inspect-data true})

(css/upsert-css "adult" (get-adult-root))
