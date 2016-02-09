(ns default-db-format.core
  (:require [clojure.string :as s]
            [cljs.pprint :as pp :refer [pprint]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [examples :as examples]))

(enable-console-print!)

;;
;; Only exists for purpose of experimenting with boot
;;
(defn widget [data owner]
      (reify
        om/IRender
        (render [this]
                (dom/h1 nil (:text data)))))
(defn init []
      (println "Where's browser console?")
      (om/root widget {:text "On thinking.."}
               {:target (. js/document (getElementById "container"))}))

(defn by-id-kw?
  "Config should include {:ident-strings []} b/c user shouldn't have
   to call them all by-id in the denorm state"
  [kw]
  (and (keyword? kw)
       (= (name kw) "by-id"))) ;; name returns part after "/"

;;
;; [:graph-point/by-id 2003]
;;
(defn ident? [tuple]
  (and (vector? tuple)
       (= 2 (count tuple))
       (by-id-kw? (first tuple))
       (number? (second tuple))))

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

(defn category-part-of-ident [ident]
  (-> ident first str category-part))

(defn known-category [kw knowns]
  (let [before-slash (category-part (str kw))
        ;_ (println before-slash)
        ]
    (first (filter #(= % before-slash) knowns))))

(defn vec-of-vec? [v]
  (and (vector? v) (not-empty v) (vector? (first v))))

(defn non-id->error
  "Returns nil if there is no error. Error wrapped in a vector for mapcat's benefit"
  [knowns k v]
  (let [k-err (if (not (known-category k knowns)) (str "Unknown category: " (category-part (str k))))]
    (if k-err
      [k-err]
      (let [val-is-ident (ident? v)
            val-is-vector-of-vectors (vec-of-vec? v)]
        (if (not val-is-vector-of-vectors)
          (if val-is-ident
            nil
            [(str "Expect to always have Idents, problem at: " k)])
          (let [_ (println "vec of vect of size " (count v))
                non-idents (remove #(ident? %) v)]
            (when (pos? (count non-idents))
              [(str "The vector value should (but does not) contain only Idents, at: " k)])))))))

(defn known-map?
  [okay-maps test-map]
  (first (filter #(= % test-map) okay-maps)))

(defn bad-inside-by-leaf-id-val?
  "The (normalized) graph's values should only be true leaf data types or idents"
  [okay-maps v]
  (not (or (number? v)
           (string? v)
           (known-map? okay-maps v)
           (ident? v)
           (vec-of-vec? v))))

(defn bad-inside-by-id-val? [okay-maps map-key map-value]
  (for [[k v] map-value
        :let [problem? (bad-inside-by-leaf-id-val? okay-maps v)
              msg-to-usr (when problem? (str "In " map-key ": " k ", " v))]
        :when problem?]
    msg-to-usr))

(defn id->error [okays-maps k v]
  (if (not (map? v))
    [(str "Value of " k " has to be a map")]
    (let [bad-inside? (partial bad-inside-by-id-val? okays-maps)]
      (mapcat (fn [kv] (when-let [res (bad-inside? k (val kv))]
                         ;[(str "Not all values in " k " are either rudimentary or Idents")]
                         res
                         )) v))))

;(defn test-err []
;  (id->error ["graph" "app"] :line/by-id (:line/by-id state)))

(defn non-by-id-entries

  "The first part where the part after the / is not \"by-id\""
  [state]
  (filter (fn [kv]
            (let [k (key kv)]
              (and (not (by-id-kw? k))
                   (= 2 (count (s/split (kw->str k) #"/"))))))
          state))

(defn by-id-entries [state]
  (filter (fn [kv]
            (let [k (key kv)]
              (by-id-kw? k)))
          state))

(defn check
      "Checks to see if normalization works as expected. Returns a hash-map you can pprint"
      ([config state]
        (fn []
            (let [{:keys [okay-maps]} config
                  by-id (by-id-entries state)
                  ;_ (println "num id:" (count by-id-entries))
                  names (into #{} (map (comp category-part str key) by-id))
                  non-by-id (non-by-id-entries state)
                  ;_ (println "non id:" non-by-id-entries)
                  categories (into #{} (distinct (map (comp category-part str key) non-by-id)))]
                 (if (not (map? state))
                   {:failed-assumptions ["supposed-normalized-state param must be a map"]}
                   (if (empty? names)
                     {:failed-assumptions ["by-id normalized file required"]}
                     (if (empty? categories)
                       {:failed-assumptions [
                                             "Expected to have categories - top level keywords should have a / in them, and the LHS is the name of the category"]}
                       (let [non-id-tester (partial non-id->error categories)
                             id-tester (partial id->error okay-maps)]
                            {:categories     categories
                             :known-names    names
                             :not-normalized (into #{} (concat
                                                         (mapcat (fn [kv] (non-id-tester (key kv) (val kv))) non-by-id)
                                                         (mapcat (fn [kv] (id-tester (key kv) (val kv))) by-id)))})))))))
      ([state]
        (check nil state)))

;(defn run []
;  (pprint ((check state))))

