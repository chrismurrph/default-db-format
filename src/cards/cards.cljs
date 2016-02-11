(ns cards.cards
  (:require
    [default-db-format.core :refer [display check]]
    [examples.gases :as gases]
    [examples.kanban :as kanban]
    [om.dom :as dom]
    )
  (:require-macros
    [devcards.core :as dc :refer [defcard]]))

(enable-console-print!)

(def irrelevant-keys #{:graph/labels-visible?
                       :graph/hover-pos
                       :graph/args
                       :graph/translators
                       :graph/init
                       :graph/last-mouse-moment})

(defcard card-22
         "#####Real project, fixed component idents and saying that colour maps are okay"
         (fn [props _] (display @props))
         (check {:excluded-keys irrelevant-keys
                      :okay-value-maps #{[:r :g :b]}} gases/real-project-fixed-component-idents)
         {:inspect-data false})

(defcard card-23
         "#####Real project, fixed component idents"
         (fn [props _] (display @props))
         (check {:excluded-keys irrelevant-keys} gases/real-project-fixed-component-idents)
         {:inspect-data false})

(defcard card-24
         "#####Real project, lots of problems"
         (fn [props _] (display @props))
         (check {:excluded-keys irrelevant-keys} gases/from-real-project)
         {:inspect-data false})

(defcard card-25
         "#####Should show where not normalized in gases db (1 problem plus 3 id problems)"
         (fn [props _] (display @props))
         (check gases/gas-norm-state)
         {:inspect-data false})

(defcard card-26
         "#####Now with even more problems"
         (fn [props _] (display @props))
         (check gases/include-non-id-problem)
         {:inspect-data false})

(defcard card-27
         "#####Kanban db (expect to show problem because no use of / in names)"
         (fn [props _] (display @props))
         (check kanban/kanban-norm-state)
         {:inspect-data false})

(defcard card-28
         "#####Kanban db (now fixed so there is use of / in names)"
         (fn [props _] (display @props))
         (check kanban/kanban-corrected-norm-state)
         {:inspect-data false})

(defcard card-29
         "#####Where you give nil state to core/check"
         (fn [props _] (display @props))
         (check nil)
         {:inspect-data false})

(defcard card-30
         "#####Where you give empty state to core/check"
         (fn [props _] (display @props))
         (check {})
         {:inspect-data false})
