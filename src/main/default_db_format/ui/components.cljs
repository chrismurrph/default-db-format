(ns default-db-format.ui.components
  (:require [fulcro.client.primitives :as prim :refer-macros [defui]]
            [fulcro.client.dom :as dom]
            [fulcro-css.css :as css]
            [default-db-format.ui.domain :as ui.domain]
            [default-db-format.general.dev :as dev]))

(def hard-left #js {:style #js {:margin 15 :padding 0}})
(def reddish-style #js {:style #js {:color ui.domain/reddish}})
(def pad-in #js {:style #js {:margin 20 :padding 0}})

(def global-css (css/get-classnames ui.domain/CSS))

(defui ^:once OneBad
       ;static prim/Ident
       ;(ident [this props]
       ;       [:one-bad/by-id (:id props)])
       ;static prim/IQuery
       ;(query [_] [:id :bad])

       static css/CSS
       (local-rules [_] [[:.list-item {:display          "flex"
                                       :color            "#ffffff"
                                       :background-color "#6f6f6f"
                                       :margin-left      "50px"}]
                         [:.bad-key {:display          "flex"
                                     :background-color "#dddddd"
                                     :color            ui.domain/reddish
                                     :border-right     "2px solid rgba(100, 100, 100, 0.2)"
                                     ;:min-width        "35px"
                                     :margin-right     "5px"
                                     :padding          "0 3px"}]])
       (include-children [_])
       Object
       (render [this]
               (let [css (css/get-classnames OneBad)
                     {:keys [id bad]} (prim/props this)]
                 (dom/div #js {:className (:left-justified-container global-css)}
                          (dom/div #js {:className (:list-item css)}
                                   (let [[k v] bad]
                                     (dom/div #js {:className (:left-justified-container global-css)}
                                              (dom/div #js {:className (:bad-key css)} (str k))
                                              (dom/div #js {:className (:flex global-css)} (str v)))))))))
(def one-bad-component (prim/factory OneBad {:keyfn :id}))

(def allow-follow-on #js {:style #js {:whiteSpace "pre"}})
(def follow-on #js {:style #js {:display "inline-block"}})
(def reddish-follow-on #js {:style #js {:display "inline-block" :color ui.domain/reddish}})

(defui ^:once JoinsTextItem
       ;static prim/Ident
       ;(ident [this props]
       ;       [:item/by-id (:id props)])
       Object
       (render [this]
               (let [{:keys [id text problem]} (prim/props this)]
                 (dom/li nil (dom/span nil (dom/span nil text ": ") (dom/span reddish-follow-on (str problem)))))))
(def joins-item-component (prim/factory JoinsTextItem {:keyfn :id}))

(defui ^:once JoinsTextList
       ;static prim/Ident
       ;(ident [this props]
       ;       [:list/by-id (:id props)])
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
       ;static prim/Ident
       ;(ident [this props]
       ;       [:bads/by-id (:id props)])
       ;
       ;static prim/IQuery
       ;(query [_] [:id :bads-map])

       static css/CSS
       (local-rules [_] [[:.list-header {:display          "flex"
                                         :background-color "#dddddd"
                                         :color            "#881391"
                                         :border-right     "2px solid rgba(100, 100, 100, 0.2)"
                                         :min-width        "35px"
                                         :margin-bottom    "1px"
                                         :margin-right     "5px"
                                         :margin-left      "25px"
                                         :padding          "0 3px"}]])
       (include-children [OneBad])
       Object
       (render [this]
               (let [{:keys [id bads-map]} (prim/props this)
                     css (css/get-classnames BadTablesEntry)]
                 (dom/div #js {:className (:vertical css)}
                          (dom/div #js {:className (:left-justified-container global-css)}
                                   (dom/div #js {:className (:list-header css)} (str id)))
                          (apply dom/div #js {:className (:vertical-container global-css)}
                                 (for [bad bads-map]
                                   (one-bad-component {:id (first bad) :bad bad})))))))

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
       (initial-state [_ {:keys [tool-name tool-version] :as params}]
                      (merge params {::id          (random-uuid)
                                     :tool-name    tool-name
                                     :tool-version tool-version}))

       static prim/Ident
       (ident [_ props] [::id (::id props)])

       static prim/IQuery
       (query [_] [::id :tool-name :tool-version :not-normalized-table-entries :not-normalized-join-entries :failed-assumption])

       static css/CSS
       (local-rules [_] [[:.container {:display          "flex"
                                       ;:background     "#f3f3f3"
                                       :background-color "rgba(100, 255, 100, 0.08)"
                                       :flex-direction   "column"
                                       ;:width          "100%"
                                       ;:height         "100%"
                                       :margin           "0px 0px 0px 0px"
                                       :overflow         "hidden"}]
                         [:.header {:background    "#f3f3f3"
                                    :border-bottom "1px solid #ccc"
                                    :border-left   "1px solid #ccc"
                                    :display       "flex"
                                    :align-items   "center"
                                    :height        "28px"
                                    :font-family   ui.domain/label-font-family
                                    :font-size     ui.domain/label-font-size
                                    :color         ui.domain/color-text-normal
                                    }]
                         [:.title {:width           "145px"
                                   :display         "flex"
                                   :justify-content "space-around"
                                   }]
                         [:.label {:color ui.domain/color-text-strong}]
                         [:.minor-label {:color ui.domain/color-text-faded
                                         }]
                         [:.display-name {:background  "#e5efff"
                                          :color       "#051d38"
                                          :display     "flex"
                                          :padding     "4px 8px"
                                          :font-family ui.domain/mono-font-family
                                          :font-size   "14px"}]
                         ])
       (include-children [_] [ui.domain/CSS BadTablesEntry OneBad])
       Object
       (render [this]
               (let [props (prim/props this)
                     {:keys [tool-name tool-version not-normalized-table-entries not-normalized-join-entries failed-assumption]} props
                     _ (assert tool-version)
                     report-problem? (not (okay? props))
                     ;_ (println "not-normalized-join-entries: " not-normalized-join-entries)
                     css (css/get-classnames DisplayDb)
                     ]
                 (if report-problem?
                   (dom/div #js {:className (:container css)}
                            (dom/div #js {:className (:header css)}
                                     (dom/div #js {:className (:title css)}
                                              (dom/div #js {:className (:label css)} tool-name)
                                              (dom/div #js {:className (:minor-label css)} (str "ver " tool-version))))

                            (if failed-assumption
                              (poor-assump-div failed-assumption)
                              (dom/div nil
                                       (when (seq not-normalized-join-entries)
                                         (dom/div nil
                                                  (dom/div #js {:className (:display-name css)} "Normalization in joins problems (:excluded-keys in config one way to fix)")
                                                  (joins-list-component {:id "Normalization in joins problems" :items not-normalized-join-entries})))
                                       (when (seq not-normalized-table-entries)
                                         (dom/div nil
                                                  (dom/div #js {:className (:display-name css)} "Not normalized in tables problems")
                                                  (dom/div nil
                                                           (for [by-id (into {} not-normalized-table-entries)
                                                                 :let [present-lower {:id (first by-id) :bads-map (second by-id)}]]
                                                             (bad-table-entries-component present-lower))))))))
                   (dom/div nil "No problem to report")))))
(def display-db-component (prim/factory DisplayDb {:keyfn :id}))

(defui ^:once GenericDisplayer
       Object
       (render [this]
               (let [val (prim/props this)]
                 (dom/pre nil (with-out-str (cljs.pprint/pprint val))))))
(def display (prim/factory GenericDisplayer))
