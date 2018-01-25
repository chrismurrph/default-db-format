(ns default-db-format.test-core
  (:require [clojure.test :refer :all]
            [default-db-format.core :as core]
            [examples.gases :as gases]
            [examples.fulcro-template :as template]
            [default-db-format.dev :as dev]))

(def expected-gas-issues
  {:categories      #{"graph" "app"},
   :known-names     #{"drop-info" "line" "graph-point"}
   :skip-root-joins #{{:text    "Expect Idents",
                       :problem :app/system-gases
                       :problem-value
                                [{:id 200, :short-name "Methane"}
                                 {:id 201, :short-name "Oxygen"}
                                 {:id 202, :short-name "Carbon Monoxide"}
                                 {:id 203, :short-name "Carbon Dioxide"}]}}
   ;;
   ;; Hmm - its a set of tuples rather than a map. Fix when going for perfection...
   ;;
   :skip-table-fields
                    #{[:drop-info/by-id {:x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}]
                      [:line/by-id {:intersect {:id 302}, :colour {:r 255, :g 0, :b 0}}]}
   })

(deftest gas-problems
  (let [res (dissoc (core/check gases/gas-norm-state) :version)]
    (is (= expected-gas-issues
           res))))

(deftest gas-problem-no-id
  (let [res (dissoc (core/check gases/include-non-id-problem) :version)]
    (is (= 2
           (-> res :skip-root-joins count)))))

(deftest joins-and-bad-rgb
  (let [res (dissoc (core/check {:skip-field-join      [:graph/init :graph/translators]
                                 :acceptable-map-value [:r :g :b]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 1
           (-> res :skip-table-fields count)))))

;;
;; Almost all are /by-id, so expect to get lots (19) of problems
;;
(deftest joins-and-proper-rgb
  (let [res (dissoc (core/check {
                                 :by-id-ns-name        "id"
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 19
           (-> res :skip-root-joins count)))
    ;; temp
    #_(is (= 0
           (-> res :skip-table-fields count)))))

(deftest joins-and-proper-rgb-ignoring
  (let [res (dissoc (core/check {
                                 :by-id-ns-name        "id"
                                 ;; Will be ignored
                                 :by-id-ending         "id"
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 19
           (-> res :skip-root-joins count)))
    (is (= 0
           (-> res :skip-table-fields count)))))

(deftest single-bad-join
  (let [res (dissoc (core/check {:by-id-ending         "id"
                                 :skip-link            :graph/translators
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 20
           (-> res :skip-root-joins count)))))

;;
;; button/id is a table name that won't be supported
;; The table :button/id will have to be thought of as a join, and won't have idents in it
;; The top level join :app/buttons will of course have idents, but as they are [:button/id ?]
;; rather than [:button/by-id ?], they won't be recognised as idents.
;;
(deftest joins-and-missing-id
  (let [res (dissoc (core/check {:skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 2
           (-> res :skip-root-joins count)))
    (is (= 0
           (-> res :skip-table-fields count)))
    #_(dev/pp res)))

;;
;; Shows that the :id is ignored. The tool will tell about this problem.
;;
(deftest joins-and-bad-id
  (let [res (dissoc (core/check {:by-id-ending         [:id "by-id"]
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 2
           (-> res :skip-root-joins count)))
    (is (= 0
           (-> res :skip-table-fields count)))
    ;(dev/pp res)
    ))

;;
;; Perversely recognise only :button/id as a table
;;
(deftest joins-and-one-good-id
  (let [res (dissoc (core/check {:by-id-ending         "id"
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 0
           (-> res :skip-table-fields count)))
    (is (= 19
           (-> res :skip-root-joins count)))
    #_(dev/pp res)))

(def expected-template-1-res
  {:categories      #{"ui" "root" "fulcro.inspect.core"},
   :known-names     #{"fulcro.ui.bootstrap3.modal" "fulcro.client.routing.routers" "user"},
   :skip-root-joins #{{:text          "Expect Idents", :problem :root/modals
                       :problem-value {:welcome-modal [:fulcro.ui.bootstrap3.modal/by-id :welcome]}}},
   :skip-table-fields
                    #{[:fulcro.client.routing.routers/by-id #:fulcro.client.routing{:current-route [:login :page]}]}
   })

(deftest fulcro-template-1
  (let [res (dissoc (core/check {}
                                template/initial-state) :version)]
    (is (= expected-template-1-res
           res))
    ;(dev/pp res)
    ))

(def expected-template-2-res
  {:categories        #{"ui" "root" "fulcro.inspect.core"},
   :known-names       #{"fulcro.ui.bootstrap3.modal" "fulcro.client.routing.routers" "user" "login"},
   :skip-root-joins   #{{:text          "Expect Idents", :problem :root/modals
                         :problem-value {:welcome-modal [:fulcro.ui.bootstrap3.modal/by-id :welcome]}}},
   :skip-table-fields #{}})

(deftest fulcro-template-2
  (let [res (dissoc (core/check {:routing-table [:login]}
                                template/initial-state) :version)]
    (is (= expected-template-2-res
           res))
    #_(dev/pp res)))

(deftest fulcro-template-3
  (let [res (dissoc (core/check {:routing-table [:login]
                                 :skip-link     :root/modals}
                                template/initial-state) :version)]
    (is (= true
           (core/ok? res)))
    #_(dev/pp res)))