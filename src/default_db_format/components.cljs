(ns default-db-format.components
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui OneBad
  static om/Ident
  (ident [this props]
    [:one-bad/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [bad]} (om/props this)]
      (dom/li nil (dom/pre nil (apply str bad))))))
(def one-bad-component (om/factory OneBad {:keyfn :id}))

(defui BadByIds
  static om/Ident
  (ident [this props]
    [:bads/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id bads-map]} (om/props this)]
      (dom/ul nil
              (for [bad bads-map]
                (one-bad-component {:id (first bad) :bad bad}))))))
(def bad-by-ids-component (om/factory BadByIds {:keyfn :id}))

(defui DisplayDb
       Object
       (render [this]
               (let [props (om/props this)
                     {:keys [result]} props
                     not-normalized (into {} (:not-normalized result))
                     ]
                 (dom/div nil (for [by-id not-normalized
                                    :let [present-lower {:id (first by-id) :bads-map (second by-id)}]]
                                (bad-by-ids-component present-lower))))))
(def display-db-component (om/factory DisplayDb {:keyfn :id}))
