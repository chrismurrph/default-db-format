(ns ^:figwheel-always cards.cards
  (:require
    [default-db-format.core :refer [show-hud check]]
    [examples.gases :as gases]
    [examples.kanban :as kanban]
    [examples.so-question :as so-question]
    [om.dom :as dom]
    [cljs.test :refer-macros [deftest testing is]]
    [cljs.pprint :refer [pprint]]
    )
  (:require-macros
    [devcards.core :as dc :refer [defcard deftest]]))

(enable-console-print!)

(def irrelevant-keys #{:graph/labels-visible?
                       :graph/hover-pos
                       :graph/args
                       :graph/translators
                       :graph/init
                       :graph/last-mouse-moment})

(defcard card-1
         "Kanban db (expect to show problem because no use of / in names)
         ***"
         (fn [props _] (show-hud @props))
         (check kanban/kanban-norm-state)
         {:inspect-data false})

(defcard card-1-test
         (dc/tests
           (let [res (check kanban/kanban-norm-state)]
             (is (= (-> res :failed-assumption :text) "All top level keys must be namespaced (have a slash)"))
             (is (= (-> res :failed-assumption :problems) '(:lanes :secrets :cards :boards :users)))
             )
           ))

(defcard kanban-fixed-up
         (dc/tests
           "Fixed by now having / in names
           ***"
           (let [res (check kanban/kanban-corrected-norm-state)]
             (is (= (:categories res) #{"kanban"}))
             (is (= (:known-names res) #{"secret" "card" "lane" "user" "board"}))
             (is (= (:not-normalized-ref-entries res) #{}))
             (is (= (:not-normalized-table-entries res) #{}))
             )
           ))

(defcard card-2
         "Real project, lots of problems
         ***"
         (fn [props _] (show-hud @props))
         (check {:excluded-keys irrelevant-keys} gases/from-real-project)
         {:inspect-data false}
         )

(defcard card-2-test
         (dc/tests
           (let [res (check {:excluded-keys irrelevant-keys} gases/from-real-project)]
             (is (= (:categories res) #{"tube", "app", "graph"}))
             (is (= (:known-names res) #{"label" "gas-at-location" "x-gas-details" "tube" "graph-point" "gas-of-system", "button"}))
             (is (= (:not-normalized-ref-entries res)
                      #{{:text "Expect Idents" :problem :graph/drop-info}
                        {:text "Expect Idents" :problem :graph/lines}
                        {:text "Expect Idents" :problem :graph/plumb-line}})
                 ))))

(defcard card-3
         "Real project, fixed component Idents but colour still picked up as not being an Ident
         ***"
         (fn [props _] (show-hud @props))
         (check {:excluded-keys irrelevant-keys} gases/real-project-fixed-component-idents)
         {:inspect-data false}
         )

(defcard card-3-test
         (dc/tests
           (let [res (check {:excluded-keys irrelevant-keys} gases/real-project-fixed-component-idents)]
             (is (= (:categories res) #{"tube", "app", "graph"}))
             (is (= (:known-names res) #{"gas-of-system" "plumb-line" "drop-info" "label" "gas-at-location" 
                                         "x-gas-details" "line" "tube" "graph-point" "button"}))
             (is (= (:not-normalized-ref-entries res) #{}))
             (is (= (:not-normalized-table-entries res) #{[:line/by-id {:colour {:r 255 :g 0 :b 0}}]}))
             )
           ))

(defcard card-4
         "Should show where not normalized in gases db (1 problem plus 3 id problems)
         ***"
         (fn [props _] (show-hud @props))
         (check gases/gas-norm-state)
         {:inspect-data false})

(defcard card-4-test
         (dc/tests
           (let [res (check gases/gas-norm-state)]
             (is (= (:categories res) #{"app", "graph"}))
             (is (= (:known-names res) #{"line" "graph-point" "drop-info"}))
             (is (= (:not-normalized-ref-entries res)
                      #{{:text "Expect Idents" :problem :app/system-gases}}))
             (is (= (:not-normalized-table-entries res)
                    #{[:line/by-id {:intersect {:id 302} :colour {:r 255 :g 0 :b 0}}]
                      [:drop-info/by-id {:x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}]})))))

(defcard card-5
         "Now with one extra problem to card 4
         ***"
         (fn [props _] (show-hud @props))
         (check gases/include-non-id-problem)
         {:inspect-data false})

(defcard card-5-test
         (dc/tests
           (let [res (check gases/include-non-id-problem)]
             (is (= (:categories res) #{"app", "graph"}))
             (is (= (:known-names res) #{"line" "graph-point" "drop-info"}))
             (is (= (:not-normalized-ref-entries res)
                      #{{:text "Expect Idents" :problem :app/system-gases}
                        {:text "Expect Idents" :problem :app/tubes}}))
             (is (= (:not-normalized-table-entries res)
                    #{[:line/by-id {:intersect {:id 302} :colour {:r 255 :g 0 :b 0}}]
                      [:drop-info/by-id {:x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}]})))))

(defcard card-6
         "From SO question (will become a test as shows nothing because checks fine)
         ***"
         (fn [props _] (show-hud @props))
         (check so-question/state)
         {:inspect-data false})

(defcard bad-input-data
         (dc/tests
           (is (= (-> (check nil) :failed-assumption :text) "state param must be a map"))
           (is (= (-> (check {}) :failed-assumption :text) "by-id normalized file required")))
         )

(defcard doing-much-and-all-good
         (dc/tests
           (let [res (check {:excluded-keys   irrelevant-keys
                             :okay-value-maps #{[:r :g :b]}} gases/real-project-fixed-component-idents)]
             (is (= (:categories res) #{"tube" "app" "graph"}))
             (is (= (:known-names res) #{"gas-of-system" "plumb-line" "drop-info" "label" "gas-at-location"
                                         "x-gas-details" "line" "tube" "graph-point" "button"}))
             (is (= (:not-normalized-table-entries res) #{}))
             (is (= (:not-normalized-ref-entries res) #{}))
             )))

(defcard still-all-good-even-with-empties
         (dc/tests
           (let [res (check {:excluded-keys   irrelevant-keys
                             :okay-value-maps #{[:r :g :b]}} gases/real-project-empty-points)]
             (is (= (:categories res) #{"tube" "app" "graph"}))
             (is (= (:known-names res) #{"gas-of-system" "plumb-line" "drop-info" "label" "gas-at-location"
                                         "x-gas-details" "line" "tube" "button"}))
             (is (= (:not-normalized-table-entries res) #{}))
             (is (= (:not-normalized-ref-entries res) #{}))
             )))

;;
;; If one of the tests is failing run this function from the REPL
;;
(defn test-problem []
  (pprint (:not-normalized-table-entries (check {:excluded-keys irrelevant-keys} gases/real-project-fixed-component-idents))))

(defcard test-template
         (dc/tests
           "Test template
           ***"
           (let [res 1]
             (is 1)
             )
           ))
