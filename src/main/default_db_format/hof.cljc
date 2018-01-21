(ns default-db-format.hof)

;;
;; hof stands for 'higher order function'
;;

(defn by-id-ending-hof
  [config-kw-strs]
  (assert (set? config-kw-strs))
  ;'Unexpected identifier' JavaScript error, so can't debug here
  ;(dev/log (str "by-id-ending-hof given " config-kw-strs))
  (fn [kw]
    (and (keyword? kw)
         (some #{(name kw)} config-kw-strs))))

(defn map-entry-single-id-hof
  [config-ids]
  (assert (set? config-ids))
  (fn [_ v]
    (and (map? v)
         (= 1 (count v))
         (-> v ffirst config-ids))))

(defn single-id-hof
  [config-ids]
  (assert (set? config-ids))
  (fn [cls id]
    (config-ids id)))

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

(def kw->hof
  {:by-id-ending         by-id-ending-hof
   :one-of-id            map-entry-single-id-hof
   :not-by-id-table      not-by-id-table-hof
   :before-slash-routing routed-ns-hof
   :after-slash-routing  routed-name-hof
   :routing-table        routing-table-hof})

;;
;; Helps with the 'one or a set or a vector guarantee'. If don't have this requirement just use
;; the normal `set` constructor instead. For interring developer-user given parameters.
;;
(defn setify [in]
  (cond
    ((some-fn string? keyword?) in) #{in}
    (seq in) (cond (set? in) in
                   (sequential? in) (into #{} in)
                   :else #{in})
    :else #{}))

(defn reveal-f [kw config]
  ((kw->hof kw) (setify (kw config))))
