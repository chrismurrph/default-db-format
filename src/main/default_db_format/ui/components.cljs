(ns default-db-format.ui.components
  (:require [fulcro.client.primitives :as prim :refer-macros [defui]]
            [fulcro.client.dom :as dom]
            [fulcro-css.css :as css]
            [default-db-format.ui.domain :as ui.domain]
            [default-db-format.dev :as dev]))

(def global-css (css/get-classnames ui.domain/CSS))

(defui ^:once RootJoinsTextItem
       static css/CSS
       (local-rules [_] [[:.problem {:background  ui.domain/gray
                                     :color       ui.domain/light-red
                                     :font-family ui.domain/mono-font-family
                                     :margin-left  "25px"
                                     :border-right "2px solid rgba(100, 100, 100, 0.2)"
                                     :padding      "0 3px"
                                     :display      "flex"
                                     }]])
       (include-children [_])
       Object
       (render [this]
               (let [css (css/get-classnames RootJoinsTextItem)
                     {:keys [problem problem-value]} (prim/props this)]
                 (dom/div #js {:className (:left-justified-container global-css)}
                          (dom/div #js {:className (:problem css)} (str problem))
                          (dom/div #js {:className (:bad-value global-css)}
                                   (str (dev/summarize problem-value))))
                 )))
(def root-joins-item-component (prim/factory RootJoinsTextItem {:keyfn :id}))

(defui ^:once OneBad
       static css/CSS
       (local-rules [_] [[:.list-item {:display          "inline-block"
                                       :margin-left      "50px"}]
                         ;;
                         ;; The width of this bad-key is done by content, and has priority over
                         ;; the value, which only displays as much as it can (i.e. truncation is fine)
                         ;;
                         [:.bad-key {:display          "flex"
                                     :white-space      "nowrap"
                                     :background-color ui.domain/gray
                                     :color            ui.domain/light-red
                                     :border-right     "2px solid rgba(100, 100, 100, 0.2)"
                                     :padding          "0 3px"}]])
       (include-children [_] [])
       Object
       (render [this]
               (let [css (css/get-classnames OneBad)
                     {:keys [bad]} (prim/props this)]
                 (dom/div #js {:className (:left-justified-container global-css)}
                          (dom/div #js {:className (:list-item css)}
                                   (let [[k v] bad]
                                     (dom/div #js {:className (:left-justified-container global-css)}
                                              (dom/div #js {:className (:bad-key css)} (str k))
                                              (dom/div #js {:className (:bad-value global-css)}
                                                       (str (dev/summarize v))))))))))
(def one-bad-component (prim/factory OneBad {:keyfn :id}))

(defui ^:once JoinsTextList
       Object
       (render [this]
               (let [{:keys [items]} (prim/props this)]
                 (apply dom/div nil
                        (for [item items
                              :let [{:keys [text problem problem-value]} item
                                    _ (assert text)
                                    _ (assert problem)]]
                          (root-joins-item-component {:id (str text problem)
                                                      :text text
                                                      :problem problem
                                                      :problem-value problem-value}))))))
(def joins-list-component (prim/factory JoinsTextList {:keyfn :id}))

(defui ^:once BadTablesEntry
       static css/CSS
       (local-rules [_] [[:.list-header {:display          "flex"
                                         :background-color ui.domain/gray
                                         :color            ui.domain/purple
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

(defui ^:once DisplayDb
       static prim/InitialAppState
       (initial-state [_ {:keys [tool-name tool-version] :as params}]
                      (assert tool-name (str "No tool name delivered to initial state: <" params ">"))
                      {:tool-name    tool-name
                       :tool-version tool-version})

       static prim/Ident
       (ident [_ props] [:display-panel/by-id :UI])

       static prim/IQuery
       (query [_] [:tool-name :tool-version :bad-table-fields :bad-root-joins :failed-assumption])

       static css/CSS
       (local-rules [_] [[:.container {:display          "flex"
                                       :background-color "rgba(100, 255, 100, 0.08)"
                                       :flex-direction   "column"
                                       :margin           "0px 0px 0px 0px"
                                       :overflow         "hidden"}]
                         [:.header {:background      ui.domain/light-green
                                    :border-bottom   "1px solid #ccc"
                                    :border-left     "1px solid #ccc"
                                    :display         "flex"
                                    :align-items     "center"
                                    :justify-content "space-between"
                                    :height          "28px"
                                    :font-family     ui.domain/label-font-family
                                    :font-size       ui.domain/label-font-size
                                    :color           ui.domain/color-text-normal}]
                         [:.title {:width           "145px"
                                   :display         "flex"
                                   :justify-content "space-around"}]
                         [:.label {:color ui.domain/color-text-strong}]
                         [:.minor-label {:color ui.domain/color-text-faded}]
                         [:.keystroke {:color        ui.domain/blue
                                       :margin-right "5px"
                                       :cursor       "pointer"}
                          [:&:hover
                           {:text-decoration "underline"}]]
                         [:.problem-sentence {:background  ui.domain/very-light-blue
                                              :color       ui.domain/close-to-black
                                              :display     "flex"
                                              :font-family ui.domain/mono-font-family
                                              :font-size   "14px"}]
                         ])
       (include-children [_] [ui.domain/CSS BadTablesEntry OneBad RootJoinsTextItem])
       Object
       (render [this]
               (let [props (prim/props this)
                     {:keys [toggle-collapse-f]} (prim/get-computed this)
                     {:keys [tool-name tool-version bad-table-fields bad-root-joins failed-assumption]} props
                     _ (when (nil? tool-name)
                         (dev/warn "No tool name when rendering. props:" props "- s/be impossible"))
                     keystroke (or (prim/shared this [:lein-options :collapse-keystroke]) "ctrl-q")
                     report-problem? (not (ui.domain/okay? props))
                     css (css/get-classnames DisplayDb)
                     root-join-problems? (seq bad-root-joins)
                     field-join-problems? (seq bad-table-fields)]
                 (if report-problem?
                   (dom/div #js {:className (:container css)}
                            (dom/div #js {:className (:header css)}
                                     (dom/div #js {:className (:title css)}
                                              (dom/div #js {:className (:label css)} tool-name)
                                              (dom/div #js {:className (:minor-label css)} (str "ver " tool-version)))
                                     (dom/div #js {:className (:keystroke css)
                                                   :onClick   toggle-collapse-f} keystroke))
                            (if failed-assumption
                              (let [{:keys [text]} failed-assumption
                                    boiler-text (str "Failed assumption")]
                                (dom/div #js {:className (:vertical-container global-css)}
                                         (dom/div #js {:className (:problem-sentence css)} boiler-text)
                                         (dom/div #js {:className (:left-justified-container global-css)}
                                                  (dom/div #js {:className (:text-explanation-simple global-css)} text))))
                              (dom/div nil
                                       (when root-join-problems?
                                         (dom/div nil
                                                  (dom/div #js {:className (:problem-sentence css)}
                                                           (dom/div nil
                                                                    "Expect Ident/s in "
                                                                    (dom/span #js {:className (:red-coloured global-css)}
                                                                              "root join")
                                                                    " (consider "
                                                                    (dom/span #js {:className (:purple-coloured global-css)}
                                                                              ":link")
                                                                    " in edn config)"
                                                                    ))
                                                  (joins-list-component {:items bad-root-joins})))
                                       (when (and root-join-problems? field-join-problems?)
                                         (dom/br nil))
                                       (when field-join-problems?
                                         (dom/div nil
                                                  (dom/div #js {:className (:problem-sentence css)}
                                                           (dom/div nil
                                                                    "Expect Ident/s in "
                                                                    (dom/span #js {:className (:red-coloured global-css)}
                                                                              "field join")
                                                                    " (consider "
                                                                    (dom/span #js {:className (:purple-coloured global-css)}
                                                                              ":bad-field-join")
                                                                    " in edn config)"
                                                                    ))
                                                  (dom/div nil
                                                           (for [by-id (into {} bad-table-fields)
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
