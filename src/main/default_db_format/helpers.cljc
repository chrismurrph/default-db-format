(ns default-db-format.helpers
  (:require [clojure.string :as s]
            [fulcro.client.primitives :as prim]
            [default-db-format.dev :as dev]
            [default-db-format.hof :as hof]))

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
  [by-id-ending-fn? single-id? table? state]
  (let [table-like-key? (some-fn by-id-ending-fn? table?)]
    (filter (fn [[k v]]
              (when (or (nil? v) (and (map? v) (= 1 (count v))))
                (dev/debug-check "EXAMINE: " k v))
              (or (table-like-key? k)
                  (single-id? k v)))
            state)))

(defn my-uuid? [x]
  #?(:cljs (instance? UUID x)
     :clj  (instance? java.util.UUID x)))

;;
;; Including vector? seems controversial, but that's what Fulcro Inspector uses for ids,
;; for example for the table :fulcro.inspect.ui.element/panel-id. A vector is a fair
;; enough way to keep an index. Of course this shows a bit of a hole in our conceptualisation.
;; Basically any type can be used as an id, so why are we bothering to check at all?
;;
(def acceptable-id? (some-fn number? symbol? prim/tempid? keyword? string? my-uuid? vector?))

(defn config->fns [config]
  (let [by-id-ending? (hof/reveal-f :by-id-ending config)
        table? (hof/reveal-f :not-by-id-table config)
        routed-ns? (hof/reveal-f :before-slash-routing config)
        routed-name? (hof/reveal-f :after-slash-routing config)
        routing-table? (hof/reveal-f :routing-table config)
        not-join-key-f? (some-fn by-id-ending? routed-ns? routed-name? routing-table? table?)
        one-of-id? (hof/reveal-f :one-of-id config)]
    {:one-of-id? one-of-id?
     :by-id-ending? by-id-ending?
     :table? table?
     :not-join-key-f? not-join-key-f?}))

;;
;; The outer function accepts the same config that check accepts.
;; Thus externally it can be used relatively easily.
;; [:graph-point/by-id 2003]
;;
(defn -ident-like-hof?
  [{:keys [not-join-key-f? one-of-id?]}]
  (let [okay-key? (fn [cls]
                    (let [res (not-join-key-f? cls)]
                      (dev/log-off "acceptable key? " cls " " (boolean res))
                      res))
        okay-id? (fn [id]
                   (let [res (acceptable-id? id)]
                     (dev/log-off "acceptable id? " id " " (boolean res))
                     res))
        ]
    (fn [tuple]
      (when (and (vector? tuple)
                 (= 2 (count tuple)))
        (let [[cls id] tuple]
          (or (one-of-id? cls id)
              (and (okay-key? cls)
                   (okay-id? id))))))))

;;
;; When using the tool default merging will be done twice. That's because
;; `check` can be used all alone. And in messages we don't want the user to
;; see nothing when 'by-id' is there - that's the reason for the early merge.
;;
(def default-edn-config
  "This default can be overridden using the config arg to the check function.
  Each key here will be overridden by normal merge behaviour"
  {:by-id-ending #{"by-id" "BY-ID"}})

(defn ident-like-hof? [config]
  "Accepts the same config that check accepts. Returned function can be called `ident-like?`"
  (-ident-like-hof? (config->fns (merge default-edn-config config))))

(def ident-like?
  "Instead of this use ident-like-hof? if you need other than default-config"
  (-ident-like-hof? (config->fns default-edn-config)))

(defn kw->str [kw]
  (-> kw str exclude-colon))

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
