(ns default-db-format.test-helpers
  (:require [clojure.test :refer :all]
            [default-db-format.helpers :as help]
            [examples.gases :as gases]))

(defn probe [x]
  (println x)
  x)

(deftest is-ident
  (is (= true
         (help/ident-like? [:my/by-id 10]))))

(deftest is-not-ident
  (is (= nil
         (help/ident-like? [:my/by-by-id 10]))))

(deftest table-categories
  (let [by-id-kw? (-> help/default-config :by-id-kw help/setify help/by-id-kw-hof)
        state gases/gas-norm-state]
    (is (= 3
           (->> state
                (help/table-entries by-id-kw?)
                (into #{} (map (comp help/category-part str key)))
                count)))))