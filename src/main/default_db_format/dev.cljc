(ns default-db-format.dev
  (:require
    [fulcro.client.primitives :as prim]))

(def debug-check? false)
(def debug-config? false)
(def debug-visual? false)
(def debug-state-change? false)

#?(:cljs (enable-console-print!))

;;
;; Use apply on args to js/console.log to get devtools to format it properly
;;
(def log-pr #?(:cljs js/console.log
               :clj  println))

(defn init-state-atom [comp data]
  (atom (prim/tree->db comp (prim/get-initial-state comp data) true)))

(defn warn [& args]
  (apply log-pr "WARN:" args))

(defn log-off [& _])

(defn log [& args]
  (apply log-pr args))

(defn debug-check [& args]
  (when debug-check?
    (apply log-pr args)))

(defn debug-config [& args]
  (when debug-config?
    (apply log-pr args)))

(defn debug-visual [& args]
  (when debug-visual?
    (apply log-pr args)))

(defn debug-state-change [& args]
  (when debug-state-change?
    (apply log-pr args)))

(defn summarize [x]
  (cond
    (map? x) (let [counted (count x)]
               (if (> counted 5)
                 (str counted " map-entries; keys: " (vec (keys x)))
                 (->> x
                      (map (fn [[k v]]
                             [k (summarize v)]))
                      (into {}))))
    (coll? x) (let [counted (count x)]
                (if (> counted 5)
                  (str counted " items...")
                  x))
    :else x))

(def n-able? (every-pred coll? (complement map?)))
