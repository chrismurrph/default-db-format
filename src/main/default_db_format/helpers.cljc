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
  [by-id-kw-fn? table? state]
  (filter (fn [[k _]]
            (println "EXAMINE:" k)
            (or (by-id-kw-fn? k) (table? k)))
          state))

;;
;; Helps with the 'one or a set or a vector guarantee'. If don't have this requirement just use
;; `set` constructor instead, which won't wrap a set it param might be given.
;;
(defn -setify [in]
  (cond
    ((some-fn string? keyword?) in) #{in}
    (seq in) (cond (set? in) in
                   (sequential? in) (into #{} in)
                   :else #{in})
    :else #{}))

(defn by-id-kw-hof
  [config-kw-strs debug?]
  (assert (set? config-kw-strs))
  ;'Unexpected identifier' JavaScript error, so can't debug here
  ;(dev/log (str "by-id-kw-hof given " config-kw-strs))
  (fn [kw]
    (when debug?
      (dev/log (str "by-id-kw? for " kw)))
    (and (keyword? kw)
         (some #{(name kw)} config-kw-strs))))

(defn not-by-id-table-hof
  [config-kw-tables]
  (assert (set? config-kw-tables))
  (fn [kw]
    (and (keyword? kw)
         (some #{kw} config-kw-tables))))

(defn routed-ns-hof
  [config-ns-strs]
  (assert (set? config-ns-strs))
  (fn [namespaced-kw]
    (and (keyword? namespaced-kw)
         (let [ns (namespace namespaced-kw)]
           (and ns
                (some #{ns} config-ns-strs))))))

(def acceptable-id? (some-fn number? symbol? prim/tempid? keyword? string?))

;;
;; The outer function accepts the same config that check accepts.
;; Thus externally it can be used relatively easily.
;; [:graph-point/by-id 2003]
;;
(defn ident-like-hof?
  "Accepts the same config that check accepts. Returned function can be called `ident-like?`"
  [{:keys [by-id-kw routing-ns not-by-id-table]}]
  ;(dev/log (dev/assert-str "by-id-kw" by-id-kw))
  ;(dev/log (dev/assert-str "routing-ns" routing-ns))
  (let [by-id-kw? (-> by-id-kw -setify (by-id-kw-hof false))
        routed-ns? (-> routing-ns -setify routed-ns-hof)
        table? (-> not-by-id-table -setify not-by-id-table-hof)
        ]
    (fn [tuple]
      (when (and (vector? tuple)
                 (= 2 (count tuple)))
        (let [[cls id] tuple]
          (and (or (dev/probe-off-msg "by-id-kw?" (by-id-kw? cls))
                   (dev/probe-off-msg "routed-ns?" (routed-ns? cls))
                   (dev/probe-off-msg "table?" (table? cls)))
               (dev/probe-off-msg "acceptable-id?" (acceptable-id? id))))))))

(def default-config
  "This default can be overridden using the config arg to the check function.
  Each key here will be overridden by normal merge behaviour"
  {:by-id-kw   #{"by-id" "BY-ID"}
   :routing-ns "routed"})

(def ident-like?
  "Instead of this use ident-like-hof? if you need other than default-config"
  (ident-like-hof? default-config))
