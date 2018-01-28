(ns default-db-format.test-core
  (:require [clojure.test :refer :all]
            [default-db-format.core :as core]
            [examples.gases :as gases]
            [examples.medical-records :as medical]
            [examples.fulcro-template :as template]
            [default-db-format.dev :as dev]
            [default-db-format.helpers :as help]))

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
    ;(dev/pp res)
    (is (= 1
           (-> res :skip-table-fields count)))))

;;
;; Almost all are /by-id, so expect to get lots (19) of problems
;;
(deftest joins-and-proper-rgb
  (let [res (dissoc (core/check {
                                 :by-id-ending         "/id"
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 19
           (-> res :skip-root-joins count)))
    (is (= 0
           (-> res :skip-table-fields count)))))

(deftest single-bad-join
  (let [res (dissoc (core/check {:by-id-ending         "/id"
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
  (let [res (dissoc (core/check {:by-id-ending         [:id "/by-id"]
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 2
           (-> res :skip-root-joins count)))
    (is (= 0
           (-> res :skip-table-fields count)))
    ))

;;
;; Perversely recognise only :button/id as a table
;;
(deftest joins-and-one-good-id
  (let [res (dissoc (core/check {:by-id-ending         "/id"
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
                                template/initial-state)
                    :version)]
    (is (= true
           (core/ok? res)))
    #_(dev/pp res)))

(deftest link-ident
  (let [res (dissoc (core/check
                      {:skip-link medical/irrelevant-keys}
                      medical/norm-state)
                    :version)]
    (is (= true
           (core/ok? res)))
    #_(dev/pp res)))

;;
;; Field tester is expecting "Guy" to be a map, so can have a look at all its fields.
;; Needs to recognise that it is not and just leave it alone. Are in a link and links
;; never refer to the normalized world, they only contain scalar values.
;; (My assumption, prolly correct)
;;
(def link-map-entry [:current-person/by-id #:person{:first-name "Guy", :last-name "Rundle"}])
(def usual-map-entry [:clinic/by-id {1 #:db{:id nil}}])
(def bad-join-map-entry [:clinic/by-id {1 #:db{:id {:a "Join s/be Ident/s"}}}])
(def not-a-table-liar [:fulcro.inspect.core/app-id 'default-db-format.baby-sharks/AdultRoot])

(defn create-field-tester [config]
  (let [{:keys [acceptable-map-value acceptable-vector-value ignore-skip-field-joins] :as init-map}
        (help/config->init (merge help/default-edn-config config))
        ident-like? (help/-ident-like-hof? init-map)
        conformance-predicates
        {:ident-like?               ident-like?
         :acceptable-table-value-f? (constantly false)}
        ]
    (core/field-join->error-hof
      conformance-predicates
      acceptable-map-value
      acceptable-vector-value
      ignore-skip-field-joins)))

;;
;; Without `(map? (second m))` (just search in code) check calling check where a
;; link is referred to will output this error message:
;; UnsupportedOperationException nth not supported on this type: Character  clojure.lang.RT.nthFrom (RT.java:962)
;; Here test only needs to make sure that there's no failure.
;;
(deftest dont-investigate-link
  (let [field-tester (create-field-tester {:skip-link medical/irrelevant-keys})]
    (is (= nil
           (field-tester link-map-entry)))))

(deftest no-link-to-not-investigate
  (let [field-tester (create-field-tester {:skip-link medical/irrelevant-keys})]
    (is (= nil
           (field-tester usual-map-entry)))))

(deftest bad-join-output
  (let [field-tester (create-field-tester {:skip-link medical/irrelevant-keys})]
    (is (= #:clinic{:by-id #:db{:id {:a "Join s/be Ident/s"}}}
           (field-tester bad-join-map-entry)))))

(deftest matched-but-not-a-table
  (is (= :fulcro.inspect.core/app-id
         (-> not-a-table-liar core/table-structure->error :problem))))