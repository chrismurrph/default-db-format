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
            [default-db-format.watcher :as watcher]
            [default-db-format.general.dev :as dev]
            [default-db-format.iframe :as iframe]))

(defn set-style! [node prop value]
  (gobj/set (gobj/get node "style") prop value))

(defui ^:once GlobalInspector
       static prim/InitialAppState
       (initial-state [_ params] {:ui/size      50
                                  :ui/visible?  false
                                  :ui/inspector (prim/get-initial-state components/DisplayDb params)})

       static prim/Ident
       (ident [_ props] [:floating-panel/by-id "main"])

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
                                               :edn     (:edn options)})
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

(defmutation state-inspection
  [{:keys [visible? check-result]}]
  (action [{:keys [state]}]
          (let [st @state
                display-db-ident (get-in st [:floating-panel/by-id "main" :ui/inspector])]
            ;(println "Check if there's a problem, and bring up UI:" visible?)
            ;(println "keys of ddf app" (keys st))
            (swap! state #(-> %
                              (assoc-in [:floating-panel/by-id "main" :ui/visible?] visible?)
                              (update-in display-db-ident merge check-result))))
          ))

(defn dump []
  (-> (global-inspector) :reconciler prim/app-state deref))

#_(defn dump-fulcro-inspect []
    (-> (fulcro.inspect.core/global-inspector) :reconciler prim/app-state deref keys dev/pp))

(defn update-inspect-state-hof [tool-reconciler]
  (let [shared-config (-> tool-reconciler prim/app-root prim/shared)
        config (:edn shared-config)]
    ;(println "config:" (dev/pp-str config))
    (fn [new-state]
      ;(println "NEW STATE:" new-state)
      (let [check-result (core/check config new-state)]
        ;(println "check-result ok?: " (core/ok? check-result))
        (prim/transact! tool-reconciler [`(state-inspection {:visible?     ~(-> check-result core/ok? not)
                                                             :check-result ~check-result}) [:floating-panel/by-id "main"]])))))

(def state-inspector (atom nil))

(defn install-app [app-id target-app]
  (let [tool-reconciler (:reconciler (global-inspector))
        shared-config (-> tool-reconciler prim/app-root prim/shared)
        timeout (-> shared-config :options :state-change-debounce-timeout)
        update-inspect-state (gfun/debounce (update-inspect-state-hof tool-reconciler) timeout)
        ]
    (reset! state-inspector update-inspect-state)
    (add-watch (some-> target-app :reconciler :config :state) app-id
               #(update-inspect-state %4))))

;;
;; Not using. Why check on every transact when can check on every change to state?
;; (For instance react key changes are not picked up)
;; Hmm - a very good reason is so don't check every time the developer saves his work!
;; Slowing the machine down at the worst time possible for no good reason!
;;
#_(defn inspect-tx [{:keys [reconciler] :as env} _]
  (if (prim/app-root reconciler)                            ; ensure host/target app is initialized
    (let [tool-reconciler (:reconciler (global-inspector))
          shared-config (-> tool-reconciler prim/app-root prim/shared)
          new-state (select-keys env [:new-state])
          config (:edn shared-config)
          timeout (-> shared-config :options :state-change-debounce-timeout)]
      (prim/transact! tool-reconciler [`(open-inspector) [:floating-panel/by-id "main"]])
      (println "new-state" (dev/pp-str new-state))
      (println "config:" (dev/pp-str config))
      (println "timeout:" timeout)
      ))
  )

(defn install [options]
  (when-not @global-inspector*
    (js/console.log "Installing \"Default DB Format\""
                    (select-keys options [:launch-keystroke :state-change-debounce-timeout]))
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

       ;::fulcro/tx-listen
       ;#'inspect-tx
       })))
