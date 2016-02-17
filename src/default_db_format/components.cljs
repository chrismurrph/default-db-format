(ns default-db-format.components
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def reddish "#E11F1F")
(def hard-left #js {:style #js {:margin 15 :padding 0}})
(def reddish-style #js {:style #js {:color reddish}})
(def pad-in #js {:style #js {:margin 20 :padding 0}})

(defn format-bad-3 [bad]
  (let [[k v] bad]
    (dom/pre nil (str k " " v))))

(defui OneBad
  static om/Ident
  (ident [this props]
    [:one-bad/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id bad]} (om/props this)]
      (dom/li pad-in (format-bad-3 bad)))))
(def one-bad-component (om/factory OneBad {:keyfn :id}))

(def allow-follow-on #js {:style #js {:whiteSpace "pre"}})
(def follow-on #js {:style #js {:display "inline-block"}})
(def reddish-follow-on #js {:style #js {:display "inline-block" :color reddish}})

(defui NonIdsTextItem
  static om/Ident
  (ident [this props]
    [:item/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id text problem]} (om/props this)]
      (dom/li nil (dom/span nil (dom/span nil text ": ") (dom/span reddish-follow-on (str problem)))))))
(def non-ids-item-component (om/factory NonIdsTextItem {:keyfn :id}))

(defui NonIdsTextList
  static om/Ident
  (ident [this props]
    [:list/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id items]} (om/props this)]
      (dom/ul nil
              (for [item items
                    :let [{:keys [text problem]} item
                          _ (assert text)
                          _ (assert problem)]]
                (non-ids-item-component {:id (str text problem) :text text :problem problem}))))))
(def non-ids-list-component (om/factory NonIdsTextList {:keyfn :id}))

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
      (dom/ul hard-left (dom/span reddish-style (str id))
              (for [bad bads-map]
                (one-bad-component {:id (first bad) :bad bad}))))))
(def bad-by-ids-component (om/factory BadByIds {:keyfn :id}))

(defn okay? [check-result]
  (let [{:keys [failed-assumption not-normalized-not-ids not-normalized-ids]} check-result]
    (not (or failed-assumption (seq not-normalized-not-ids) (seq not-normalized-ids)))))

(def coloured-follow-on #js {:style #js {:display "inline-block" :color "#0000FF"}})

;;
;; Later in html do equiv of this:
;; (apply str (interpose ", " are-not-slashed))
;; Also after comment might want to append: ", see: "
;;
(defn poor-assump-div [failed-assumption]
  (let [{:keys [text problems]} failed-assumption
        boiler-text (str "Failed assumption: \"" text "\"")]
    (if (nil? problems)
      (dom/div nil boiler-text)
      (dom/div allow-follow-on
               (dom/span follow-on (str boiler-text ", see: "))
               (dom/span reddish-follow-on (apply str (interpose ", " problems)))))))

(defui DisplayDb
       Object
       (render [this]
               (let [props (om/props this)
                     {:keys [not-normalized-ids not-normalized-not-ids failed-assumption version]} props
                     _ (assert version)
                     report-problem? (not (okay? props))]
                 (when report-problem?
                   (dom/div nil (dom/span allow-follow-on
                                          (dom/h3 coloured-follow-on (str "default-db-format"))
                                          (dom/h4 follow-on (str "  (ver: " version ")")))
                            (if failed-assumption
                              (poor-assump-div failed-assumption)
                              (dom/div nil
                                       (when (seq not-normalized-not-ids)
                                         (dom/div nil "Normalization problems:"
                                                  (non-ids-list-component {:id "Normalization problems" :items not-normalized-not-ids}))
                                         )
                                       (when (seq not-normalized-ids)
                                         (dom/div nil "Not normalized id problems:"
                                                  (for [by-id (into {} not-normalized-ids)
                                                        :let [present-lower {:id (first by-id) :bads-map (second by-id)}]]
                                                    (bad-by-ids-component present-lower)))))))))))
(def display-db-component (om/factory DisplayDb {:keyfn :id}))

(defui GenericDisplayer
  Object
  (render [this]
    (let [val (om/props this)]
      (dom/pre nil (with-out-str (cljs.pprint/pprint val))))))
(def display (om/factory GenericDisplayer))
