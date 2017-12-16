(ns default-db-format.general.replace
  (:require [clojure.string :as s]
            [clojure.java.io :as io]))

(defn directory->files [root-dir-path file-name-pattern]
  (filter #(re-matches file-name-pattern (.getName %))
          (file-seq (io/file root-dir-path))))

(defn replace-in-file [search-text replace-text]
  (fn [java-file]
    (spit java-file (s/replace (slurp java-file) search-text replace-text))))

;;
;; https://github.com/fulcrologic/fulcro/blob/2.0/README-fulcro-2.0.adoc
;;
(defn clojure-files []
  (->> (directory->files "src" #".*\.clj|.*\.cljs|.*\.cljc")
       (map (juxt #(.getPath %) identity))))

(defn ->replacers [v]
  (mapv (fn [[from to]]
          (replace-in-file from to)) v))

(def requires-from-to
  [["om.next.impl.parser :as"
    "fulcro.client.impl.parser :as"]
   ["om.next.protocols :as"
    "fulcro.client.impl.protocols :as"]
   ;; Not reversible
   ["om.next.server :as"
    "fulcro.server :as"]
   ["om.dom :as"
    "fulcro.client.dom :as"]
   ["om.tempid :as"
    "fulcro.tempid :as"]
   ["om.util :as"
    "fulcro.util :as"]
   ["om.next :as"
    "fulcro.client.primitives :as"]])

(def exclude-f #(s/ends-with? % "replace.clj"))

;; File replace, to use as a manual 'play' test to verify
;; replacing works as you understand it.
(defn play-test []
  (let [replacements [["cljc.general.om-helpers :refer"
                       "cljc.general.om-bad-helpers :refer"]]
        replacers (->replacers replacements)
        files (->> (clojure-files)
                   (remove #(-> % first exclude-f))
                   (filter #(s/starts-with? (first %) "src/main/accounting/test_data")))]
    (assert (= 1 (count files)))
    (assert (= 1 (count replacers)))
    (doseq [[_ java-file] files]
      (doseq [replacer replacers]
        (replacer java-file)))))

(defn fulcro1->2
  "require replacements for going from Fulcro 1 to Fulcro 2"
  []
  (let [replacers (->replacers requires-from-to)
        files (->> (clojure-files)
                   (remove #(-> % first exclude-f)))]
    (doseq [[_ java-file] files]
      (doseq [replace-all-in-file replacers]
        (replace-all-in-file java-file)))))

;;
;; If you don't crash your JVM (by asking to compile for instance) then using this to
;; reverse out all changes is possible. If crash it just use discard from version control.
;;
(defn fulcro2->1
  "require replacements for going from Fulcro 2 to Fulcro 1"
  []
  (let [to-from (mapv (comp vec reverse) requires-from-to)
        replacers (->replacers to-from)
        files (->> (clojure-files)
                   (remove #(-> % first exclude-f)))]
    (doseq [[_ java-file] files]
      (doseq [replace-all-in-file replacers]
        (replace-all-in-file java-file)))))
