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
    (dom/pre nil (str k " " v))))
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

(defui TextItem
  static om/Ident
  (ident [this props]
    [:item/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id text]} (om/props this)]
      (dom/li nil text))))
(def item-component (om/factory TextItem {:keyfn :id}))

(defui TextList
  static om/Ident
  (ident [this props]
    [:list/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id items]} (om/props this)]
      (dom/ul nil
              (for [item items]
                (item-component {:id item :text item}))))))
(def list-component (om/factory TextList {:keyfn :id}))

(defui Label
  Object
  (render [this]
    (let [{:keys [id text]} (om/props this)]
      (dom/label nil text))))
(def label-component (om/factory Label {:keyfn :id}))

(defui BadByIds
  static om/Ident
  (ident [this props]
    [:bads/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id bads-map]} (om/props this)]
      (dom/ul nil (str id)
              (for [bad bads-map]
                (one-bad-component {:id (first bad) :bad bad}))))))
(def bad-by-ids-component (om/factory BadByIds {:keyfn :id}))

(defn okay? [check-result]
  (let [{:keys [failed-assumption not-normalized-not-ids not-normalized-ids]} check-result]
    (or failed-assumption (seq not-normalized-not-ids) (seq not-normalized-ids))))

(defui DisplayDb
       Object
       (render [this]
               (let [props (om/props this)
                     {:keys [not-normalized-ids not-normalized-not-ids failed-assumption version]} props
                     _ (assert version)
                     ;_ (println "not-normalized-not-ids:" not-normalized-not-ids
                     ;           ", not-normalized-ids:" not-normalized-ids
                     ;           ", failed-assumptions:" failed-assumption)
                     report-problem (okay? props)
                     ]
                 (when report-problem
                   (dom/div nil (dom/h3 nil (str "default-db-format"))
                                (dom/h4 nil (str "ver:" version))
                            (if failed-assumption
                              (dom/div nil (str "Failed assumption: \"" failed-assumption "\""))
                              (dom/div nil
                                       (when (seq not-normalized-not-ids)
                                         (dom/div nil "Normalization problems:"
                                                  (list-component {:id "Normalization problems" :items not-normalized-not-ids}))
                                         )
                                       (when (seq not-normalized-ids)
                                         (dom/div nil "Not normalized id problems:"
                                                  (for [by-id (into {} not-normalized-ids)
                                                        :let [present-lower {:id (first by-id) :bads-map (second by-id)}]]
                                                    (bad-by-ids-component present-lower)))))))))))
(def display-db-component (om/factory DisplayDb {:keyfn :id}))
