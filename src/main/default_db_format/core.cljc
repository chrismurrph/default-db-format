(ns default-db-format.core
  (:require [clojure.string :as s]
            [fulcro.client.primitives :as prim]
    #?(:cljs [cljs.pprint :refer [pprint]])
    #?(:clj
            [clojure.pprint :refer [pprint]])
    #?(:cljs [default-db-format.ui.components :as components :refer [display-db-component okay? detail-okay?]])
            [default-db-format.helpers :as help]
            [default-db-format.general.dev :as dev]
            ))

(def tool-name "Default DB Format")
(def tool-version
  "`lein clean` helps make sure using the latest version of this library.
  version value not changing alerts us to the fact that we have forgotten to `lein clean`"
  30)

(def always-false-fn (fn [_] false))

(defn bool? [v]
  (or (true? v) (false? v)))

#?(:cljs (enable-console-print!))

#?(:cljs
   (def ok? okay?))

#?(:cljs
   (def detail-ok? detail-okay?))

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
  "Given non id top level keys, find out those not in correct format and return them.
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
  #?(:cljs (instance? js/Date x)
     :clj  (instance? java.util.Date x)))

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
  (let [sz-goog-date (count goog-date)
        type-as-str (str (type v))]
    (and
      (>= (count type-as-str) sz-goog-date)
      (= goog-date (subs type-as-str 0 sz-goog-date)))))

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
            not-empty (seq gathered)]
        (when not-empty
          {k (into {} gathered)})))))

(defn kw->str [kw]
  (-> kw str help/exclude-colon))

(defn- join-entries
  "There are only two types of top level keys in 'default db format'. This function returns those for which
  the part after the / is neither 'by-id' nor a routing ident nor a link nor a 'one of'"
  ([ident-like? one-of? state links]
   (filter (fn [[k v]]
             (and (= 2 (count (s/split (kw->str k) #"/")))
                  (not (contains? links k))
                  (not (ident-like? k))
                  (not (one-of? k v))))
           state))
  ([ident-like? one-of? state]
   (join-entries ident-like? one-of? state nil)))

;;
;; In the past made this a hard and fast rule, even for keys that are to be ignored
;; Now no longer using!
;;
(defn not-slashed-keys
  "Returns all the keys that are not namespaced"
  [state]
  (keys (filter (fn [kv]
                  (let [k (key kv)]
                    (not= 2 (count (s/split (kw->str k) #"/")))))
                state)))

(defn- ret [m]
  (merge m {:version tool-version}))

(defn- incorrect [text]
  {:text text})

(defn- state-looks-like-config [state]
  (let [{:keys [okay-value-maps by-id-kw links acceptable-table-value-fn?]} state]
    (or okay-value-maps by-id-kw links acceptable-table-value-fn?)))

(defn- failed-state [state]
  (if (not (map? state))
    (ret {:failed-assumption (incorrect "State must be in the form of a map")})
    (when (state-looks-like-config state)
      (ret {:failed-assumption (incorrect "params order: config must be first, state second")}))))

(def fulcro-links [:fulcro/server-error :fulcro.ui.forms/form :fulcro.client.routing/routing-tree])

(defn -check
  "Checks to see if normalization works as expected. Returns a hash-map you can pprint
  config param keys:
  :by-id-kw -> What comes after the slash in the Ident tuple-2's first position. As a String.
               By default is \"by-id\" as that's what the convention is.
               Can be a #{} or [] of Strings where > 1 required.
  :one-of-id -> Something standard in the Ident tuple-2's second position, for components that the
               application only needs one of. Can be a #{} or [], just in case there are a few
               different variations on this convention.
  :not-by-id-table -> Some table names do not follow a \"by-id\" convention, and are not necessarily
               namespaced. Legitimate convention when there is no 'id', when there is only going
               to be one of these tables. Can be a #{} or []. Usually keyword/s.
  :routing-tables -> Any table used as the first/class part of a routing ident. #{} or [] of these.
               Usually keywords but doesn't have to be.
  :okay-value-maps -> Description using a vector where it is a real leaf thing, e.g. [:r :g :b] for colour
               will mean that {:g 255 :r 255 :b 255} is accepted. This is a #{} or [] of these.
  :okay-value-vectors -> Allowed objects in a vector, e.g. [:report-1 :report-2] for a list of reports
               will mean that [:report-1] is accepted but [:report-1 :report-3] is not. Note that the order
               of the objects is not important. This is a #{} or [] of these.
  :links -> #{} (or []) of keys that we don't want to be part of normalization. Both joins and links exist
               at the top level, and we want to ensure that normalization checking is still done for the
               top level joins. A top level join is a join in the root component. Links and joins are
               indistinguishable when looking at state. Top level joins may contain non-normalized data,
               and need to be 'fixed' by being included here. This might happen if the join in the root
               component is to a component that does not have an ident. Note that top level keys that are
               not namespaced or just contain simple data are ignored anyway (assumed to be links).
  :acceptable-table-value-fn? -> Predicate function so user can decide if the given value from table data is valid, 
               in that it is intended to be there, and does not indicate failed normalization.
  These last two are 'undocumented' as easy to just use `:routing-tables`:
  :before-slash-routing -> What comes before the slash for a routing Ident. For example with `[:routed/banking :top]`
               \"routed\" would be the routing namespace. Can be a #{} or [] of Strings where > 1 required.
  :after-slash-routing -> What comes after the slash for a routing Ident. For example with `[:banking/routed :top]`
               \"routed\" would be the routing namespace. Can be a #{} or [] of Strings where > 1 required.
"
  ([config state]
   (or (failed-state state)
       (let [
             ;; In the case of the tool this defaulting has already been done. But we can't assume that
             ;; `check` will only be called from the tool - this is a public api. Rightmost wins so we can
             ;; do this without fear.
             config (merge help/default-config config)
             {:keys [okay-value-maps okay-value-vectors links acceptable-table-value-fn?]} config
             ident-like? (help/ident-like-hof? config)
             conformance-predicates {:ident-like?               ident-like?
                                     :acceptable-table-value-f? (or acceptable-table-value-fn? always-false-fn)}
             by-id-kw? (-> config :by-id-kw help/-setify help/by-id-kw-hof)
             single-id? (-> config :one-of-id help/-setify help/map-entry-single-id-hof)
             table? (-> config :not-by-id-table help/-setify help/not-by-id-table-hof)
             routed-ns? (-> config :before-slash-routing help/-setify help/routed-ns-hof)
             routed-name? (-> config :after-slash-routing help/-setify help/routed-name-hof)
             routing-table? (-> config :routing-tables help/-setify help/routing-table-hof)
             somehow-table-entries (help/table-entries by-id-kw? single-id? table? state)
             table-names (into #{} (map (comp help/category-part str key) somehow-table-entries))
             keys-to-ignore (help/-setify (into links fulcro-links))
             not-join-key-f? (some-fn by-id-kw? routed-ns? routed-name? routing-table? table?)
             top-level-joins (join-entries not-join-key-f? single-id? state keys-to-ignore)
             all-keys-count (+ (count top-level-joins)
                               (count somehow-table-entries))
             no-tables? (and (empty? table-names)
                             (pos? all-keys-count))]
         (if no-tables?
           (do
             (ret {:failed-assumption (incorrect "by-id normalized file required")}))
           (let [categories (into #{} (distinct (map (comp help/category-part str key) top-level-joins)))
                 join-entries-tester (join-entry->error-hof ident-like? categories)
                 okay-maps (help/-setify okay-value-maps)
                 okay-vectors (help/-setify okay-value-vectors)
                 id-tester (table-entry->error-hof conformance-predicates okay-maps okay-vectors keys-to-ignore)]
             (ret {:categories  categories
                   :known-names table-names
                   :not-normalized-join-entries
                                (into #{} (mapcat (fn [kv] (join-entries-tester kv)) top-level-joins))
                   :not-normalized-table-entries
                                (into #{} (mapcat (fn [kv] (id-tester kv)) somehow-table-entries))}))))))
  ([state]
   (-check help/default-config state)))

(def check (memoize -check))
