(ns default-db-format.test-helpers
  (:require [clojure.test :refer :all]
            [default-db-format.helpers :as help]
            [examples.gases :as gases]))

(deftest is-ident
  (is (= true
         (help/ident-like? [:my/by-id 10]))))

(deftest is-not-ident
  (is (= nil
         (help/ident-like? [:my/by-by-id 10]))))

(deftest table-categories
  (let [by-id-kw? (-> help/default-config :by-id-kw help/-setify help/by-id-kw-hof)
        single-id? (-> help/default-config :one-of-id help/-setify help/map-entry-single-id-hof)
        table? (-> help/default-config :by-id-kw help/-setify help/table-hof)
        state gases/gas-norm-state]
    (is (= 3
           (->> state
                (help/table-entries by-id-kw? single-id? table?)
                (into #{} (map (comp help/category-part str key)))
                count)))))