(ns default-db-format.general.dev
  (:require
    [fulcro.client.primitives :as prim]
    #?(:cljs [cljs.pprint :as pp])
    #?(:clj
    [clojure.pprint :as pp])))

#?(:cljs (enable-console-print!))

(defn warn
  ([want? txt]
   (when-not want?
     (println (str "WARN: " txt #_" -> >" #_want? #_"<"))))
  ([txt]
   (warn true txt)))

(def width 120)

(defn pp-str
  ([n x]
   (binding [pp/*print-right-margin* n]
     (-> x pp/pprint with-out-str)))
  ([x]
   (pp-str width x)))

(defn pp
  ([n x]
   (binding [pp/*print-right-margin* n]
     (-> x pp/pprint)))
  ([x]
   (pp width x)))

(defn log-off [_])

(defn log [txt]
  (println txt))

(def debug-check? false)
(def debug-config? true)

(defn debug-check [txt]
  (when debug-check?
    (println txt)))

(defn debug-config [txt]
  (when debug-config?
    (println txt)))

(defn summarize [x]
  (str (cond
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
         :else x)))