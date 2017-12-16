(ns default-db-format.ui.components
  (:require [fulcro.client.primitives :as prim :refer-macros [defui]]
            [fulcro.client.dom :as dom]))

(def reddish "#E11F1F")
(def hard-left #js {:style #js {:margin 15 :padding 0}})
(def reddish-style #js {:style #js {:color reddish}})
(def pad-in #js {:style #js {:margin 20 :padding 0}})

(defn format-bad [bad]
  (let [[k v] bad]
    (dom/pre nil (str k " " v))))

(defui ^:once OneBad
       static prim/Ident
       (ident [this props]
              [:one-bad/by-id (:id props)])
       Object
       (render [this]
               (let [{:keys [id bad]} (prim/props this)]
                 (dom/li pad-in (format-bad bad)))))
(def one-bad-component (prim/factory OneBad {:keyfn :id}))

(def allow-follow-on #js {:style #js {:whiteSpace "pre"}})
(def follow-on #js {:style #js {:display "inline-block"}})
(def reddish-follow-on #js {:style #js {:display "inline-block" :color reddish}})

(defui ^:once JoinsTextItem
       static prim/Ident
       (ident [this props]
              [:item/by-id (:id props)])
       Object
       (render [this]
               (let [{:keys [id text problem]} (prim/props this)]
                 (dom/li nil (dom/span nil (dom/span nil text ": ") (dom/span reddish-follow-on (str problem)))))))
(def joins-item-component (prim/factory JoinsTextItem {:keyfn :id}))

(defui ^:once JoinsTextList
       static prim/Ident
       (ident [this props]
              [:list/by-id (:id props)])
       Object
       (render [this]
               (let [{:keys [id items]} (prim/props this)]
                 (dom/ul nil
                         (for [item items
                               :let [{:keys [text problem]} item
                                     _ (assert text)
                                     _ (assert problem)]]
                           (joins-item-component {:id (str text problem) :text text :problem problem}))))))
(def joins-list-component (prim/factory JoinsTextList {:keyfn :id}))

(defui ^:once BadTablesEntry
       static prim/Ident
       (ident [this props]
              [:bads/by-id (:id props)])
       Object
       (render [this]
               (let [{:keys [id bads-map]} (prim/props this)]
                 (dom/ul hard-left (dom/span reddish-style (str id))
                         (for [bad bads-map]
                           (one-bad-component {:id (first bad) :bad bad}))))))
(def bad-table-entries-component (prim/factory BadTablesEntry {:keyfn :id}))

(defn okay? [check-result]
  (let [{:keys [failed-assumption not-normalized-join-entries not-normalized-table-entries]} check-result]
    (not (or failed-assumption (seq not-normalized-join-entries) (seq not-normalized-table-entries)))))

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

(defui ^:once DisplayDb
       static prim/InitialAppState
       (initial-state [_ params] (merge params {::id (random-uuid)}))

       static prim/Ident
       (ident [_ props] [::id (::id props)])

       static prim/IQuery
       (query [_] [::id :version :not-normalized-table-entries :not-normalized-join-entries :failed-assumption])

       Object
       (render [this]
               (let [props (prim/props this)
                     {:keys [not-normalized-table-entries not-normalized-join-entries failed-assumption version]} props
                     _ (assert version)
                     report-problem? (not (okay? props))
                     ;_ (println "not-normalized-join-entries: " not-normalized-join-entries)
                     ]
                 (if report-problem?
                   (dom/div nil (dom/span allow-follow-on
                                          (dom/h3 coloured-follow-on (str "default-db-format"))
                                          (dom/h4 follow-on (str "  (ver: " version ")")))
                            (if failed-assumption
                              (poor-assump-div failed-assumption)
                              (dom/div nil
                                       (when (seq not-normalized-join-entries)
                                         (dom/div nil "Normalization in joins problems (:excluded-keys in config one way to fix):"
                                                  (joins-list-component {:id "Normalization in joins problems" :items not-normalized-join-entries}))
                                         )
                                       (when (seq not-normalized-table-entries)
                                         (dom/div nil "Not normalized in tables problems:"
                                                  (for [by-id (into {} not-normalized-table-entries)
                                                        :let [present-lower {:id (first by-id) :bads-map (second by-id)}]]
                                                    (bad-table-entries-component present-lower)))))))
                   (dom/div nil "No problem to report")))))
(def display-db-component (prim/factory DisplayDb {:keyfn :id}))

(defui ^:once GenericDisplayer
       Object
       (render [this]
               (let [val (prim/props this)]
                 (dom/pre nil (with-out-str (cljs.pprint/pprint val))))))
(def display (prim/factory GenericDisplayer))
