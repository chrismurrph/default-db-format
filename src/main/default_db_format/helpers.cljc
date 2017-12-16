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
  where the part after the / is 'by-id' (easiest to say 'by-id', but the String used can be configured)"
  [by-id-kw-fn? state]
  (filter (fn [[k _]]
            (by-id-kw-fn? k))
          state))

;;
;; Helps with the 'one or a set or a vector guarantee'. If don't have this requirement just use
;; `set` constructor instead, which won't wrap a set it param might be given.
;;
(defn setify [in]
  (if (seq in)
    (cond (set? in) in
          (sequential? in) (into #{} in)
          :else #{in})
    #{}))

(defn by-id-kw-hof
  [config-kw-strs]
  (assert (set? config-kw-strs))
  (fn [kw]
    (and (keyword? kw)
         (some #{(name kw)} config-kw-strs))))

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
  [{:keys [by-id-kw routing-ns]}]
  ;(dev/log (dev/assert-str "by-id-kw" by-id-kw))
  ;(dev/log (dev/assert-str "routing-ns" routing-ns))
  (let [by-id-kw? (-> by-id-kw setify by-id-kw-hof)
        routed-ns? (-> routing-ns setify routed-ns-hof)]
    (fn [tuple]
      (when (and (vector? tuple)
                 (= 2 (count tuple)))
        (let [[cls id] tuple]
          ;(println (dev/assert-str "tuple" tuple))
          ;(println (dev/assert-str "cls" cls))
          (and (or (dev/probe-off-msg "by-id-kw?" (by-id-kw? cls)) (dev/probe-off-msg "routed-ns?" (routed-ns? cls)))
               (dev/probe-off-msg "acceptable-id?" (acceptable-id? id))))))))

(def default-config
  "This default can be overridden using the config arg to the check function"
  {:by-id-kw   "by-id"
   :routing-ns "routed"})

(def ident-like?
  "Rather use ident-like-hof? if you need to use other than default-config"
  (ident-like-hof? default-config))
