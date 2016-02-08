(ns default-db-format.app
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))

(defn init []
  (println "Where's browser console?")
  (om/root widget {:text "On thinking..."}
           {:target (. js/document (getElementById "container"))}))
