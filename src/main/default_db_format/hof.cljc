(ns default-db-format.hof
  (:require [clojure.string :as str]
            [default-db-format.dev :as dev]))

;;
;; hof stands for 'higher order function'
;;

(defn by-id-ns-name-hof
  [config-kw-strs]
  (assert (set? config-kw-strs))
  ;'Unexpected identifier' JavaScript error, so can't debug here
  ;(dev/log (str "by-id-ending-hof given " config-kw-strs))
  (fn [kw]
    (and (keyword? kw)
         (some #{(name kw)} config-kw-strs))))

(defn table-ending-hof
  [config-kw-strs]
  (assert (set? config-kw-strs))
  (fn [kw]
    (and (keyword? kw)
         (some #(when (str/ends-with? (str kw) %) %)
               (filter string? config-kw-strs)))))

(defn my-regexp? [x]
  #?(:cljs (regexp? x)
     :clj  (instance? java.util.regex.Pattern x)))

(defn kw->string [kw]
  (when kw (assert (keyword? kw)))
  (and kw (subs (str kw) 1)))

(defn table-pattern-hof
  [config-kw-patterns]
  (assert (set? config-kw-patterns))
  (fn [kw]
    (and (keyword? kw)
         (some #(when (re-matches % (kw->string kw)) true)
               (filter my-regexp? config-kw-patterns)))))

(defn single-id-map-entry-hof
  [config-ids]
  (assert (set? config-ids))
  (fn [_ v]
    (and (map? v)
         (= 1 (count v))
         (-> v ffirst config-ids))))

(defn single-id-ident-hof
  [config-ids]
  (assert (set? config-ids))
  (fn [cls id]
    (config-ids id)))

(defn table-hof
  [config-tables]
  (assert (set? config-tables))
  (fn [kw]
    (some #{kw} config-tables)))

(def table-name-hof table-hof)
(def routing-table-name-hof table-hof)

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

(def kw->hof
  {:table-ending         table-ending-hof
   :ident-one-of-id      single-id-ident-hof
   :map-entry-one-of-id  single-id-map-entry-hof
   :table-name           table-name-hof
   :table-pattern        table-pattern-hof
   :before-slash-routing routed-ns-hof
   :after-slash-routing  routed-name-hof
   :routing-table-name   routing-table-name-hof})

;;
;; Helps with the 'one or a set or a vector guarantee'. If don't have this requirement just use
;; the normal `set` constructor instead. For interring developer-user given parameters.
;;
(defn setify [in]
  (cond
    ((some-fn string? keyword? my-regexp?) in) #{in}
    (seq in) (cond (set? in) in
                   (sequential? in) (into #{} in)
                   :else #{in})
    :else #{}))

(defn reveal-f [kw config]
  ((kw->hof kw) (setify (kw config))))
