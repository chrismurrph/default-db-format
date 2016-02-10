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
         "Should show where not normalized in gases db (3 problems)"
         (fn [props _] (components/display-db-component @props))
         (core/check gases/gas-norm-state)
         {:inspect-data false})

(defcard card-2
         "Now with even more problems"
         (fn [props _] (components/display-db-component @props))
         (core/check gases/include-non-id-problem)
         {:inspect-data false})

(defcard card-3
         "Kanban db (expect to show problem because no use of / in names)"
         (fn [props _] (components/display-db-component @props))
         (core/check kanban/kanban-norm-state)
         {:inspect-data false})

(defcard card-4
         "Kanban db (now fixed so there is use of / in names)"
         (fn [props _] (components/display-db-component @props))
         (core/check kanban/kanban-corrected-norm-state)
         {:inspect-data false})

(defcard card-5
         "Where you give nil state to core/check"
         (fn [props _] (components/display-db-component @props))
         (core/check nil)
         {:inspect-data false})

(defcard card-6
         "Where you give empty state to core/check"
         (fn [props _] (components/display-db-component @props))
         (core/check {})
         {:inspect-data false})
