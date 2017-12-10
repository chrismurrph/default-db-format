;;
;; Straight steal from fulcro-inspect, source of a lot of good stuff
;;
(ns general.card-helpers
  (:require [fulcro-css.css :as css]
            [fulcro.client.dom :as dom]
            [fulcro.client.primitives :as prim]))

(defn make-root [Root app-id]
  (prim/ui
    static prim/InitialAppState
    (initial-state [_ params] {:fulcro.inspect.core/app-id app-id
                               :ui/react-key (random-uuid)
                               :ui/root      (prim/get-initial-state Root params)})

    static prim/IQuery
    (query [_] [:ui/react-key
                {:ui/root (prim/get-query Root)}])

    static css/CSS
    (local-rules [_] [])
    (include-children [_] [Root])

    Object
    (render [this]
      (let [{:ui/keys [react-key root]} (prim/props this)
            factory (prim/factory Root)]
        (dom/div #js {:key react-key}
          (factory root))))))

(defn init-state-atom [comp data]
  (atom (prim/tree->db comp (prim/get-initial-state comp data) true)))
