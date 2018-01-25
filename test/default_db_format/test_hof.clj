(ns default-db-format.test-hof
  (:require [clojure.test :refer :all]
            [default-db-format.hof :as hof]))

(def config-kw-strs #{"by-id"})

(deftest only-name
  (is (= true
         (boolean ((hof/by-id-ending-hof config-kw-strs) :something/by-id)))))

(deftest name-is-prefixed
  (is (= true
         (boolean ((hof/by-id-ending-hof config-kw-strs) :some-ns/pet-by-id)))))

(deftest wont-match
  (is (= false
         (boolean ((hof/by-id-ending-hof config-kw-strs) :some-ns/pet-id)))))
