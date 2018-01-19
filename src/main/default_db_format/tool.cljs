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
    ;; don't delete
            [default-db-format.ui.domain :as ui.domain]
            [clojure.string :as s]))

;;
;; TODO
;; This used to be necessary. There is a way of overriding that works.
;; However when overriding I could not get this 'app' to be picked up.
;; I'm guessing there have been changes to Fulcro Inspect, and we can
;; get rid of this. But lots of testing :-(
;;
(def ignore-fulcro-inspect "fulcro.inspect.core/GlobalRoot")

(def possible-lein-options [:collapse-keystroke :debounce-timeout :host-root-path])

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
;; React's virtual DOM, and can be obviated by say setting :debounce-timeout to 10000
;; i.e. 10 seconds).
;;
(def all-state-changes? true)

(def expanded-percentage-width 50)

;; A red line on the right edge of the container will be a
;; reminder to the user.
;; Hmm - mose well just have a red line rather than collapse it!
;; With some css (bootstrap) the css seems to be overridden so that the red dot is not displayed
;; For now we are living with it. But collapsing will work in such a situation, so perhaps
;; we can make collapsing an option in a future version. Yak shaving - no red dot is still fine
(def collapsed-percentage-width 1)

(def global-css (css/get-classnames ui.domain/CSS))

(defui ^:once CollapsibleFrame
       static prim/InitialAppState
       (initial-state [_ params] {:ui/visible?   false
                                  :ui/collapsed? false
                                  :ui/display-db (prim/get-initial-state components/DisplayDb params)})

       static prim/Ident
       (ident [_ props] [:floating-panel/by-id :UI])

       static prim/IQuery
       (query [_] [{:ui/display-db (prim/get-query components/DisplayDb)}
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
               (let [{:ui/keys [visible? collapsed? display-db]} (prim/props this)
                     toggle-collapse-f #(if (prim/props this)
                                          (mutations/set-value! this :ui/collapsed? (not collapsed?))
                                          (dev/warn (str "Collapse key is ignored when no state")))
                     keystroke (or (prim/shared this [:lein-options :collapse-keystroke]) "ctrl-q")
                     css (css/get-classnames CollapsibleFrame)]
                 (dom/div #js {:style (if visible? nil #js {:display "none"})}
                          (events/key-listener {::events/action    toggle-collapse-f
                                                ::events/keystroke keystroke})
                          (if collapsed?
                            (dom/div #js {:className (:red-dot global-css)
                                          :onClick   toggle-collapse-f} nil)
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
                                                                  (prim/computed display-db {:toggle-collapse-f toggle-collapse-f}))))))))))

(def ui-tool-view (prim/factory CollapsibleFrame))

(defui ^:once ToolRoot
       static prim/InitialAppState
       (initial-state [_ _] {:ui/react-key (random-uuid)
                             :ui/root      (prim/get-initial-state CollapsibleFrame {:tool-version core/tool-version
                                                                                     :tool-name    core/tool-name})})

       static prim/IQuery
       (query [_] [{:ui/root (prim/get-query CollapsibleFrame)}
                   :ui/react-key])

       static css/CSS
       (local-rules [_] [])
       (include-children [_] [CollapsibleFrame])

       Object
       (render [this]
               (let [{:keys [ui/react-key ui/root]} (prim/props this)]
                 (dom/div #js {:key react-key}
                          (ui-tool-view root)))))

(defonce ^:private tool* (atom nil))

(defn start-tool [lein-options]
  (let [
        ;; edn will include the `by-id` defaults, so there will be something for edn
        ;; even if there isn't an edn file. On the other hand :lein-options are what you
        ;; see in the file because merging onto the defaults (say for toggle keystroke)
        ;; has not yet been done.
        configuration {:lein-options (dissoc lein-options :edn)
                       :edn          (:edn lein-options)}
        app (fulcro/new-fulcro-client :shared configuration)
        node (js/document.createElement "div")]
    (dev/debug-config (str "Whole config in start-tool:\n" (dev/pp-str configuration)))
    (js/document.body.appendChild node)
    (css/upsert-css "default-db-format" ToolRoot)
    (fulcro/mount app ToolRoot node)))

(defn tool
  ([] @tool*)
  ([lein-options]
   (or @tool*
       (reset! tool* (start-tool lein-options)))))

(defn get-config
  "The host/target application can use this when it wants know the configuration it
  set at compile time (from lein and edn), at run-time. For example to call a function
  such as default-db-format.helpers/ident-like-hof. Will return nil when called before the
  app is initialized"
  []
  (some-> (tool) :reconciler prim/app-root prim/shared))

(defmutation state-inspection
  [{:keys [config new-state]}]
  (action [{:keys [state]}]
          (let [st @state
                check-result (core/check config new-state)
                visible? (-> check-result core/ok? not)
                floating-panel-ident [:floating-panel/by-id :UI]
                inspector-join (conj floating-panel-ident :ui/display-db)
                visible-join (conj floating-panel-ident :ui/visible?)
                collapsed-join (conj floating-panel-ident :ui/collapsed?)
                display-db-ident (get-in st inspector-join)]
            (swap! state #(cond-> %
                                  true (assoc-in visible-join visible?)
                                  visible? (assoc-in collapsed-join false)
                                  true (update-in display-db-ident merge check-result))))))

(defn dump []
  (-> (tool) :reconciler prim/app-state deref))

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
    (js/console.log (str core/tool-name " on " (first host-app-path) " - edn config: " (dev/summarize config)))
    (fn [new-state]
      (dev/debug-check (str "Listening to state change and it is okay? " (-> (core/check config new-state) core/detail-ok?)))
      (prim/transact! tool-reconciler [`(state-inspection {:config    ~config
                                                           :new-state ~new-state
                                                           }) [:floating-panel/by-id :UI]]))))

;;
;; Stores a function that takes new-state. Not used when all-state-changes? is true
;;
(defonce ^:private state-inspector (atom nil))

(defn app-path [app]
  (let [display-name (some-> app :reconciler prim/app-root prim/react-type (gobj/get "displayName"))]
    [display-name (some-> display-name (s/split #"/") last)]))

;;
;; Only ever used for deref being nil or not. Happens to get set to a vector, example:
;; ["default-db-format.baby-sharks/AdultRoot" "AdultRoot"]
;;
(defonce ^:private host-root-path* (atom nil))

(defn watch-state [get-target-state tool-reconciler timeout host-root]
  (let [inspect-new-state-f (update-inspect-state-hof tool-reconciler (reset! host-root-path* host-root))
        update-inspect-state-f (gfun/debounce inspect-new-state-f timeout)]
    (if all-state-changes?
      (add-watch (get-target-state) :chrismurrph/default-db-format
                 #(update-inspect-state-f %4))
      (reset! state-inspector update-inspect-state-f))))

;;
;; Assumption behind this function is that it is called for every possible
;; target-app. This function accepts what the preference is, else the first.
;; There's only any point in specifying a preference if this message is seen:
;; "Accepted a host already, so discarding: ..."
;;
(defn install-app! [target-app]
  (let [tool-reconciler (:reconciler (tool))
        lein-opts (-> tool-reconciler prim/app-root prim/shared :lein-options)
        _ (dev/debug-config (str "install-app! lein options: " lein-opts))
        get-target-state (fn []
                           (some-> target-app :reconciler :config :state))
        watch-st-f (partial watch-state get-target-state tool-reconciler (:debounce-timeout lein-opts))
        host-root-path-preference (:host-root-path lein-opts)]
    (if (-> host-root-path* deref nil?)
      (let [[whole-path _ :as host-root] (app-path target-app)]
        (assert whole-path "Must be a host root")
        (dev/debug-config (str "Examining: " whole-path))
        (cond
          (= host-root-path-preference whole-path)
          (watch-st-f host-root)

          (and (some? host-root-path-preference) (not= host-root-path-preference whole-path))
          (dev/log (str whole-path " being ignored because :host-root-path (from lein) set to: "
                        host-root-path-preference))

          (not= ignore-fulcro-inspect whole-path) (watch-st-f host-root)
          :else (dev/log ((if (= ignore-fulcro-inspect whole-path)
                            (str "Discarding Fulcro Inspect root: " whole-path)
                            (str "Discarding: " whole-path))))))
      ;; If the wanted one is being discarded then need to add host-root-path to config, such that
      ;; the specified host-root-path will be the only one that is accepted. Will work as an
      ;; override if need to examine the state of Fulcro Inspect for instance. In which case set
      ;; host-root-path to "fulcro.inspect.core/GlobalRoot", that's at default-db-format.tool/ignore-inspect
      (dev/log (str "Accepted a host already, so discarding: " (-> target-app app-path first))))))

(defn install [lein-options]
  (when-not @tool*
    (js/console.log "Installing" core/tool-name
                    "version" core/tool-version
                    "lein options" (select-keys lein-options possible-lein-options))
    (tool lein-options)

    (fulcro/register-tool
      {::fulcro/tool-id
       ::default-db-format

       ::fulcro/app-started
       (fn [{:keys [reconciler] :as app}]
         (install-app! app)
         app)

       ::fulcro/tx-listen
       (fn [env _]
         (when-let [inspect-f @state-inspector]
           (inspect-f (:new-state env))))})))
