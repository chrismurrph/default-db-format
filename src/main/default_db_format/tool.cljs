(ns default-db-format.tool
  (:require [fulcro.client.primitives :as prim :refer [defui defsc]]
            [fulcro.client :as fulcro]
            [fulcro.client.mutations :as mutations :refer-macros [defmutation]]
            [fulcro-css.css :as css]
            [fulcro.client.dom :as dom]
            [garden.core :as g]
            [default-db-format.ui.events :as events]
            [default-db-format.ui.element :as element]
            [default-db-format.ui.components :as components]
            [goog.object :as gobj]
            [goog.functions :as gfun]
            [default-db-format.ui.inspector :as inspector]
            [default-db-format.ui.multi-inspector :as multi-inspector]
            [default-db-format.core :as core]
            [default-db-format.general.dev :as dev]
            [default-db-format.iframe :as iframe]))

(defn set-style! [node prop value]
  (gobj/set (gobj/get node "style") prop value))

(defui ^:once GlobalInspector
       static prim/InitialAppState
       (initial-state [_ params] {:ui/size         50
                                  :ui/visible?     false
                                  :ui/inspector    (prim/get-initial-state components/DisplayDb params)})

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
                     keystroke (or (prim/shared this [:options :launch-keystroke]) "ctrl-a")
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
                                                              (dom/style #js {:dangerouslySetInnerHTML #js {:__html (g/css (css/get-css components/DisplayDb))}})
                                                              (components/display-db-component inspector))))))))

(def global-inspector-view (prim/factory GlobalInspector))

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

(defui ^:once GlobalRoot
       static prim/InitialAppState
       (initial-state [_ _] {:ui/react-key (random-uuid)
                             :ui/root      (prim/get-initial-state GlobalInspector {:version core/version})})

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
  (let [app (fulcro/new-fulcro-client :shared {:options (dissoc options :edn)
                                               :edn (:edn options)})
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

(defmutation open-inspector
  [{:keys []}]
  (action [{:keys [state]}]
          (let [st @state]
            (println "Check if there's a problem, and bring up UI if there is")
            (println "keys of ddf app" (keys st))
            (swap! state #(-> %
                              (assoc-in [::floating-panel "main" :ui/visible?] true))))
          ))

#_(defn watch-state []
  (when (and first-time? (not (core/ok? check-result)))
    (js/setTimeout (fn []
                     (fulcro.client.util/force-render reconciler)
                     (un-check!))
                   timeout)))

(defn dump []
  (-> (global-inspector) :reconciler prim/app-state deref))

#_(defn dump-fulcro-inspect []
  (-> (fulcro.inspect.core/global-inspector) :reconciler prim/app-state deref keys dev/pp))

(defn inspect-tx [{:keys [reconciler] :as env} info]
  (if (prim/app-root reconciler) ; ensure host/target app is initialized
    (let [
          ;tx        (merge info (select-keys env [:old-state :new-state :ref :component]))
          ;app-id    (app-id reconciler)
          tool-reconciler (:reconciler (global-inspector))
          shared-config (-> tool-reconciler prim/app-root prim/shared)
          new-state (select-keys env [:new-state])
          config (:edn shared-config)
          timeout (-> shared-config :options :state-change-debounce-timeout)]
      (prim/transact! tool-reconciler [`(open-inspector)])
      (println "new-state" (dev/pp-str new-state))
      (println "config:" (dev/pp-str config))
      (println "timeout:" timeout)
      ))
  )

(defn update-inspect-state [reconciler app-id state]
  (println "Should be called whenever the state of " app-id " is changed. keys: " (keys state)))

(defn install-app [app-id target-app]
  (let [inspector (global-inspector)
        state* (some-> target-app :reconciler :config :state)]
    (add-watch state* app-id
               #(update-inspect-state (:reconciler inspector) app-id %4))))

(defn inspect-app [app-id target-app]
  (let [inspector     (global-inspector)
        state*        (some-> target-app :reconciler :config :state)
        new-inspector (-> (prim/get-initial-state inspector/Inspector @state*)
                          (assoc ::inspector/id app-id)
                          ;;(assoc-in [::inspector/app-state ::data-history/history-id] [::app-id app-id])
                          ;;(assoc-in [::inspector/network ::network/history-id] [::app-id app-id])
                          (assoc-in [::inspector/element ::element/panel-id] [::app-id app-id])
                          (assoc-in [::inspector/element ::element/target-reconciler] (:reconciler target-app))
                          ;;(assoc-in [::inspector/transactions ::transactions/tx-list-id] [::app-id app-id])
                          )]
    (prim/transact! (:reconciler inspector) [::multi-inspector/multi-inspector "main"]
                  [`(multi-inspector/add-inspector ~new-inspector)
                   ::inspectors])

    #_(inspect-network-init (-> target-app :networking :remote) {:inspector inspector
                                                               :app       target-app})

    (add-watch state* app-id
               #(update-inspect-state (:reconciler inspector) app-id %4))

    new-inspector))

(def edn-keys [:excluded-keys :okay-value-maps :okay-value-vectors :by-id-kw :routing-ns])

(defn install [options]
  (when-not @global-inspector*
    (js/console.log "Installing \"Default DB Format\"" (apply dissoc options edn-keys))
    (global-inspector options)

    (fulcro/register-tool
      {::fulcro/tool-id
       ::default-db-format

       ::fulcro/app-started
       (fn [{:keys [reconciler] :as app}]
         (let [id (-> reconciler app-id dedupe-id)]
           (swap! (-> reconciler prim/app-state) assoc ::app-id id)
           (install-app id app))
         app)

       ::fulcro/tx-listen
       #'inspect-tx})))
