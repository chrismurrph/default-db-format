(ns default-db-format.tool
  (:require [fulcro.client.primitives :as prim]
            [fulcro.client :as fulcro]
            [fulcro.client.mutations :as mutations :refer-macros [defmutation]]
            [goog.object :as gobj]
            [fulcro-css.css :as css]
            [fulcro.client.dom :as dom]
            [garden.core :as g]
            [default-db-format.ui.events :as events]
            [default-db-format.ui.element :as element]
            [default-db-format.ui.components :as components]
            [goog.functions :as gfun]))

(defn update-frame-content [this child]
  (let [frame-component (gobj/get this "frame-component")]
    (when frame-component
      (js/ReactDOM.render child frame-component))))

(defn start-frame [this]
  (let [frame-body (.-body (.-contentDocument (js/ReactDOM.findDOMNode this)))
        {:keys [child]} (prim/props this)
        e1         (.createElement js/document "div")]
    (when (= 0 (gobj/getValueByKeys frame-body #js ["children" "length"]))
      (.appendChild frame-body e1)
      (gobj/set this "frame-component" e1)
      (update-frame-content this child))))

(prim/defui IFrame
          Object
          (componentDidMount [this] (start-frame this))

          (componentDidUpdate [this _ _]
                              (let [child (:child (prim/props this))]
                                (update-frame-content this child)))

          (render [this]
                  (dom/iframe
                    (-> (prim/props this)
                        (dissoc :child)
                        (assoc :onLoad #(start-frame this))
                        clj->js))))

(let [factory (prim/factory IFrame)]
  (defn ui-iframe [props child]
    (factory (assoc props :child child))))

(defn set-style! [node prop value]
  (gobj/set (gobj/get node "style") prop value))

(defn app-id [reconciler]
  (or (some-> reconciler prim/app-state deref ::app-id)
      (some-> reconciler prim/app-root prim/react-type (gobj/get "displayName") symbol)))

(defn inc-id [id]
  (let [new-id (if-let [[_ prefix d] (re-find #"(.+?)(\d+)$" (str id))]
                 (str prefix (inc (js/parseInt d)))
                 (str id "-0"))]
    (cond
      (keyword? id) (keyword (subs new-id 1))
      (symbol? id) (symbol new-id)
      :else new-id)))

(prim/defui ^:once GlobalInspector
          static prim/InitialAppState
          (initial-state [_ params] {:ui/size      50
                                     :ui/visible?  false
                                     :ui/inspector (prim/get-initial-state components/DisplayDb params)})

          static prim/Ident
          (ident [_ props] [::floating-panel "main"])

          static prim/IQuery
          (query [_] [{:ui/inspector (prim/get-query components/DisplayDb)}
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
                        keystroke (or (prim/shared this [:options :launch-keystroke]) "ctrl-g")
                        size      (or size 50)
                        css       (css/get-classnames GlobalInspector)]
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
                                                                                vw    js/document.body.clientWidth
                                                                                pos   (* (/ mouse vw) 100)]
                                                                            (when (pos? pos)
                                                                              (set-style! (gobj/get this "resizer") "left" (str pos "%"))
                                                                              (set-style! (gobj/get this "container") "width" (str (- 100 pos) "%"))
                                                                              ((gobj/get this "resize-debouncer") pos))))
                                                                frame   (js/ReactDOM.findDOMNode (gobj/get this "frame-node"))]
                                                            (set-style! frame "pointerEvents" "none")
                                                            (js/document.addEventListener "mousemove" handler)
                                                            (js/document.addEventListener "mouseup"
                                                                                          (fn [e]
                                                                                            (gobj/set (.-style frame) "pointerEvents" "initial")
                                                                                            (js/document.removeEventListener "mousemove" handler)))))})
                             (dom/div #js {:className (:container css)
                                           :style     #js {:width (str (- 100 size) "%")}
                                           :ref       #(gobj/set this "container" %)}
                                      (ui-iframe {:className (:frame css) :ref #(gobj/set this "frame-node" %)}
                                                 (dom/div nil
                                                          (when-let [frame (gobj/get this "frame-dom")]
                                                            (events/key-listener {::events/action    #(mutations/set-value! this :ui/visible? (not visible?))
                                                                                  ::events/keystroke keystroke
                                                                                  ::events/target    (gobj/getValueByKeys frame #js ["contentDocument" "body"])}))
                                                          (dom/style #js {:dangerouslySetInnerHTML #js {:__html (g/css [[:body {:margin "0" :padding "0" :box-sizing "border-box"}]])}})
                                                          (dom/style #js {:dangerouslySetInnerHTML #js {:__html (g/css (css/get-css components/DisplayDb))}})
                                                          (components/display-db-component inspector))))))))

(def global-inspector-view (prim/factory GlobalInspector))

(prim/defui ^:once GlobalRoot
          static prim/InitialAppState
          (initial-state [_ _] {:ui/react-key (random-uuid)
                                :ui/root      (prim/get-initial-state GlobalInspector {:version -1})})

          static prim/IQuery
          (query [_] [{:ui/root (prim/get-query GlobalInspector)}
                      :ui/react-key])

          static css/CSS
          (local-rules [_] [])
          (include-children [_] [GlobalInspector])

          Object
          (render [this]
                  (let [{:keys [ui/react-key ui/root]} (prim/props this)]
                    (dom/div #js {:key react-key}
                             (global-inspector-view root)))))

(defonce ^:private global-inspector* (atom nil))

(defn start-global-inspector [options]
  (let [app  (fulcro/new-fulcro-client :shared {:options options})
        node (js/document.createElement "div")]
    (js/document.body.appendChild node)
    (css/upsert-css "default-db-format" GlobalRoot)
    (fulcro/mount app GlobalRoot node)))

(defn global-inspector
  ([] @global-inspector*)
  ([options]
   (or @global-inspector*
       (reset! global-inspector* (start-global-inspector options)))))

(defn dedupe-id [id]
  (let [ids-in-use (some-> (global-inspector) :reconciler prim/app-state deref ::components/id)]
    (loop [new-id id]
      (if (contains? ids-in-use new-id)
        (recur (inc-id new-id))
        new-id))))

(defn inspect-tx [{:keys [reconciler] :as env} info]
  (let [inspector (global-inspector)
        tx        (merge info (select-keys env [:old-state :new-state :ref :component]))
        app-id    (app-id reconciler)]
    (println "inspect-tx, expect to see this regularly")))

(defn update-inspect-state [reconciler app-id state]
  (println "Can do anything we want, the state has changed"))

(defn inspect-app [app-id target-app]
  (let [inspector     (global-inspector)
        state*        (some-> target-app :reconciler :config :state)]
    (add-watch state* app-id
               #(update-inspect-state (:reconciler inspector) app-id %4))))

(defn install [options]
  (when-not @global-inspector*
    (js/console.log "Installing \"Default DB Format\"" options)
    (global-inspector options)

    (fulcro/register-tool
      {::fulcro/tool-id
       ::default-db-format

       ::fulcro/app-started
       (fn [{:keys [reconciler] :as app}]
         (let [id (-> reconciler app-id dedupe-id)]
           (swap! (-> reconciler prim/app-state) assoc ::app-id id)
           (inspect-app id app))
         app)

       ::fulcro/tx-listen
       #'inspect-tx})))
