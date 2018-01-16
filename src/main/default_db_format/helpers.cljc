(ns default-db-format.helpers
  (:require [clojure.string :as s]
            [fulcro.client.primitives :as prim]
            [default-db-format.general.dev :as dev]))

(defn exclude-colon [s]
  (apply str (next s)))

(defn category-part [s]
  (-> s
      (s/split #"/")
      first
      exclude-colon))

(defn table-entries
  "There are only two types of top level keys in 'default db format'. This function returns the 'by id' ones,
  where the part after the / is 'by-id' (easiest to say 'by-id', but the String used can be configured).
  Also included are table names given to config that are not necessarily namespaced. A convention may develop
  whereby 'one of' tables do not follow some /by-id or /id convention, as there isn't an id in these cases."
  [by-id-kw-fn? single-id? table? state]
  (let [table-like-key? (some-fn by-id-kw-fn? table?)]
    (filter (fn [[k v]]
              (when (or (nil? v) (and (map? v) (= 1 (count v))))
                (dev/debug (str "EXAMINE: " k v)))
              (or (table-like-key? k)
                  (single-id? k v)))
            state)))

;;
;; Helps with the 'one or a set or a vector guarantee'. If don't have this requirement just use
;; the normal `set` constructor instead. For interring developer-user given parameters.
;;
(defn -setify [in]
  (cond
    ((some-fn string? keyword?) in) #{in}
    (seq in) (cond (set? in) in
                   (sequential? in) (into #{} in)
                   :else #{in})
    :else #{}))

(defn by-id-kw-hof
  [config-kw-strs]
  (assert (set? config-kw-strs))
  ;'Unexpected identifier' JavaScript error, so can't debug here
  ;(dev/log (str "by-id-kw-hof given " config-kw-strs))
  (fn [kw]
    (and (keyword? kw)
         (some #{(name kw)} config-kw-strs))))

(defn map-entry-single-id-hof
  [config-ids]
  (assert (set? config-ids))
  (fn [_ v]
    (and (map? v)
         (= 1 (count v))
         (-> v ffirst config-ids))
    ))

(defn single-id-hof
  [config-ids]
  (assert (set? config-ids))
  (fn [cls id]
    (config-ids id)
    ))

(defn table-hof
  [config-tables]
  (assert (set? config-tables))
  (fn [kw]
    (some #{kw} config-tables)))

(def not-by-id-table-hof table-hof)
(def routing-table-hof table-hof)

;;
;; ns means before the slash.
;; After fn will be called routed-name-hof
;;
(defn routed-ns-hof
  [config-ns-strs]
  (assert (set? config-ns-strs))
  (fn [namespaced-kw]
    (and (keyword? namespaced-kw)
         (let [ns (namespace namespaced-kw)]
           (and ns
                (some #{ns} config-ns-strs))))))

(defn routed-name-hof
  [config-ns-strs]
  (assert (set? config-ns-strs))
  (fn [namespaced-kw]
    (and (keyword? namespaced-kw)
         (let [nm (name namespaced-kw)]
           (and nm
                (some #{nm} config-ns-strs))))))

(def acceptable-id? (some-fn number? symbol? prim/tempid? keyword? string?))

;;
;; The outer function accepts the same config that check accepts.
;; Thus externally it can be used relatively easily.
;; [:graph-point/by-id 2003]
;;
(defn ident-like-hof?
  "Accepts the same config that check accepts. Returned function can be called `ident-like?`"
  [{:keys [by-id-kw by-one-id before-slash-routing after-slash-routing not-by-id-table routing-tables]}]
  (let [by-id-kw? (-> by-id-kw -setify by-id-kw-hof)
        by-one-id? (-> by-one-id -setify single-id-hof)
        routed-ns? (-> before-slash-routing -setify routed-ns-hof)
        routed-name? (-> after-slash-routing -setify routed-name-hof)
        table? (-> not-by-id-table -setify not-by-id-table-hof)
        routing-table? (-> routing-tables -setify routing-table-hof)
        acceptable-key? (some-fn by-id-kw? routed-ns? routed-name? table? routing-table?)
        okay-key? (fn [cls]
                    (let [res (acceptable-key? cls)]
                      (dev/log-off (str "acceptable key? " cls " " (boolean res)))
                      res))
        okay-id? (fn [id]
                   (let [res (acceptable-id? id)]
                     (dev/log-off (str "acceptable id? " id " " (boolean res)))
                     res))
        ]
    (fn [tuple]
      (when (and (vector? tuple)
                 (= 2 (count tuple)))
        (let [[cls id] tuple]
          (or (by-one-id? cls id)
              (and (okay-key? cls)
                   (okay-id? id))))))))

(def default-config
  "This default can be overridden using the config arg to the check function.
  Each key here will be overridden by normal merge behaviour"
  {:by-id-kw #{"by-id" "BY-ID"}})

(def ident-like?
  "Instead of this use ident-like-hof? if you need other than default-config"
  (ident-like-hof? default-config))
