(ns default-db-format.ui.element
  (:require
    [fulcro-css.css :as css]
    [fulcro.client.primitives :as prim]))

(prim/defui ^:once MarkerCSS
            static css/CSS
            (local-rules [_] [[:.container {:position       "absolute"
                                  :display        "none"
                                  :background     "rgba(67, 132, 208, 0.5)"
                                  :pointer-events "none"
                                  :overflow       "hidden"
                                  :color          "#fff"
                                  :padding        "3px 5px"
                                  :box-sizing     "border-box"
                                  :font-family    "monospace"
                                  :font-size      "12px"
                                  :z-index        "999999"}]

                    [:.label {:position       "absolute"
                              :display        "none"
                              :pointer-events "none"
                              :box-sizing     "border-box"
                              :font-family    "sans-serif"
                              :font-size      "10px"
                              :z-index        "999999"

                              :background     "#333740"
                              :border-radius  "3px"
                              :padding        "6px 8px"
                              ;:color          "#ee78e6"
                              :color          "#ffab66"
                              :font-weight    "bold"
                              :white-space    "nowrap"}
                     [:&:before {:content      "\"\""
                                 :position     "absolute"
                                 :top          "24px"
                                 :left         "9px"
                                 :width        "0"
                                 :height       "0"
                                 :border-left  "8px solid transparent"
                                 :border-right "8px solid transparent"
                                 :border-top   "8px solid #333740"}]]])
            (include-children [_]))