(ns cards.cards
  (:require
    [default-db-format.core :as core]
    [default-db-format.components :as components]
    [examples.examples :as examples]
    [om.dom :as dom]
    )
  (:require-macros
    [devcards.core :as dc :refer [defcard]]))

(enable-console-print!)

(defcard card-1
         "Where not normalized in gases db"
         (fn [props _] (components/display-db-component @props))
         {:result ((core/check examples/gas-graph))}
         {:inspect-data false})
