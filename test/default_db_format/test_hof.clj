(ns default-db-format.test-hof
  (:require [clojure.test :refer :all]
            [default-db-format.hof :as hof]))

(def config-kw-strs #{"/by-id"})

(deftest only-name
  (is (= true
         (boolean ((hof/table-ending-hof config-kw-strs) :something/by-id)))))

(deftest name-is-prefixed-1
  (is (= false
         (boolean ((hof/table-ending-hof config-kw-strs) :some-ns/pet-by-id)))))

(deftest name-is-prefixed-2
  (is (= true
         (boolean ((hof/table-ending-hof #{"by-id"}) :some-ns/pet-by-id)))))

(deftest wont-match
  (is (= false
         (boolean ((hof/table-ending-hof config-kw-strs) :some-ns/pet-id)))))
