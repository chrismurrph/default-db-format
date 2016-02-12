(ns default-db-format.core
  (:require [clojure.string :as s]
            [cljs.pprint :as pp :refer [pprint]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [default-db-format.components :refer [display-db-component okay?]]))

(enable-console-print!)

(defn ok? [check-result]
  (okay? check-result))

(defn display [check-result]
  "Brings up into the browser an Om Next defui component whose render method returns either nil
  or a description of the lack of full normalization. This method should be called at the beginning
  of the application's root component's render method, usually just inside a div"
  (display-db-component check-result))

;;
;; Not all config state is put in here, just some things inconvenient to load
;; from the top. Will get rid of later with high level functions created in main.
;; This just shows us the candidates for that refactor.
;;
(def local-atom-config (atom {:by-id-kw nil}))

;;
;; When I looked all projects used 'by-id'.
;;
(defn by-id-kw?
  [kw]
  (let [by-id-kw-str (:by-id-kw @local-atom-config)
        _ (assert by-id-kw-str)]
    (and (keyword? kw)
         (= (name kw) by-id-kw-str)))) ;; name returns part after "/"

;;
;; [:graph-point/by-id 2003]
;;
(defn ident? [tuple]
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

(defn vec-of-idents? [v]
  (and (vector? v) (not-empty v) (vector? (first v)) (empty? (remove ident? v))))

(defn non-id->error
  "Given non id top level keys, find out those not in correct format and return them.
  Returns nil if there is no error. Error wrapped in a vector for mapcat's benefit"
  [knowns k v]
  (let [k-err (when (not (known-category k knowns)) [{:text (str "Unknown category") :problem (category-part (str k))}])]
    (if k-err
      k-err
      (let [val-is-ident (ident? v)
            val-is-vector-of-vectors (vec-of-idents? v)]
        (if (not val-is-vector-of-vectors)
          (if val-is-ident
            nil
            [{:text "Expect Idents" :problem k}])
          (let [non-idents (remove #(ident? %) v)]
            (when (pos? (count non-idents))
              [{:text "The vector value should (but does not) contain only Idents" :problem k}])))))))

(defn map-of-partic-format?
  "Returns true if the shape of the test-map is as given by vec-format e.g. [:r :g :b] {:r 0 :g 0 :b}"
  [partic-vec-format test-map]
  (let [;_ (println (str "FORMAT:" vec-format " " test-map))
        truths (map (fn [good-key [test-k _]] (= good-key test-k)) partic-vec-format test-map)
        ;_ (println truths)
        res (filter false? truths)
        ]
    (empty? res)))

(defn known-map?
  [okay-value-maps test-map]
  (first (filter #(map-of-partic-format? % test-map) okay-value-maps)))

(defn- bad-inside-by-leaf-id-val?
  "The (normalized) graph's values should only be true leaf data types or idents"
  [okay-value-maps v]
  (not (or (number? v)
           (string? v)
           (ident? v)
           (boolean? v)
           (vec-of-idents? v)
           (known-map? okay-value-maps v))))

(defn- bad-inside-by-id-val? [okay-value-maps map-value]
  (for [[k v] map-value
        :let [problem? (bad-inside-by-leaf-id-val? okay-value-maps v)
              msg-to-usr (when problem? [k v])]
        :when problem?]
    msg-to-usr))

(defn- gather-bads-inside [okays-maps v]
  (let [bad-inside? (partial bad-inside-by-id-val? okays-maps)
        res2 (mapcat (fn [kv] (when-let [res1 (bad-inside? (val kv))]
                               res1
                               )) v)]
    res2))

(defn id->error
  "Given id top level keys, find out those not in correct format and return them
  Returns nil if there is no error. Specific hash-map data structure is returned"
  [okays-maps k v]
  (if (not (map? v))
    [(str "Value of " k " has to be a map")]
    (let [gathered (gather-bads-inside okays-maps v)
          not-empty (not (empty? gathered))
          res {k (into {} gathered)}
          ;_ (println "RESULT:" res ", not-empty " not-empty)
          ]
      (when not-empty res))))

;(defn test-err []
;  (id->error ["graph" "app"] :line/by-id (:line/by-id state)))

(defn non-by-id-entries
  "There are only two types of top level keys in 'default db format'. This function returns those for which
  the part after the / is not 'by-id' (easiest to say 'by-id', but the String used can be configured)"
  [state excluded-keys]
  (filter (fn [kv]
            (let [k (key kv)]
              (and (not (contains? excluded-keys k))
                   (not (by-id-kw? k))
                   (= 2 (count (s/split (kw->str k) #"/"))))))
          state))

(defn by-id-entries
  "There are only two types of top level keys in 'default db format'. This function returns the 'by id' ones,
  where the part after the / is 'by-id' (easiest to say 'by-id', but the String used can be configured)"
  [state]
  (filter (fn [kv]
            (let [k (key kv)]
              (by-id-kw? k)))
          state))

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

(def version
  "`lein clean` helps make sure using the latest version of this library.
  version value not changing alerts us to the fact that we have forgotten to `lein clean`"
  6)

(defn- ret [m]
  (merge m {:version version}))

;;
;; Later in html do equiv of this:
;; (apply str (interpose ", " are-not-slashed))
;; Also after comment might want to append: ", see: "
;;
(defn- incorrect
  ([text problems]
   (if (nil? problems)
     {:text text}
     {:text text :problems problems}))
  ([text] (incorrect text nil)))

(defn check
      "Checks to see if normalization works as expected. Returns a hash-map you can pprint
      config param keys:
      :by-id-kw  -> What comes after the slash in the Ident tuple-2's first position. As a String.
                    By default is \"by-id\" as that's what project's I've looked at have used.
      :okay-value-maps -> Description using a vector where it is a real leaf thing, e.g. [:r :g :b] for colour
                    will mean that {:r 255 :g 255 :b 255} is accepted. This is a set #{} of these
      :excluded-keys -> #{} of top level keys that we don't want to be part of normaliztion (must still be namespaced)"
      ([config state]
       (let [are-not-slashed (not-slashed-keys state)]
         (if (seq are-not-slashed)
           (ret {:failed-assumption (incorrect "All top level keys must be namespaced (have a slash)" are-not-slashed)})
           (let [{:keys [okay-value-maps by-id-kw excluded-keys]} config
                 _ (swap! local-atom-config assoc :by-id-kw (if by-id-kw by-id-kw (:by-id-kw default-config)))
                 by-id (by-id-entries state)
                 ;_ (println "num id:" (count by-id-entries))
                 names (into #{} (map (comp category-part str key) by-id))
                 non-by-id (non-by-id-entries state excluded-keys)
                 ;_ (println "non by id:" non-by-id)
                 categories (into #{} (distinct (map (comp category-part str key) non-by-id)))]
             (if (not (map? state))
               (ret {:failed-assumption (incorrect "state param must be a map")})
               (if (empty? names)
                 (ret {:failed-assumption (incorrect "by-id normalized file required")})
                 (if (empty? categories)
                   (ret {:failed-assumption (incorrect "Expected to have categories - top level keywords should have a / in them, and the LHS is the name of the category")})
                   (let [non-id-tester (partial non-id->error categories)
                         id-tester (partial id->error okay-value-maps)]
                     (ret {:categories             categories
                           :known-names            names
                           :not-normalized-not-ids (mapcat (fn [kv] (non-id-tester (key kv) (val kv))) non-by-id)
                           :not-normalized-ids     (into #{} (into {} (mapcat (fn [kv] (id-tester (key kv) (val kv))) by-id)))})))))))))
      ([state]
        (check default-config state)))
