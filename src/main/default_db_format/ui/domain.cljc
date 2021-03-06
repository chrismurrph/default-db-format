(ns default-db-format.ui.domain
  (:require [fulcro.client.primitives :as prim]
            [fulcro-css.css :as css]
            [garden.selectors :as gs]))

(defn detail-okay [check-result]
  (let [{:keys [poor-table-structures failed-assumption
                skip-root-joins skip-table-fields
                non-vector-root-joins non-vector-table-fields
                ]} check-result
        un-normalized-joins-exist? (boolean (seq skip-root-joins))
        un-normalized-tables-exist? (boolean (seq skip-table-fields))
        non-vector-joins-exist? (boolean (seq non-vector-root-joins))
        non-vector-tables-exist? (boolean (seq non-vector-table-fields))
        bad-map-entries-exist? (boolean (seq poor-table-structures))
        ]
    [(not bad-map-entries-exist?) (not failed-assumption)
     (not un-normalized-joins-exist?) (not un-normalized-tables-exist?)
     (not non-vector-joins-exist?) (not non-vector-tables-exist?)
     ]))

(defn okay? [check-result]
  (= [true true true true true true] (detail-okay check-result)))

;;
;; Some stealing from fulcro-inspect, source of a lot of good stuff
;;

(def mono-font-family "monospace")
(def label-font-family "sans-serif")
(def label-font-size "12px")
(def color-text-normal "#5a5a5a")
(def color-text-strong "#333")
(def color-text-faded "#bbb")

(def reddish "#E11F1F")
(def light-red "#cc3333")
(def gray "#dddddd")
(def darker-gray "#6f6f6f")
(def white "#ffffff")
(def purple "#881391")
(def light-green "#f3f3f3")
(def blue "#3366ff" #_"#0099ff")
(def light-blue "#e5efff")
(def very-light-blue "#f0ffff")
(def close-to-black "#051d38")

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

(prim/defui ^:once CSS
            static css/CSS
            (local-rules [_] [[:.red-coloured {:color reddish}]
                              [:.light-red-coloured {:color light-red}]
                              [:.purple-coloured {:color purple}]
                              [:.flex {:display "flex"}]
                              [:.vertical-container {:display        "flex"
                                                     :flex-direction "column"}]
                              [:.left-justified-container {:display         "flex"
                                                           :justify-content "flex-start"}]
                              [:.red-dot {:border        "5px solid red"
                                          :border-radius "5px"
                                          :position      "fixed"
                                          :top           "6px"
                                          :left          "6px"
                                          :cursor        "pointer"
                                          :z-index       "9999999"}]
                              [:.text-explanation-simple {:background  light-blue
                                                          :color       close-to-black
                                                          :font-family mono-font-family
                                                          :margin-left "25px"
                                                          }]
                              [:.text-explanation-abutting {:display      "flex"
                                                            :background   light-blue
                                                            :color        close-to-black
                                                            :font-family  mono-font-family
                                                            :margin-left  "25px"
                                                            :border-right "2px solid rgba(100, 100, 100, 0.2)"
                                                            :padding      "0 3px"
                                                            }]
                              [:.bad-value {:display          "flex"
                                            :text-overflow    "ellipsis"
                                            :overflow         "hidden"
                                            :white-space      "nowrap"
                                            :color            white
                                            :background-color darker-gray
                                            }]
                              [:.info-group css-info-group
                               [(gs/& gs/first-child) {:border-top "0"}]]
                              [:.info-label css-info-label]
                              [:.alarm-label css-alarm-label]
                              ])
            (include-children [_] []))

