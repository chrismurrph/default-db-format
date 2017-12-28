(ns default-db-format.tool
  (:require [fulcro.client.primitives :as prim :refer [defui defsc]]
            [fulcro.client :as fulcro]
            [fulcro.client.mutations :as mutations :refer-macros [defmutation]]
            [fulcro-css.css :as css]
            [fulcro.client.dom :as dom]
            [garden.core :as g]
            [default-db-format.ui.events :as events]
            [default-db-format.ui.components :as components]
            [default-db-format.general.dev :as dev]
            [goog.object :as gobj]
            [goog.functions :as gfun]
            [default-db-format.core :as core]
            [default-db-format.iframe :as iframe]
            [default-db-format.ui.domain :as ui.domain]
            [clojure.string :as s]))

(def expanded-percentage-width 50)

;; A red line on the right edge of the container will be a
;; reminder to the user.
;; Hmm - mose well just have a red line rather than collapse it!
;; With some css (bootstrap) the css is overridden so that the red dot is not displayed
;; For now we are living with it. But collapsing will work in such a situation, so perhaps
;; we can make collapsing an option in a future version. Yak shaving - no red dot is still fine.
(def collapsed-percentage-width 1)

(def global-css (css/get-classnames ui.domain/CSS))

(defui ^:once GlobalInspector
       static prim/InitialAppState
       (initial-state [_ params] {:ui/visible?   false
                                  :ui/collapsed? false
                                  :ui/inspector  (prim/get-initial-state components/DisplayDb params)})

       static prim/Ident
       (ident [_ props] [:floating-panel/by-id "main"])

       static prim/IQuery
       (query [_] [{:ui/inspector (prim/get-query components/DisplayDb)}
                   :ui/visible? :ui/collapsed?])

       static css/CSS
       (local-rules [_] [[:.container {:background "#f3f3f3"
                                       :box-shadow "rgba(0, 0, 0, 0.3) 0px 0px 4px"
                                       :position   "fixed"
                                       :top        "0"
                                       :left       "0"
                                       :bottom     "0"
                                       :overflow   "hidden"
                                       :z-index    "9999999"}]
                         [:.frame {:width  "100%"
                                   :height "100%"
                                   :border "0"}]])
       (include-children [_] [])

       Object
       (componentDidMount [this]
                          (gobj/set this "frame-dom" (js/ReactDOM.findDOMNode (gobj/get this "frame-node"))))

       (componentDidUpdate [this _ _]
                           (gobj/set this "frame-dom" (js/ReactDOM.findDOMNode (gobj/get this "frame-node"))))

       (render [this]
               (let [{:ui/keys [visible? collapsed? inspector]} (prim/props this)
                     toggle-collapse-f #(mutations/set-value! this :ui/collapsed? (not collapsed?))
                     keystroke (or (prim/shared this [:options :collapse-keystroke]) "ctrl-q")
                     css (css/get-classnames GlobalInspector)]
                 (dom/div #js {:style (if visible? nil #js {:display "none"})}
                          (events/key-listener {::events/action    toggle-collapse-f
                                                ::events/keystroke keystroke})
                          (if collapsed?
                            (dom/div #js {:className (:red-dot global-css)
                                          :onClick toggle-collapse-f} nil)
                            (dom/div #js {:className (:container css)
                                          :style     #js {:width (str expanded-percentage-width "%")}
                                          :ref       #(gobj/set this "container" %)}
                                     (iframe/ui-iframe {:className (:frame css) :ref #(gobj/set this "frame-node" %)}
                                                       (dom/div nil
                                                                (when-let [frame (gobj/get this "frame-dom")]
                                                                  (events/key-listener {::events/action    #(mutations/set-value! this :ui/collapsed? (not collapsed?))
                                                                                        ::events/keystroke keystroke
                                                                                        ::events/target    (gobj/getValueByKeys frame #js ["contentDocument" "body"])}))
                                                                (dom/style #js {:dangerouslySetInnerHTML #js {:__html (g/css [[:body {:margin "0" :padding "0" :box-sizing "border-box"}]])}})
                                                                (dom/style #js {:dangerouslySetInnerHTML #js {:__html (g/css (css/get-css components/DisplayDb))}})
                                                                (components/display-db-component
                                                                  (prim/computed inspector {:toggle-collapse-f toggle-collapse-f}))))))))))

(def global-inspector-view (prim/factory GlobalInspector))

(defui ^:once ToolRoot
       static prim/InitialAppState
       (initial-state [_ _] {:ui/react-key (random-uuid)
                             :ui/root      (prim/get-initial-state GlobalInspector {:tool-version core/tool-version
                                                                                    :tool-name    core/tool-name})})

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
  (let [configuration {:options (dissoc options :edn)
                       :edn     (:edn options)}
        app (fulcro/new-fulcro-client :shared configuration)
        node (js/document.createElement "div")]
    (js/document.body.appendChild node)
    (css/upsert-css "default-db-format" ToolRoot)
    (fulcro/mount app ToolRoot node)))

(defn global-inspector
  ([] @global-inspector*)
  ([options]
   (or @global-inspector*
       (reset! global-inspector* (start-global-inspector options)))))

(defn get-config
  "The host/target application can use this when it wants know the configuration it
  set at compile time (from lein and edn), at run-time. For example to call a function
  such as default-db-format.helpers/ident-like-hof. Will return nil when called before the
  app is initialized"
  []
  (some-> (global-inspector) :reconciler prim/app-root prim/shared))

(defmutation state-inspection
  [{:keys [visible? check-result]}]
  (action [{:keys [state]}]
          (let [st @state
                floating-panel-ident [:floating-panel/by-id "main"]
                inspector-join (conj floating-panel-ident :ui/inspector)
                visible-join (conj floating-panel-ident :ui/visible?)
                collapsed-join (conj floating-panel-ident :ui/collapsed?)
                display-db-ident (get-in st inspector-join)]
            (swap! state #(cond-> %
                                  true (assoc-in visible-join visible?)
                                  visible? (assoc-in collapsed-join false)
                                  true (update-in display-db-ident merge check-result))))))

(defn dump []
  (-> (global-inspector) :reconciler prim/app-state deref))

;;
;; Only commented out because Figwheel issues a warning:
;;
;; WARNING: Use of undeclared Var fulcro.inspect.core/global-inspector at line 180
;; /home/chris/IdeaProjects/default-db-format/src/main/default_db_format/tool.cljs
;;
;; Regardless of the warning works fine to see another tool's state.
;;
#_(defn dump-fulcro-inspect []
    (-> (fulcro.inspect.core/global-inspector) :reconciler prim/app-state deref keys dev/pp))

(defn update-inspect-state-hof [tool-reconciler host-app-path]
  (let [config (-> tool-reconciler prim/app-root prim/shared :edn)]
    (js/console.log (str core/tool-name " on " (first host-app-path) " - config: " (dev/summarize config)))
    (fn [new-state]
      (let [check-result (core/check config new-state)]
        (prim/transact! tool-reconciler [`(state-inspection {:visible?     ~(-> check-result core/ok? not)
                                                             :check-result ~check-result}) [:floating-panel/by-id "main"]])))))

;;
;; Stores a function that takes new-state. Will only be stored if we are listen-transactions-only?
;;
(defonce ^:private state-inspector (atom nil))

(defn app-path [app]
  (let [display-name (some-> app :reconciler prim/app-root prim/react-type (gobj/get "displayName"))]
    [display-name (some-> display-name (s/split #"/") last)]))

(def ignore-inspect "fulcro.inspect.core/GlobalRoot")
(defonce ^:private host-root-path* (atom nil))

;;
;; Why check on every transact when can check on every change to state?
;; (For instance react key changes are not picked up when on every transact)
;; One reason would be so don't check every time the developer saves his work,
;; slowing the machine down at the worst possible time for no good reason.
;; So a favourable choice is to not to slow the developer's machine down at the expense
;; of missing a change to app state that did not go through a `transact!`.
;; And quite possibly there is no such thing - everything goes through `transact!`.
;; So originally had this as false.
;; Changed to true, when realising that the env that comes thru to
;; ::fulcro/tx-listen is a bit of an unknown when there are many clients. It
;; was listening to Fulcro Inspector! Conversely with the watching way (all-state-changes?
;; being true) we can control which client we are listening to.
;; (And the 'every time a developer saves his work' argument was not based on an
;; empirical observation of an actual problem, which prolly doesn't exist because of
;; React's virtual DOM).
;;
(def all-state-changes? true)

(defn watch-state [target-app tool-reconciler timeout host-root]
  (let [inspect-new-state-f (update-inspect-state-hof tool-reconciler (reset! host-root-path* host-root))
        update-inspect-state-f (gfun/debounce inspect-new-state-f timeout)]
    (if (not all-state-changes?)
      (reset! state-inspector update-inspect-state-f)
      (add-watch (some-> target-app :reconciler :config :state) :chrismurrph/default-db-format
                 #(update-inspect-state-f %4)))))

(defn install-app [target-app]
  (let [tool-reconciler (:reconciler (global-inspector))
        shared-config (-> tool-reconciler prim/app-root prim/shared)
        timeout (-> shared-config :options :debounce-timeout)
        watch-st (partial watch-state target-app tool-reconciler timeout)
        host-root-path-preference (-> shared-config :options :host-root-path)
        msg-f (fn [whole-path]
                (if (= ignore-inspect whole-path)
                  (str "Discarding Fulcro Inspect root: " whole-path)
                  (str "Discarding: " whole-path)))]
    (if (-> host-root-path* deref nil?)
      (let [[whole-path leaf :as host-root] (app-path target-app)]
        (assert whole-path "Must be a host root")
        (cond
          (= host-root-path-preference whole-path) (watch-st host-root)
          (not= ignore-inspect whole-path) (watch-st host-root)
          :else (js/console.log (msg-f whole-path))))
      ;; If the wanted one is being discarded then need to add host-root-path to config, such that
      ;; the specified host-root-path will be the only one that is accepted. Will work as an
      ;; override if need to examine the state of Fulcro Inspect for instance. In which case set
      ;; host-root-path to "fulcro.inspect.core/GlobalRoot", equivalently default-db-format.tool/ignore-inspect
      (dev/log (str "Accepted a host already, so discarding: " (-> target-app app-path first))))))

(defn install [options]
  (when-not @global-inspector*
    (js/console.log "Installing" core/tool-name
                    (select-keys options [:collapse-keystroke :debounce-timeout :host-root-path]))
    (global-inspector options)

    (fulcro/register-tool
      {::fulcro/tool-id
       ::default-db-format

       ::fulcro/app-started
       (fn [{:keys [reconciler] :as app}]
         (install-app app)
         app)

       ::fulcro/tx-listen
       (fn [env _]
         (when-let [inspect-f @state-inspector]
           (inspect-f (:new-state env))))
       })))
