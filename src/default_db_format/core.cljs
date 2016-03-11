(ns default-db-format.core
  (:require [clojure.string :as s]
            [cljs.pprint :refer [pprint]]
            [om.core :include-macros true]
            [om.dom :include-macros true]
            [default-db-format.components :as components :refer [display-db-component okay?]]))

(enable-console-print!)

(defn ok? [check-result]
  (okay? check-result))

(defn show-hud [check-result]
  "Brings up into the browser an Om Next defui component whose render method returns either nil
  or a description of the lack of full normalization. This method should be called at the beginning
  of the application's root component's render method, usually just inside a div"
  (display-db-component check-result))

(def display components/display)

(defn by-id-kw-hof
  "When I looked all projects used 'by-id'. Never-the-less, this is configurable"
  [config-kw-str]
  (fn [kw]
    (and (keyword? kw)
         (= (name kw) config-kw-str)))) ;; name returns part after "/"

;;
;; [:graph-point/by-id 2003]
;;
(defn ident?
  "e.g. [:foo/by-id 203] passes, so long as first param is \"by-id\""
  [by-id-kw? tuple]
  (and (vector? tuple)
       (= 2 (count tuple))
       (by-id-kw? (first tuple))
       (number? (second tuple))))

(defn boolean? [v]
  (or (true? v) (false? v)))

(defn exclude-colon [s]
  (apply str (next s)))

(defn kw->str [kw]
  (-> kw str exclude-colon))

(defn category-part [s]
  (let [res (-> s
                (s/split #"/")
                first
                exclude-colon)]
    res))

(defn known-category [kw knowns]
  (let [before-slash (category-part (str kw))
        ;_ (println before-slash)
        ]
    (first (filter #(= % before-slash) knowns))))

(defn vec-of-idents? [by-id-kw? v]
  (or (and (vector? v) (not-empty v) (vector? (first v)) (empty? (remove (partial ident? by-id-kw?) v)))
      (and (vector? v) (empty? v))))

(defn ref-entry->error
  "Given non id top level keys, find out those not in correct format and return them.
  Returns nil if there is no error. Error wrapped in a vector for mapcat's benefit"
  [by-id-kw? knowns k v]
  (let [k-err (when (not (known-category k knowns)) [{:text (str "Unknown category") :problem (category-part (str k))}])]
    (if k-err
      k-err
      (if (nil? v)
        nil
        (if (not (seqable? v))
          [{:text (str "Not sequable") :problem (str "k: " k " v: " v)}]
          (if (empty? v)
            nil
            (let [val-is-ident (ident? by-id-kw? v)
                  val-is-vector-of-vectors (vec-of-idents? by-id-kw? v)
                  ;_ (println "is-ident: " val-is-ident ", is-vector-of-vectors: " val-is-vector-of-vectors ", val: " v)
                  ]
              (if (not val-is-vector-of-vectors)
                (if val-is-ident
                  nil
                  [{:text "Expect Idents" :problem k}])
                (let [non-idents (remove #(ident? by-id-kw? %) v)]
                  (when (pos? (count non-idents))
                    [{:text "The vector value should (but does not) contain only Idents" :problem k}]))))))))))

(defn map-of-partic-format?
  "Returns true if the shape of the test-map is as given by vec-format e.g. [:r :g :b] {:r 0 :g 0 :b}"
  [partic-vec-format test-map]
  (when (map? test-map)
    (let [;_ (println (str "FORMAT:" partic-vec-format " " test-map))
          truths (map (fn [good-key [test-k _]] (= good-key test-k)) partic-vec-format test-map)
          ;_ (println truths)
          res (filter false? truths)
          ]
      (empty? res))))

(defn known-map?
  [okay-value-maps test-map]
  (first (filter #(map-of-partic-format? % test-map) okay-value-maps)))

(def goog-date "function (opt_year, opt_month, opt_date, opt_hours,")
;;
;; This should catch anything complicated put into the state, for instance a channel:
;; #object[cljs.core.async.impl.channels.ManyToManyChannel]
;; - when you str it it becomes [object Object]
;; Hmm - that didn't catch time, so another check looking just for "function Date"
;; Hmm - need this to be user definable as who knows what types...
;; Have now done user definable - :acceptable-table-value-fn? in the input map
;; Strategy will be to hard-code common things here
;;
(defn anything-else?
  [v]
  ;(println "VAL: " (str v) ", OR: " v ", OR: " (type v) ", OR: " (str (type v)))
  (or (= "[object Object]" (subs (str v) 0 15))
      (= "function Date" (subs (str (type v)) 0 13))
      (= goog-date (subs (str (type v)) 0 (count goog-date)))
      ))

(defn- bad-inside-leaf-table-entries-val?
  "The (normalized) graph's values should only be true leaf data types or idents"
  [predicate-fns okay-value-maps v]
  (let [{:keys [by-id-kw? acceptable-table-value?]} predicate-fns]
    (assert (and by-id-kw? acceptable-table-value?))
    (not (or (nil? v)
             (number? v)
             (string? v)
             (ident? by-id-kw? v)
             (boolean? v)
             (keyword? v)
             (vec-of-idents? by-id-kw? v)
             (known-map? okay-value-maps v)
             (anything-else? v)
             (acceptable-table-value? v)
             ))))

(defn- bad-inside-table-entry-val? [predicate-fns okay-value-maps map-value]
  (for [[k v] map-value
        :let [problem? (bad-inside-leaf-table-entries-val? predicate-fns okay-value-maps v)
              msg-to-usr (when problem? [k v])]
        :when problem?]
    msg-to-usr))

(defn- gather-table-entry-bads-inside [predicate-fns okays-maps v]
  (let [bad-inside? (partial bad-inside-table-entry-val? predicate-fns okays-maps)
        res2 (mapcat (fn [kv] (when-let [res1 (bad-inside? (val kv))]
                               res1
                               )) v)]
    res2))

(defn table-entry->error
  "Given id top level keys, find out those not in correct format and return them
  Returns nil if there is no error. Specific hash-map data structure is returned"
  [predicate-fns okays-maps k v]
  (if (not (map? v))
    [(str "Value of " k " has to be a map")]
    (let [gathered (gather-table-entry-bads-inside predicate-fns okays-maps v)
          not-empty (not (empty? gathered))
          res {k (into {} gathered)}
          ]
      (when not-empty res))))

;(defn test-err []
;  (table-entry->error ["graph" "app"] :line/by-id (:line/by-id state)))

(defn- ref-entries-impl
  ([by-id-kw-fn? state excluded-keys]
   (filter (fn [kv]
             (let [k (key kv)]
               (and (not (contains? excluded-keys k))
                    (not (by-id-kw-fn? k))
                    (= 2 (count (s/split (kw->str k) #"/"))))))
           state))
  ([by-id-kw-fn? state]
    (ref-entries-impl by-id-kw-fn? state nil))
  )

(defn ref-entries
  "There are only two types of top level keys in 'default db format'. This function returns those for which
  the part after the / is not 'by-id' (easiest to say 'by-id', but the String used can be configured)"
  ([by-id-kw-fn? state excluded-keys]
   (into {} (ref-entries-impl by-id-kw-fn? state excluded-keys)))
  ([by-id-kw-fn? state]
   (into {} (ref-entries-impl by-id-kw-fn? state))))

(defn- table-entries-impl
  [by-id-kw-fn? state]
  (filter (fn [kv]
            (let [k (key kv)]
              (by-id-kw-fn? k)))
          state))

(defn table-entries
  "There are only two types of top level keys in 'default db format'. This function returns the 'by id' ones,
  where the part after the / is 'by-id' (easiest to say 'by-id', but the String used can be configured)"
  [by-id-kw-fn? state]
  (into {} (table-entries-impl by-id-kw-fn? state)))

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

(def default-config
  "Used internally. This default (and prevalent) way of 'by-id' can be
  overriden using config arg (:by-id-kw) to the check function"
  {:by-id-kw "by-id"})

(def always-false-fn (fn [_] false))

(def version
  "`lein clean` helps make sure using the latest version of this library.
  version value not changing alerts us to the fact that we have forgotten to `lein clean`"
  23)

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

(defn check
  "Checks to see if normalization works as expected. Returns a hash-map you can pprint
  config param keys:
  :by-id-kw  -> What comes after the slash in the Ident tuple-2's first position. As a String.
                By default is \"by-id\" as that's what project's I've looked at have used.
  :okay-value-maps -> Description using a vector where it is a real leaf thing, e.g. [:r :g :b] for colour
                will mean that {:r 255 :g 255 :b 255} is accepted. This is a set #{} of these
  :excluded-keys -> #{} of top level keys that we don't want to be part of normaliztion (must still be namespaced)
  :acceptable-table-value-fn? -> Predicate function so user can decide if the given value from table data is valid, 
                in that it is indented to be there, and does not indicate failed normalization"
  ([config state]
   (or (failed-state state)
       (let [are-not-slashed (not-slashed-keys state)]
         (if (seq are-not-slashed)
           (ret {:failed-assumption (incorrect "All top level keys must be namespaced (have a slash)" are-not-slashed)})
           (let [{:keys [okay-value-maps by-id-kw excluded-keys acceptable-table-value-fn?]} config
                 by-id-kw-fn? (by-id-kw-hof (or by-id-kw (:by-id-kw default-config)))
                 predicate-fns {:by-id-kw?               by-id-kw-fn?
                                :acceptable-table-value? (or acceptable-table-value-fn? always-false-fn)}
                 by-id (table-entries-impl by-id-kw-fn? state)
                 table-names (into #{} (map (comp category-part str key) by-id))
                 non-by-id (ref-entries-impl by-id-kw-fn? state excluded-keys)
                 ;_ (println "non by id:" non-by-id)
                 categories (into #{} (distinct (map (comp category-part str key) non-by-id)))]
             (if (empty? table-names)
               (ret {:failed-assumption (incorrect "by-id normalized file required")})
               (if (empty? categories)
                 (ret {:failed-assumption (incorrect
                                            "Expected to have categories - top level keywords should have a / in them,
                                            and the LHS is the name of the category")})
                 (let [ref-entries-tester (partial ref-entry->error by-id-kw-fn? categories)
                       id-tester (partial table-entry->error predicate-fns okay-value-maps)]
                   (ret {:categories  categories
                         :known-names table-names
                         :not-normalized-ref-entries
                                      (into #{}
                                            (mapcat (fn [kv] (ref-entries-tester (key kv) (val kv))) non-by-id))
                         :not-normalized-table-entries
                                      (into #{}
                                            (into {} (mapcat (fn [kv] (id-tester (key kv) (val kv))) by-id)))}))))
             )))))
  ([state]
   (check default-config state)))
