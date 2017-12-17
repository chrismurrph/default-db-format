(ns default-db-format.ui.domain
  (:require [fulcro.client.primitives :as prim]
            [fulcro-css.css :as css]
            [fulcro.ui.icons :as icons]
            [garden.selectors :as gs]
            [fulcro.client.dom :as dom]))

;;
;; Straight steal from fulcro-inspect, source of a lot of good stuff
;;

(def mono-font-family "monospace")
(def label-font-family "sans-serif")
(def label-font-size "12px")
(def color-text-normal "#5a5a5a")
(def color-text-strong "#333")
(def color-text-faded "#bbb")

(def reddish "#E11F1F")
(def light-red "#cc3333")

(def css-info-group
  {:border-top "1px solid #eee"
   :padding    "7px 0"})

(def -css-label
  {:margin-bottom "6px"
   :font-weight   "bold"
   :font-family   label-font-family
   :font-size     "13px"})

(def css-info-label
  (merge -css-label
         {:color color-text-normal}))

(def css-alarm-label
  (merge -css-label
         {:color reddish}))

(def css-timestamp
  {:font-family "monospace"
   :font-size   "11px"
   :color       "#808080"
   :margin      "0 4px 0 7px"})

(prim/defui ^:once CSS
            static css/CSS
            (local-rules [_] [[:.red-coloured {:color reddish}]
                              [:.light-red-coloured {:color light-red}]
                              [:.flex {:display "flex"}]
                              [:.vertical-container {:display        "flex"
                                                     :flex-direction "column"}]
                              [:.left-justified-container {:display         "flex"
                                                           :justify-content "flex-start"}]
                              [:.focused-panel {:border-top     "1px solid #a3a3a3"
                                                :display        "flex"
                                                :flex-direction "column"
                                                :height         "50%"}]
                              [:.focused-container {:flex     "1"
                                                    :overflow "auto"
                                                    :padding  "0 10px"}]

                              [:.info-group css-info-group
                               [(gs/& gs/first-child) {:border-top "0"}]]
                              [:.info-label css-info-label]
                              [:.alarm-label css-alarm-label]
                              [:.ident {:padding     "5px 6px"
                                        :background  "#f3f3f3"
                                        :color       "#424242"
                                        :display     "inline-block"
                                        :font-family mono-font-family
                                        :font-size   label-font-size}]
                              [:.display-name {:background  "#e5efff"
                                               :color       "#051d38"
                                               :display     "inline-block"
                                               :padding     "4px 8px"
                                               :font-family mono-font-family
                                               :font-size   "14px"}]])
            (include-children [_] []))

