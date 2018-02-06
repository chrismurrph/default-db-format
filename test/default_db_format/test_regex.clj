(ns default-db-format.test-regex
  (:require [clojure.test :refer :all]
            [default-db-format.core :as core]))

;; Uppercase and underscores
(deftest upper-under-1
  (is (= true (boolean (re-matches core/upper-under-regex "WELL_THIS_AND_THAT")))))

(deftest upper-under-2
  (is (= true (boolean (re-matches core/upper-under-regex "WELL")))))

;; Uppercase and minuses
(deftest upper-minus-1
  (is (= true (boolean (re-matches core/upper-minus-regex "WELL-THIS-AND-THAT")))))

(deftest upper-minus-2
  (is (= true (boolean (re-matches core/upper-minus-regex "WELL")))))

;; Uppercase and underscores -ive
(deftest not-upper-under
  (is (= false (boolean (re-matches core/upper-under-regex "AmActuallyCamel")))))

(deftest not-upper-minus
  (is (= false (boolean (re-matches core/upper-minus-regex "AmActuallyCamel")))))

;; Anything to a slash then Pascal-case word +ive
;; .+/ is how to match anything up to a slash
(deftest slash-camel
  (is (= true (boolean (re-matches (re-pattern (str core/up-to-slash core/upper-camel-regex)) "anything/SomeCamel")))))

(deftest slash-upper-under
  (is (= true (boolean (re-matches (re-pattern (str core/up-to-slash core/upper-under-regex)) "anything/UPPER_UNDER")))))

(deftest no-slash-camel
  (is (= false (boolean (re-matches (re-pattern (str core/up-to-slash core/upper-under-regex)) "anything/SomeCamel")))))

(deftest no-slash-upper-under
  (is (= false (boolean (re-matches (re-pattern (str core/up-to-slash core/upper-camel-regex)) "anything/UPPER_UNDER")))))

(deftest no-slash-1
  (is (= false (boolean (re-matches (re-pattern (str core/up-to-slash core/upper-camel-regex)) "anythingSomeCamel")))))

(deftest no-slash-2
  (is (= false (boolean (re-matches (re-pattern (str core/up-to-slash core/upper-under-regex)) "anythingUPPER_UNDER")))))

;; Pascal-case word +ive
(deftest camel-1
  (is (= true (boolean (re-matches core/upper-camel-regex "AmUpperCamel")))))

;; Pascal-case word +ive
(deftest camel-2
  (is (= true (boolean (re-matches core/upper-camel-regex "UPPERIsCamel")))))

;; Pascal-case word +ive (perhaps later will use a better regex and this test will fail)
;; My regex would be there must only be one uppercase character. But opinions obviously
;; vary.
(deftest camel-3
  (is (= true (boolean (re-matches core/upper-camel-regex "UPPERISCAMElHARDLY")))))

;; Pascal-case word -ive
(deftest not-camel-1
  (is (= false (boolean (re-matches core/upper-camel-regex "NotCamel_Underscore")))))

(deftest specific-after-slash
  (is (= true
         (boolean (re-matches #"^.+/sumfin" "anything/sumfin")))))

(deftest no-slash
  (is (= false
         (boolean (re-matches #"^.+/sumfin" "anything\\sumfin")))))

(deftest not-specific-after-slash
  (is (= false
         (boolean (re-matches #".+/sumfin" "anything/sumfinx")))))