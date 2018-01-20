(ns default-db-format.test-helpers
  (:require [clojure.test :refer :all]
            [default-db-format.helpers :as help]
            [examples.gases :as gases]
            [default-db-format.hof :as hof]))

(deftest is-ident
  (is (= true
         (help/ident-like? [:my/by-id 10]))))

(deftest is-not-ident
  (is (= nil
         (help/ident-like? [:my/by-by-id 10]))))

(deftest table-categories
  (let [by-id-kw? (hof/reveal-f :by-id-kw help/default-edn-config)
        single-id? (hof/reveal-f :one-of-id help/default-edn-config)
        table? (hof/reveal-f :by-id-kw help/default-edn-config)
        state gases/gas-norm-state]
    (is (= 3
           (->> state
                (help/table-entries by-id-kw? single-id? table?)
                (into #{} (map (comp help/category-part str key)))
                count)))))