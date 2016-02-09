(ns cards.cards
  (:require
    [om.next :as om :refer-macros [defui]]
    [default-db-format.core :as core]
    [om.dom :as dom]
    )
  (:require-macros
    [devcards.core :as dc :refer [defcard]]))

(enable-console-print!)

(defui DisplayDb
       Object
       (render [this]
         (let [props (om/props this)
               _ (println props)
               {:keys [result]} props
               _ (println result)
               ]
           (dom/pre nil (apply str result)))))
(def display-db-component (om/factory DisplayDb {:keyfn :id}))

(defcard card-1
         "Saying Hi"
         (fn [props _] (display-db-component @props))
         {:result ((core/check core/state))}
         {:inspect-data false})
