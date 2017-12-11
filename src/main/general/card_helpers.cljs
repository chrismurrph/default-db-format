(ns general.card-helpers
  (:require [fulcro-css.css :as css]
            [fulcro.client.dom :as dom]
            [fulcro.client.primitives :as prim]))

;;
;; Straight steal from fulcro-inspect, source of a lot of good stuff
;;

(defn init-state-atom [comp data]
  (atom (prim/tree->db comp (prim/get-initial-state comp data) true)))
