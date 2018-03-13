(ns default-db-format.tool
  (:require [fulcro.client.primitives :as prim :refer [defui defsc]]
            [fulcro.client :as fulcro]
            [fulcro.client.mutations :as mutations :refer-macros [defmutation]]
            [fulcro-css.css :as css]
            [fulcro.client.dom :as dom]
            [garden.core :as g]
            [default-db-format.ui.events :as events]
            [default-db-format.ui.components :as components]
            [default-db-format.dev :as dev]
            [goog.object :as gobj]
            [goog.functions :as gfun]
            [default-db-format.core :as core]
            [default-db-format.iframe :as iframe]
    ;; don't delete ui.domain
            [default-db-format.ui.domain :as ui.domain]
            [clojure.string :as s]
            [clojure.data :as data]
            [default-db-format.hof :as hof]))

;;
;; There is an order in which Fulcro dishes out the apps to the tools.
;; Depending upon their relative ordering in Lein preloads, either
;; Default DB Format will be dished out to Fulcro Inspect, or Fulcro Inspect
;; will be dished out to Default DB Format. The order is the reverse of the preloads order.
;; So to hide Default DB Format from Fulcro Inspect put Default DB Format before Fulcro Inspect.
;;
;; What is 'dished out'? See default-db-format.preload - this fn is called once
;; for every tool in preloads order. But order is not important for this call as is just
;; 'registration'. More importantly this registration allows a tool to provide a callback
;; at ::fulcro/app-started. The cb you give here is called for the other
;; apps (and a tool is an app). Usually there are three apps altogether if you include the
;; host app. When Default DB Format is before Inspect (which means after in preloads) it is
;; having its cb called second and is only seeing the host app, which is the first app. We can
;; also make it so that Default DB Format is third in order and so the cb at
;; ::fulcro/app-started is called twice. This explains the observation that either Inspect sees
;; default-db-format yet default-db-format does not see Inspect, or the other way round!
;;
;; So default-db-format will be able to handle being first in preloads order because it takes
;; the first app it is called with, so long at that app is not ignore-fulcro-inspect.
;; Fulcro Inspect currently has no mechanism to ignore Default DB Format.
;;
(def ignore-fulcro-inspect "fulcro.inspect.core/GlobalRoot")

;;
;; Why check on every transact when can check on every change to state?
;; (For instance react key changes are not picked up when on every transact)
;; One reason would be so don't check every time the developer saves his work,
;; slowing the machine down at the worst possible time for no good reason.
;; So a favourable choice is to not slow the developer's machine down at the expense
;; of missing a change to app state that did not go through a `transact!`.
;; And quite possibly there is no such thing - everything goes through `transact!`.
;; So originally I had this as false.
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
;; For now we are living with it. But collapsing WILL work in such a situation, so perhaps
;; we can make collapsing an option in a future version. Potential Yak shaving - no red dot is
;; still fine.
(def collapsed-percentage-width 1)

(def global-css (css/get-classnames ui.domain/CSS))

(defui ^:once CollapsibleFrame
       static prim/InitialAppState
       (initial-state [_ params] {:ui/visible?   false
                                  :ui/collapsed? true
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
                                          (let [collapsed? (-> this prim/props :ui/collapsed?)]
                                            (dev/debug-visual "Going to toggle collapsed? away from" collapsed?)
                                            (mutations/set-value! this :ui/collapsed? (not collapsed?)))
                                          (dev/warn "Collapse key is ignored when no state"))
                     keystroke (or (prim/shared this [:lein-options :collapse-keystroke]) "ctrl-q")
                     css (css/get-classnames CollapsibleFrame)]
                 (dev/debug-visual "collapsed?" collapsed?)
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
                                                                ;; Never saw this happening so deleted it - copied code from Fulcro Inspect
                                                                ;; that I don't understand the need for
                                                                #_(when-let [frame (gobj/get this "frame-dom")]
                                                                  (events/key-listener {::events/action    #((do
                                                                                                               (dev/debug-visual "Again going to toggle collapsed? away from" collapsed?)
                                                                                                               mutations/set-value!))
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

;;
;; Unsupported keys will naturally simply not be used
;;
(defn missing-keys-warnings [configuration]
  (let [[unknown-lein-keys unknown-edn-keys] (core/find-incorrect-keys configuration)]
    (when (seq unknown-lein-keys)
      (dev/warn "Unsupported lein option keys" unknown-lein-keys))
    (when (seq unknown-edn-keys)
      (dev/warn "Unsupported edn option keys" unknown-edn-keys))))

(defn consistency-warnings [{:keys []}])

(defn warning-if-not-all [kw type-pred? pred-plural-name edn]
  (let [endings (hof/setify (kw edn))
        bads (remove type-pred? endings)]
    (when (seq bads)
      (apply dev/warn "All values for" kw "must be" pred-plural-name
             (mapv (juxt type identity) bads)))))

;;
;; When do `check` that these bad values are ignored.
;; Had to be removed for the vector cases.
;; Nothing needed to be done if use a keyword instead of a string
;;
(defn key-values-warnings [{:keys [edn]}]
  (warning-if-not-all :table-ending string? "strings" edn)
  (warning-if-not-all :acceptable-map-value vector? "vectors" edn)
  (warning-if-not-all :acceptable-vector-value vector? "vectors" edn))

(defn start-tool [configuration]
  (missing-keys-warnings configuration)
  (consistency-warnings configuration)
  (key-values-warnings configuration)
  (let [
        ;; edn will include the `by-id` defaults, so there will be something for edn
        ;; even if there isn't an edn file. On the other hand :lein-options are what you
        ;; see in the file because merging onto the defaults (say for toggle keystroke)
        ;; has not yet been done.
        app (fulcro/new-fulcro-client :shared configuration)
        node (js/document.createElement "div")]
    (dev/debug-config "Whole config in start-tool:\n" configuration)
    (js/document.body.appendChild node)
    (css/upsert-css "default-db-format" ToolRoot)
    (fulcro/mount app ToolRoot node)))

(defn tool
  ([] @tool*)
  ([configuration]
   (or @tool*
       (reset! tool* (start-tool configuration)))))

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
                _ (dev/debug-visual "check-result" check-result)
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

(defonce ^:private last-state (atom nil))

;;
;; Normal path
;; Called from a watch on state, so whenever state changes
;;
(defn update-inspect-state-hof [tool-reconciler host-app-path]
  (let [config (-> tool-reconciler prim/app-root prim/shared :edn)]
    (dev/log core/tool-name "on" (str (first host-app-path) ",") "edn config:" config)
    (fn [new-state]
      (prim/transact! tool-reconciler [`(state-inspection {:config    ~config
                                                           :new-state ~new-state
                                                           }) [:floating-panel/by-id :UI]])
      (when dev/debug-state-change?
        (when @last-state
          ;; If ever really need to find out what is causing the state change, perhaps for
          ;; a bug where HUD has not been silenced.
          (dev/debug-state-change "diff" (data/diff @last-state new-state)))
        (reset! last-state new-state)))))

;;
;; Abnormal path
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
;; tool-name "is discarding ..."
;;
(defn install-app! [target-app]
  (let [tool-reconciler (:reconciler (tool))
        _ (assert tool-reconciler "No reconciler found in tool")
        lein-opts (-> tool-reconciler prim/app-root prim/shared :lein-options)
        _ (dev/debug-config "install-app! lein options:" lein-opts)
        get-target-state #(some-> target-app :reconciler :config :state)
        watch-st-f (partial watch-state get-target-state tool-reconciler (:debounce-timeout lein-opts))
        host-root-path-preference (:host-root-path lein-opts)]
    (if (-> host-root-path* deref nil?)
      (let [[whole-path _ :as host-root] (app-path target-app)]
        (assert whole-path "Must be a host root")
        (dev/debug-config "Examining:" whole-path)
        (cond
          (= host-root-path-preference whole-path)
          (watch-st-f host-root)

          (and (some? host-root-path-preference) (not= host-root-path-preference whole-path))
          (dev/log whole-path "being ignored because" :host-root-path "(from lein) set to:"
                   host-root-path-preference)

          (not= ignore-fulcro-inspect whole-path) (watch-st-f host-root)

          :else (if (= ignore-fulcro-inspect whole-path)
                  (dev/debug-config core/tool-name "is discarding Fulcro Inspect root" whole-path)
                  (dev/log core/tool-name "should never see this!! (sanity check)"))))
      ;; If the wanted one is being discarded then need to add :host-root-path to config, such that
      ;; the specified host-root-path will be the only one that is accepted. Will work as an
      ;; override if need to examine the state of Fulcro Inspect for instance. In which case set
      ;; host-root-path to "fulcro.inspect.core/GlobalRoot", that's at
      ;; default-db-format.tool/ignore-fulcro-inspect
      (dev/debug-config core/tool-name "has accepted a host already, so discarding"
                        (-> target-app app-path first)))))

(defn install [{:keys [lein-options] :as configuration}]
  (dev/debug-config "configuration:" configuration)
  (when-not @tool*
    (dev/log "Installing" (str core/tool-name ",")
             "version:" core/tool-version
             (select-keys lein-options core/possible-lein-option-keys))
    (tool configuration)

    (fulcro/register-tool
      {::fulcro/tool-id
       ::default-db-format

       ::fulcro/app-started
       (fn [app]
         (install-app! app)
         app)

       ::fulcro/tx-listen
       (fn [env _]
         (when-let [inspect-f @state-inspector]
           (inspect-f (:new-state env))))})))
