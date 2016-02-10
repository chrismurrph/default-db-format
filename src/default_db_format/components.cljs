(ns default-db-format.components
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def tab #js {:style #js {:marginLeft 20}})

;;
;; Yes I'm not good at formatting web pages. No 'proper/external' css as this is just a
;; library/utility. Would be nice just to have fixed widths and the value in red.
;; Will do the css locally ...TODO
;;
(defn format-bad-1 [bad]
  (dom/pre nil (apply str bad)))
(defn format-bad-2 [bad]
  (dom/span nil
            (dom/pre nil (str (first bad)))
            (dom/pre nil (str (second bad)))))
(defn format-bad-3 [bad]
  (let [[k v] bad]
    (dom/span nil
              (dom/pre nil (str k " " v)))))
(defn format-bad-4 [bad]
  (let [[k v] bad]
    (dom/table nil
               (dom/thead nil
                          (dom/tr nil
                                  (dom/th {:width "100px"} (str k))
                                  (dom/th {:width "700px"} (str v)))))))

(defui OneBad
  static om/Ident
  (ident [this props]
    [:one-bad/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id bad]} (om/props this)]
      (dom/li nil (format-bad-3 bad)))))
(def one-bad-component (om/factory OneBad {:keyfn :id}))

(defui BadByIds
  static om/Ident
  (ident [this props]
    [:bads/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id bads-map]} (om/props this)
          _ (println "Id ignoring: " id)]
      (dom/ul nil (str id)
              (for [bad bads-map]
                (one-bad-component {:id (first bad) :bad bad}))))))
(def bad-by-ids-component (om/factory BadByIds {:keyfn :id}))

(defui DisplayDb
       Object
       (render [this]
               (let [props (om/props this)
                     {:keys [result]} props
                     not-normalized (into {} (:not-normalized result))
                     failed-assumptions (:failed-assumptions result)
                     _ (println "not-normalized:" (count not-normalized) ", failed-assumptions:" (count failed-assumptions))
                     ]
                 (dom/div nil (for [by-id not-normalized
                                    :let [present-lower {:id (first by-id) :bads-map (second by-id)}]]
                                (bad-by-ids-component present-lower))))))
(def display-db-component (om/factory DisplayDb {:keyfn :id}))
