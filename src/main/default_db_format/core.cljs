(ns default-db-format.core
  (:require [clojure.string :as s]
            [fulcro.client.primitives :as prim]
            [cljs.pprint :refer [pprint]]
            [default-db-format.ui.components :as components :refer [display-db-component okay?]]
            [default-db-format.helpers :as help]))

(enable-console-print!)

(defn- probe [msg obj]
  (println (str (s/upper-case msg) ":\n" obj))
  obj)

(defn- probe-off [msg obj]
  obj)

(defn ok? [check-result]
  (okay? check-result))

(defn show-hud [check-result]
  "Brings up into the browser an Om Next defui component whose render method returns either nil
  or a description of the lack of full normalization. This method should be called at the beginning
  of the application's root component's render method, usually just inside a div"
  (display-db-component check-result))

(def display components/display)

(defn bool? [v]
  (or (true? v) (false? v)))

(defn known-category [kw knowns]
  (let [before-slash (help/category-part (str kw))
        ;_ (println before-slash)
        ]
    (first (filter #(= % before-slash) knowns))))

(defn vec-of-idents? [ident-like? v]
  (or (and (vector? v)
           (not-empty v)
           (vector? (first v))
           (empty? (remove ident-like? v)))
      (and (vector? v)
           (empty? v))))

(defn ref-entry->error-hof
  "Given non id top level keys, find out those not in correct format and return them.
  Returns nil if there is no error. Error wrapped in a vector for mapcat's benefit"
  [ident-like? knowns]
  (fn [[k v]]
    (let [k-err (when (not (known-category k knowns))
                  [{:text (str "Unknown category")
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
          :else [{:text "Expect Idents" :problem k}])))))

(defn map-of-partic-format?
  [partic-vec-format test-map]
  (and (map? test-map)
       (= (set (keys test-map)) (set partic-vec-format))))

(defn known-map?
  [okay-value-maps test-map]
  (first (filter #(map-of-partic-format? % test-map) okay-value-maps)))

;; Saying keys but could be anything
(defn vector-of-partic-keys?
  [partic-vec-keys test-vector]
  (when (vector? test-vector)
    (every? (set partic-vec-keys) test-vector)))

(defn known-vector?
  [okay-value-vectors test-vector]
  (first (filter #(vector-of-partic-keys? % test-vector) okay-value-vectors)))

(defn cljs-inst? [x]
  #_(inst? x)
  ;; Works for any version of cljs:
  (instance? js/Date x))

(def goog-date "function (opt_year, opt_month, opt_date, opt_hours,")
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
  (or (= "[object Object]" (subs (str v) 0 15))))

(defn goog-date? [v]
  (= goog-date (subs (str (type v)) 0 (count goog-date))))

;;
;; A temporary loading thing, not easily dumped
;;
(defn fulcro-fetch-state? [v]
  (-> v :ui/fetch-state map?))

(defn vector-of? [predicate-f?]
  (fn [v]
    (and (vector? v) (every? predicate-f? v))))

(def vector-of-instants? (vector-of? cljs-inst?))

(defn how-fine-inside-leaf-table-entries-val
  "The (normalized) graph's values should only be true leaf data types or idents"
  [predicate-fns okay-value-maps okay-value-vectors]
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
        (known-map? okay-value-maps val) :known-map
        (known-vector? okay-value-vectors val) :known-vector
        (fn? val) :function
        (cljs-inst? val) :instant
        (vector-of-instants? val) :instants
        (goog-date? val) :goog-date
        (anything-else? val) :anything-else
        (fulcro-fetch-state? val) :fulcro-fetch-state
        (acceptable-table-value-f? val) :acceptable-table-value))))

(defn- bad-inside-table-entry-val? [predicate-fns okay-value-maps okay-value-vectors keys-to-ignore]
  (fn [obj-map]
    (let [how-okay-f? (how-fine-inside-leaf-table-entries-val predicate-fns okay-value-maps okay-value-vectors)]
      (for [[k v] obj-map
            :let [how-okay (or (keys-to-ignore k) (how-okay-f? v))
                  ;_ (when (= :current-route k)
                  ;    (println ":current-route s/be okay:" v "\nhow okay:" how-okay))
                  problem? (nil? how-okay)
                  msg-to-usr (when problem? [k v])]
            :when problem?]
        msg-to-usr))))

(defn- gather-table-entry-bads-inside [predicate-fns okays-maps okays-vectors keys-to-ignore id-obj-map]
  (let [bad-inside? (bad-inside-table-entry-val? predicate-fns okays-maps okays-vectors keys-to-ignore)
        res2 (mapcat (fn [[_ obj-map]]
                       (when-let [res1 (bad-inside? obj-map)]
                         res1))
                     id-obj-map)]
    res2))

(defn table-entry->error-hof
  "Given id top level keys, find out those not in correct format and return them
  Returns nil if there is no error. Specific hash-map data structure is returned"
  [conformance-predicates okays-maps okays-vectors keys-to-ignore]
  (fn [[k v]]
    (if (not (map? v))
      [(str "Value of " k " has to be a map")]
      (let [gathered (gather-table-entry-bads-inside conformance-predicates okays-maps okays-vectors keys-to-ignore v)
            not-empty (not (empty? gathered))
            res {k (into {} gathered)}]
        (when not-empty res)))))

(defn kw->str [kw]
  (-> kw str help/exclude-colon))

(defn- ref-entries
  "There are only two types of top level keys in 'default db format'. This function returns those for which
  the part after the / is not 'by-id' nor a routing ident"
  ([ident-like? state excluded-keys]
   (filter (fn [kv]
             (let [k (key kv)]
               (and (= 2 (count (s/split (kw->str k) #"/")))
                    (not (contains? excluded-keys k))
                    (not (ident-like? k)))))
           state))
  ([ident-like? state]
   (ref-entries ident-like? state nil)))

;;
;; Making this a hard and fast rule, even for keys that are to be ignored
;;
(defn not-slashed-keys
  "Returns all the keys that are not namespaced"
  [state]
  (keys (filter (fn [kv]
                  (let [k (key kv)]
                    (not= 2 (count (s/split (kw->str k) #"/")))))
                state)))

(def always-false-fn (fn [_] false))

(def version
  "`lein clean` helps make sure using the latest version of this library.
  version value not changing alerts us to the fact that we have forgotten to `lein clean`"
  30)

(defn- ret [m]
  (merge m {:version version}))

(defn- incorrect
  ([text problems]
   (if (nil? problems)
     {:text text}
     {:text text :problems problems}))
  ([text] (incorrect text nil)))

(defn- state-looks-like-config [state]
  (let [{:keys [okay-value-maps by-id-kw excluded-keys acceptable-table-value-fn?]} state]
    (or okay-value-maps by-id-kw excluded-keys acceptable-table-value-fn?)))

(defn- failed-state [state]
  (if (not (map? state))
    (ret {:failed-assumption (incorrect "state param must be a map")})
    (when (state-looks-like-config state)
      (ret {:failed-assumption (incorrect "params order: config must be first, state second")}))))

;;
;; TODO
;; :many-okay-map-keys-sets, that is a set of sets, and relax requirements that every must be included
;; :many-okay-vector-vals-sets - same
;; :excluded-keys - must be a set
;;
(defn check
  "Checks to see if normalization works as expected. Returns a hash-map you can pprint
  config param keys:
  :by-id-kw -> What comes after the slash in the Ident tuple-2's first position. As a String.
               By default is \"by-id\" as that's what the convention is.
               Can be a #{} or [] of Strings where > 1 required.
  :routing-ns -> What comes before the slash for a routing Ident. For example with `[:routed/banking :top]`
               \"routed\" would be the routing namespace. Can be a #{} or [] of Strings where > 1 required.
  :okay-value-maps -> Description using a vector where it is a real leaf thing, e.g. [:r :g :b] for colour
               will mean that {:g 255 :r 255 :b 255} is accepted. This is a #{} or [] of these.
  :okay-value-vectors -> Allowed objects in a vector, e.g. [:report-1 :report-2] for a list of reports
               will mean that [:report-1] is accepted but [:report-1 :report-3] is not. Note that the order
               of the objects is not important. This is a #{} or [] of these.
  :excluded-keys -> #{} (or []) of keys that we don't want to be part of normalization (must still be namespaced)
  :acceptable-table-value-fn? -> Predicate function so user can decide if the given value from table data is valid, 
               in that it is intended to be there, and does not indicate failed normalization."
  ([config state]
   (or (failed-state state)
       (let [are-not-slashed (not-slashed-keys state)]
         (if (seq are-not-slashed)
           (ret {:failed-assumption (incorrect "All top level keys must be namespaced (have a slash)" are-not-slashed)})
           (let [config (merge help/default-config config)
                 {:keys [okay-value-maps okay-value-vectors excluded-keys acceptable-table-value-fn?]} config
                 ident-like? (help/ident-like-hof? config)
                 conformance-predicates {:ident-like?               ident-like?
                                         :acceptable-table-value-f? (or acceptable-table-value-fn? always-false-fn)}
                 by-id-kw? (-> config :by-id-kw help/setify help/by-id-kw-hof)
                 routed-ns? (-> config :routing-ns help/setify help/routed-ns-hof)
                 by-id-table-entries (help/table-entries by-id-kw? state)
                 table-names (into #{} (map (comp help/category-part str key) by-id-table-entries))
                 keys-to-ignore (help/setify excluded-keys)
                 non-by-id (ref-entries (some-fn by-id-kw? routed-ns?) state keys-to-ignore)
                 all-keys-count (+ (probe-off "count non-by-id" (count non-by-id)) (probe-off "count by-id" (count by-id-table-entries)))
                 categories (into #{} (distinct (map (comp help/category-part str key) non-by-id)))]
             (if (and (empty? table-names)
                      (pos? all-keys-count))
               (ret {:failed-assumption (incorrect "by-id normalized file required")})
               (let [ref-entries-tester (ref-entry->error-hof ident-like? categories)
                     okay-maps (help/setify okay-value-maps)
                     okay-vectors (help/setify okay-value-vectors)
                     id-tester (table-entry->error-hof conformance-predicates okay-maps okay-vectors keys-to-ignore)]
                 (ret {:categories  categories
                       :known-names table-names
                       :not-normalized-ref-entries
                                    (into #{}
                                          (mapcat (fn [kv] (ref-entries-tester kv)) non-by-id))
                       :not-normalized-table-entries
                                    (into #{}
                                          (into {} (mapcat (fn [kv] (id-tester kv)) by-id-table-entries)))}))))))))
  ([state]
   (check help/default-config state)))
