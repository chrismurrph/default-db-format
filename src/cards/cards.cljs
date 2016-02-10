(ns cards.cards
  (:require
    [default-db-format.core :as core]
    [default-db-format.components :as components]
    [examples.gases :as gases]
    [examples.kanban :as kanban]
    [om.dom :as dom]
    )
  (:require-macros
    [devcards.core :as dc :refer [defcard]]))

(enable-console-print!)

(defcard card-1
         "Where not normalized in gases db"
         (fn [props _] (components/display-db-component @props))
         ((core/check gases/gas-denorm-state))
         {:inspect-data false})

(defcard card-2
         "Where not normalized in kanban db (expect to show nothing)"
         (fn [props _] (components/display-db-component @props))
         ((core/check kanban/kanban-denorm-state))
         {:inspect-data false})

(defcard card-3
         "Where you give nil state to core/check"
         (fn [props _] (components/display-db-component @props))
         ((core/check nil))
         {:inspect-data false})

(defcard card-4
         "Where you give empty state to core/check"
         (fn [props _] (components/display-db-component @props))
         ((core/check {}))
         {:inspect-data false})
