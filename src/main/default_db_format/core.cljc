(ns default-db-format.core
  (:require [clojure.string :as s]
            [fulcro.client.primitives :as prim]
    #?(:cljs [cljs.pprint :refer [pprint]])
    #?(:clj
            [clojure.pprint :refer [pprint]])
    #?(:cljs [default-db-format.ui.components :as components :refer [display-db-component]])
            [default-db-format.helpers :as help]
            [default-db-format.hof :as hof]
            [default-db-format.ui.domain :as ui.domain]
            ))

(def tool-name "Default DB Format")
(def tool-version 30)

(defn bool? [v]
  (or (true? v) (false? v)))

#?(:cljs (enable-console-print!))

(def ok? ui.domain/okay?)

(def detail-ok? ui.domain/detail-okay?)

;;
;; Not needed when HUD is brought up from the tool.
;;
#?(:cljs
   (defn show-hud [check-result]
     "Brings up into the browser an Om Next defui component whose render method returns either nil
     or a description of the lack of full normalization. This method should be called at the beginning
     of the application's root component's render method, usually just inside a div"
     (display-db-component check-result)))

#?(:cljs
   (def display components/display))

(defn known-category [kw knowns]
  (let [before-slash (help/category-part (str kw))]
    (first (filter #(= % before-slash) knowns))))

(defn vec-of-idents? [ident-like? v]
  (or (and (vector? v)
           (not-empty v)
           (vector? (first v))
           (empty? (remove ident-like? v)))
      (and (vector? v)
           (empty? v))))

(defn join-entry->error-hof
  "Given non-table top level keys, find out those not in correct format and return them.
  Returns nil if there is no error. Error wrapped in a vector for mapcat's benefit"
  [ident-like? knowns]
  (fn [[k v]]
    (let [k-err (when (not (known-category k knowns))
                  [{:text    (str "Unknown category")
                    :problem (help/category-part (str k))}])]
      (when (seqable? v)
        (cond
          k-err k-err
          (nil? v) nil
          ;;Why would it have to be seqable? Single idents can be put at root level
          ;;(not (seqable? v)) [{:text (str "Not seqable") :problem [k v]}]
          (empty? v) nil
          (vec-of-idents? ident-like? v) (let [non-idents (remove ident-like? v)]
                                           (when (pos? (count non-idents))
                                             [{:text "The vector value should (but does not) contain only Idents" :problem k}]))
          (ident-like? v) nil
          ;;:ui/react-key is a string
          (string? v) nil
          :else [{:text "Expect Idents" :problem k}])))))

(defn map-of-partic-format?
  [partic-vec-format test-map]
  (and (map? test-map)
       (= (set (keys test-map)) (set partic-vec-format))))

(defn known-map?
  [acceptable-map-values test-map]
  (first (filter #(map-of-partic-format? % test-map) acceptable-map-values)))

;; Saying keys but could be anything
(defn vector-of-partic-keys?
  [partic-vec-keys test-vector]
  (when (vector? test-vector)
    (every? (set partic-vec-keys) test-vector)))

(defn known-vector?
  [acceptable-vector-values test-vector]
  (first (filter #(vector-of-partic-keys? % test-vector) acceptable-vector-values)))

(defn my-inst? [x]
  #_(inst? x)
  ;; Works for any version of cljs:
  #?(:cljs (instance? js/Date x)
     :clj  (instance? java.util.Date x)))

(def goog-date-type-info ((juxt identity count) "function (opt_year, opt_month, opt_date, opt_hours,"))
(def obj-info ((juxt identity count) "[object Object]"))
;;
;; This should catch anything complicated put into the state, for instance a channel:
;; #object[cljs.core.async.impl.channels.ManyToManyChannel]
;; - when you str it it becomes [object Object]
;; Hmm - that didn't catch time, so another check looking just for "function Date"
;; Hmm - need this to be user definable as who knows what types...
;; Have now done user definable - :acceptable-table-value-fn? in the input map
;; Strategy will be to hard-code common things here, so library user doesn't have to
;;
(defn anything-else?
  [v]
  ;(println "VAL: " (str v) ", OR: " v ", OR: " (type v) ", OR: " (str (type v)))
  (let [s (str v)
        [obj-boiler size-boiler] obj-info]
    (and
      (>= (count s) size-boiler)
      (= obj-boiler (subs s 0 size-boiler)))))

(defn goog-date? [v]
  (let [[goog-date-type-boiler size-goog-date-type-boiler] goog-date-type-info
        type-as-str (str (type v))]
    (and
      (>= (count type-as-str) size-goog-date-type-boiler)
      (= goog-date-type-boiler (subs type-as-str 0 size-goog-date-type-boiler)))))

;;
;; A temporary loading thing, not easily seen/dumped
;;
(defn fulcro-fetch-state? [v]
  (-> v :ui/fetch-state map?))

(defn vector-of? [predicate-f?]
  (fn [v]
    (and (vector? v) (every? predicate-f? v))))

(def vector-of-instants? (vector-of? my-inst?))

(defn how-fine-inside-leaf-table-entries-val
  "The (normalized) graph's values should only be true leaf data types or idents"
  [predicate-fns acceptable-map-value acceptable-vector-value]
  (fn [val]
    (let [{:keys [ident-like? acceptable-table-value-f?]} predicate-fns]
      (assert (and ident-like? acceptable-table-value-f?))
      (cond
        (nil? val) :nil
        (number? val) :number
        (string? val) :string
        (ident-like? val) :ident-like
        (prim/tempid? val) :tempid
        (bool? val) :bool
        (keyword? val) :keyword
        (symbol? val) :symbol
        (vec-of-idents? ident-like? val) :vec-of-idents
        (known-map? acceptable-map-value val) :known-map
        (known-vector? acceptable-vector-value val) :known-vector
        (fn? val) :function
        (my-inst? val) :instant
        (help/my-uuid? val) :uuid
        (vector-of-instants? val) :instants
        (goog-date? val) :goog-date
        (anything-else? val) :anything-else
        (fulcro-fetch-state? val) :fulcro-fetch-state
        (acceptable-table-value-f? val) :acceptable-table-value))))

(defn- bad-inside-table-entry-val? [predicate-fns acceptable-map-value acceptable-vector-value keys-to-ignore]
  (fn [obj-map]
    (let [how-okay-f? (how-fine-inside-leaf-table-entries-val
                        predicate-fns acceptable-map-value acceptable-vector-value)]
      (for [[k v] obj-map
            :let [how-okay (or (keys-to-ignore k) (how-okay-f? v))
                  ;_ (when (= :current-route k)
                  ;    (println ":current-route s/be okay:" v "\nhow okay:" how-okay))
                  problem? (nil? how-okay)
                  msg-to-usr (when problem? [k v])]
            :when problem?]
        msg-to-usr))))

(defn- gather-table-entry-bads-inside [predicate-fns acceptable-map-value acceptable-vector-value
                                       keys-to-ignore id-obj-map]
  (let [bad-inside? (bad-inside-table-entry-val?
                      predicate-fns acceptable-map-value acceptable-vector-value keys-to-ignore)]
    (mapcat (fn [[_ obj-map]]
              (bad-inside? obj-map))
            id-obj-map)))

(defn table-entry->error-hof
  "Given id top level keys, find out those not in correct format and return them
  Returns nil if there is no error. Specific hash-map data structure is returned"
  [conformance-predicates acceptable-map-value acceptable-vector-value keys-to-ignore]
  (fn [[k v]]
    (if (not (map? v))
      [(str "Value of " k " has to be a map")]
      (let [gathered (gather-table-entry-bads-inside conformance-predicates acceptable-map-value
                                                     acceptable-vector-value keys-to-ignore v)]
        (when (seq gathered)
          {k (into {} gathered)})))))

(defn- ret [m]
  (merge m {:version tool-version}))

(defn- incorrect [text]
  {:text text})

(defn- state-looks-like-config [state]
  (let [{:keys [acceptable-map-values by-id-ending bad-join acceptable-table-value-fn?]} state]
    (or acceptable-map-values by-id-ending bad-join acceptable-table-value-fn?)))

(defn- failed-state [state]
  (if (not (map? state))
    (ret {:failed-assumption (incorrect "State must be in the form of a map")})
    (when (state-looks-like-config state)
      (ret {:failed-assumption (incorrect "params order: config must be first, state second")}))))

;;
;; check takes keys of two types, function keys and collection keys
;;
(def collection-keys [:acceptable-map-value :acceptable-vector-value :bad-join])
(def check-keys (into #{} (concat collection-keys (keys hof/kw->hof))))

;;
;; Just so it is obvious. edn file contents goes straight to `check`.
;;
(def possible-edn-option-keys check-keys)
(def possible-lein-option-keys #{:collapse-keystroke :debounce-timeout :host-root-path})

(defn find-incorrect-keys [{:keys [lein-options edn]}]
  (let [unknown-lein-keys (clojure.set/difference (-> lein-options keys set) possible-lein-option-keys)
        unknown-edn-keys (clojure.set/difference (-> edn keys set) possible-edn-option-keys)]
    [unknown-lein-keys unknown-edn-keys]))

(defn -check
  ([config state]
   (or (failed-state state)
       (let [
             ;; In the case of the tool this defaulting has already been done. But we can't assume that
             ;; `check` will only be called from the tool - this is a public api. Rightmost wins so we can
             ;; do this without fear.
             config (merge help/default-edn-config config)
             {:keys [acceptable-table-value-fn?]} config
             {:keys [acceptable-map-value acceptable-vector-value ignore-links ignore-bad-field-joins one-of-id? table-key?]
              :as init-map} (help/config->init config)
             ident-like? (help/-ident-like-hof? init-map)
             conformance-predicates {:ident-like?               ident-like?
                                     :acceptable-table-value-f? (or acceptable-table-value-fn? (constantly false))}
             ;; TODO
             ;; Combine table-entries and join-entries to get the performance benefit of one parse. Esp true
             ;; since execution of the same predicates is repeated in both filter operations. Must put in some
             ;; tests before do this. Then use stopwatch.
             somehow-table-entries (help/table-entries table-key? one-of-id? state)
             top-level-joins (help/join-entries table-key? one-of-id? state ignore-links)
             table-names (into #{} (map (comp help/category-part str key) somehow-table-entries))
             all-keys-count (+ (count top-level-joins)
                               (count somehow-table-entries))
             no-tables? (and (empty? table-names)
                             (pos? all-keys-count))]
         (if no-tables?
           (do
             (ret {:failed-assumption (incorrect "by-id normalized file required")}))
           (let [categories (into #{} (distinct (map (comp help/category-part str key) top-level-joins)))
                 join-entries-tester (join-entry->error-hof ident-like? categories)
                 id-tester (table-entry->error-hof conformance-predicates acceptable-map-value
                                                   acceptable-vector-value ignore-bad-field-joins)]
             (ret {:categories  categories
                   :known-names table-names
                   ;;
                   ;; :bad-root-joins is (I think) only where a root level join
                   ;; (anything that is not a table is a root level join)
                   ;; does not have idents or vectors of idents in it
                   ;;
                   :bad-root-joins (into #{} (mapcat (fn [kv] (join-entries-tester kv)) top-level-joins))
                   ;;
                   ;; :bad-table-fields is where the table has been recognised, and is in the right
                   ;; format, but there are joins that do not have idents or vectors of idents in them
                   ;;
                   :bad-table-fields (into #{} (mapcat (fn [kv] (id-tester kv)) somehow-table-entries))}))))))
  ([state]
   (-check help/default-edn-config state)))

(def check
  "Checks to see if normalization works as expected. Returns a small hash-map indicating normalization health.
  config param keys:
  :by-id-ending -> What comes after the slash in the Ident tuple-2's first position. Must be a string.
               By default is #{\"by-id\" \"BY-ID\"} as that's what the convention is.
               Can be a #{} or [] or a single string when only 1 required.
  :one-of-id -> Something standard in the Ident tuple-2's second position, for components that the
               application only needs one of. Can be a #{} or [], just in case there are a few
               different variations on this convention.
  :not-by-id-table -> Some table names do not follow a \"by-id\" convention, and are not necessarily
               namespaced. Makes sense when there is no 'id', when there is only going
               to be one of these tables. Can be a #{} or []. Usually keyword/s.
  :routing-table -> Any table used as the first/class part of a routing ident. #{} or [] of these.
               Usually keywords but doesn't have to be.
  :link -> #{} (or [] or just a key) of root level join keys that we don't want to be part of
               normalization. This might happen if the join in the root component is to a component
               that does not have an Ident. Note that join keys that are not namespaced or just contain
               simple scalar data are ignored anyway. Here a link is defined as a root level join where
               the value is not normalized.
  :bad-field-join -> #{} (or [] or just a key) of field level join keys that we don't want to be part of
               normalization. Note that join keys that are not namespaced or just contain simple scalar
               data are ignored anyway.
  :acceptable-map-value -> Description using a vector where it is a real leaf thing, e.g. [:r :g :b] for colour
               will mean that {:g 255 :r 255 :b 255} is accepted. This is a #{} or [] of these.
  :acceptable-vector-value -> Allowed objects in a vector, e.g. [:report-1 :report-2] for a list of reports
               will mean that [:report-1] is accepted but [:report-1 :report-3] is not. Note that the order
               of the objects is not important. This is a #{} or [] of these.
  :acceptable-table-value-fn? -> Predicate function so user can decide if the given value from table data is valid,
               in that it is intended to be there, and does not indicate failed normalization. Will re-visit this
               functionality if it is required. See read-from-edn and think about not using edn/read-string., but
               instead the 'security hole' read-string.
               Hard-coding (as has been done for date instances) is a good idea for new things that
               pop up.
  These last two are also 'undocumented' as easy to just use `:routing-table`:
  :before-slash-routing -> What comes before the slash for a routing Ident. For example with `[:routed/banking :top]`
               \"routed\" would be the routing namespace. Can be a #{} or [] of Strings where > 1 required.
  :after-slash-routing -> What comes after the slash for a routing Ident. For example with `[:banking/routed :top]`
               \"routed\" would be the routing namespace. Can be a #{} or [] of Strings where > 1 required.
"
  (memoize -check))
