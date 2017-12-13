(ns default-db-format.inspect-global-inspector
  (:require [default-db-format.ui.multi-inspector :as multi-inspector]
            [fulcro-css.css :as css]
            [garden.core :as g]
            [fulcro.client.dom :as dom]
            [fulcro.client.mutations :as mutations]
            [default-db-format.ui.events :as events]
            [fulcro.client.primitives :as prim :refer [defui defsc]]
            [goog.object :as gobj]
            [goog.functions :as gfun]
            [default-db-format.iframe :as iframe]))

(defn set-style! [node prop value]
  (gobj/set (gobj/get node "style") prop value))

(defui ^:once GlobalInspector
       static prim/InitialAppState
       (initial-state [_ params] {:ui/size      50
                                  :ui/visible?  false
                                  :ui/inspector (prim/get-initial-state multi-inspector/MultiInspector params)})

       static prim/Ident
       (ident [_ props] [::floating-panel "main"])

       static prim/IQuery
       (query [_] [{:ui/inspector (prim/get-query multi-inspector/MultiInspector)}
                   :ui/size :ui/visible?])

       static css/CSS
       (local-rules [_] [[:.container {:background "#fff"
                                       :box-shadow "rgba(0, 0, 0, 0.3) 0px 0px 4px"
                                       :position   "fixed"
                                       :top        "0"
                                       :right      "0"
                                       :bottom     "0"
                                       :width      "50%"
                                       :overflow   "hidden"
                                       :z-index    "9999999"}]
                         [:.resizer {:position    "fixed"
                                     :cursor      "ew-resize"
                                     :top         "0"
                                     :left        "50%"
                                     :margin-left "-5px"
                                     :width       "10px"
                                     :bottom      "0"
                                     :z-index     "99999"}]
                         [:.frame {:width  "100%"
                                   :height "100%"
                                   :border "0"}]])
       (include-children [_] [element/MarkerCSS])

       Object
       (componentDidMount [this]
                          (gobj/set this "frame-dom" (js/ReactDOM.findDOMNode (gobj/get this "frame-node")))
                          (gobj/set this "resize-debouncer"
                                    (gfun/debounce #(mutations/set-value! this :ui/size %) 300)))

       (componentDidUpdate [this _ _]
                           (gobj/set this "frame-dom" (js/ReactDOM.findDOMNode (gobj/get this "frame-node"))))

       (render [this]
               (let [{:ui/keys [size visible? inspector]} (prim/props this)
                     keystroke (or (prim/shared this [:options :launch-keystroke]) "ctrl-f")
                     size (or size 50)
                     css (css/get-classnames GlobalInspector)]
                 (dom/div #js {:className (:reset css)
                               :style     (if visible? nil #js {:display "none"})}
                          (events/key-listener {::events/action    #(mutations/set-value! this :ui/visible? (not visible?))
                                                ::events/keystroke keystroke})
                          (dom/div #js {:className   (:resizer css)
                                        :ref         #(gobj/set this "resizer" %)
                                        :style       #js {:left (str size "%")}
                                        :onMouseDown (fn [_]
                                                       (let [handler (fn [e]
                                                                       (let [mouse (.-clientX e)
                                                                             vw js/document.body.clientWidth
                                                                             pos (* (/ mouse vw) 100)]
                                                                         (when (pos? pos)
                                                                           (set-style! (gobj/get this "resizer") "left" (str pos "%"))
                                                                           (set-style! (gobj/get this "container") "width" (str (- 100 pos) "%"))
                                                                           ((gobj/get this "resize-debouncer") pos))))
                                                             frame (js/ReactDOM.findDOMNode (gobj/get this "frame-node"))]
                                                         (set-style! frame "pointerEvents" "none")
                                                         (js/document.addEventListener "mousemove" handler)
                                                         (js/document.addEventListener "mouseup"
                                                                                       (fn [e]
                                                                                         (gobj/set (.-style frame) "pointerEvents" "initial")
                                                                                         (js/document.removeEventListener "mousemove" handler)))))})
                          (dom/div #js {:className (:container css)
                                        :style     #js {:width (str (- 100 size) "%")}
                                        :ref       #(gobj/set this "container" %)}
                                   (iframe/ui-iframe {:className (:frame css) :ref #(gobj/set this "frame-node" %)}
                                                     (dom/div nil
                                                              (when-let [frame (gobj/get this "frame-dom")]
                                                                (events/key-listener {::events/action    #(mutations/set-value! this :ui/visible? (not visible?))
                                                                                      ::events/keystroke keystroke
                                                                                      ::events/target    (gobj/getValueByKeys frame #js ["contentDocument" "body"])}))
                                                              (dom/style #js {:dangerouslySetInnerHTML #js {:__html (g/css [[:body {:margin "0" :padding "0" :box-sizing "border-box"}]])}})
                                                              (dom/style #js {:dangerouslySetInnerHTML #js {:__html (g/css (css/get-css multi-inspector/MultiInspector))}})
                                                              (multi-inspector/multi-inspector inspector))))))))

(def global-inspector-view (prim/factory GlobalInspector))
