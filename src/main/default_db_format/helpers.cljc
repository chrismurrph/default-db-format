(ns default-db-format.helpers
  (:require [clojure.string :as s]
            [fulcro.client.primitives :as prim]
            [default-db-format.dev :as dev]
            [default-db-format.hof :as hof]))

(def expect-idents "Expect Ident/s")
(def expect-vector "Expect vector")

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
  [table-key? ident-single-id? state]
  (filter (fn [[k v]]
            (when (and dev/debug-check?
                       (or (nil? v)
                           (and (map? v) (= 1 (count v)))))
              (dev/debug-check "EXAMINE: " k v))
            (or (table-key? k)
                (ident-single-id? k v)))
          state))

(defn kw->str [kw]
  (-> kw str exclude-colon))

(defn join-entries
  "There are only two types of top level keys in 'default db format'. This function returns those for which
  the part after the / is neither 'by-id' nor a routing ident nor a link nor a 'one of'"
  ([table-key? one-of? state known-bad-joins]
   (filter (fn [[k v]]
             (and (= 2 (count (s/split (kw->str k) #"/")))
                  (not (contains? known-bad-joins k))
                  (not (table-key? k))
                  (not (one-of? k v))))
           state))
  ([ident-like? one-of? state]
   (join-entries ident-like? one-of? state nil)))

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

(def fulcro-skip-field-joins [:fulcro.ui.forms/form])
(def fulcro-skip-links [:fulcro/server-error :fulcro.client.routing/routing-tree])

;;
;; Not pluralizing even after setify, just for the sake of having a convention. This convention
;; only for these 'configuration' keys, and lasts right until they are needed for low level filter
;; or whatever they can be pluralised again (looks silly otherwise). Anything that isn't exactly
;; the same key gets pluralized here, evidence ignore-links and ignore-bad-field-joins.
;;
(defn config->init [{:keys [acceptable-map-value acceptable-vector-value skip-link skip-field-join] :as config}]
  (let [table-ending? (hof/reveal-f :table-ending config)
        table-pattern? (hof/reveal-f :table-pattern config)
        table-name? (hof/reveal-f :table-name config)
        routed-ns? (hof/reveal-f :before-slash-routing config)
        routed-name? (hof/reveal-f :after-slash-routing config)
        routing-table? (hof/reveal-f :routing-table-name config)]
    {:ident-single-id?        (hof/reveal-f :ident-one-of-id config)
     :map-entry-single-id?    (hof/reveal-f :map-entry-one-of-id config)
     :table-key?              (some-fn table-name? table-ending? table-pattern? routing-table? routed-ns? routed-name?)
     :acceptable-map-value    (->> acceptable-map-value
                                   hof/setify
                                   (remove (complement vector?))
                                   hof/setify)
     :acceptable-vector-value (->> acceptable-vector-value
                                   hof/setify
                                   (remove (complement vector?))
                                   hof/setify)
     :ignore-skip-links       (into (hof/setify skip-link) fulcro-skip-links)
     :ignore-skip-field-joins (into (hof/setify skip-field-join) fulcro-skip-field-joins)
     }))

;;
;; The outer function accepts the same config that check accepts.
;; Thus externally it can be used relatively easily.
;; [:graph-point/by-id 2003]
;;
(defn -ident-like-hof?
  [{:keys [table-key? ident-single-id?]}]
  (let [okay-key? (fn [cls]
                    (let [res (table-key? cls)]
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
          (or (ident-single-id? cls id)
              (and (okay-key? cls)
                   (okay-id? id))))))))

;;
;; When using the tool default merging will be done twice. That's because
;; `check` can be used all alone. And in messages we don't want the user to
;; see nothing when really '/by-id' '/BY-ID' will be there - that's the
;; reason for the early merge.
;;
(def default-edn-config
  "This default can be overridden using the config arg to the check function"
  {:table-ending #{"/by-id" "/BY-ID"}})

(defn ident-like-hof? [config]
  "Accepts the same config that check accepts. Returned function can be called `ident-like?`"
  (-ident-like-hof? (config->init (merge default-edn-config config))))

(def ident-like?
  "Instead of this use ident-like-hof? if you need other than default-config"
  (-ident-like-hof? (config->init default-edn-config)))

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
