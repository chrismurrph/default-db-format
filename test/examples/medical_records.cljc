(ns examples.medical-records)

(def irrelevant-keys #{:app/route
                       :om.next/tables})

(comment
  :acceptable-table-value-fn? (fn [v] (= keyword? v)))

(def norm-state
  {:code/list      [[:code/by-id 17592186045430]],
   :code-type/by-id
                   {17592186045423
                    {:db/id     17592186045423,
                     :code/name :codes/allergy,
                     :code/type "allergy"},
                    17592186045424
                    {:db/id     17592186045424,
                     :code/name :codes/diagnosis,
                     :code/type "diagnosis"},
                    17592186045421
                    {:db/id     17592186045421,
                     :code/name :codes/history,
                     :code/type "history"},
                    17592186045422
                    {:db/id     17592186045422,
                     :code/name :codes/medicine,
                     :code/type "medicine"},
                    17592186045425
                    {:db/id     17592186045425,
                     :code/name :codes/locale,
                     :code/type "locale"}},
   :app/route      [:new-clinic '_],
   :om.next/tables #{:code-type/by-id},
   :app/args       nil,
   :code-type/list
                   [[:code-type/by-id 17592186045423]
                    [:code-type/by-id 17592186045424]
                    [:code-type/by-id 17592186045421]
                    [:code-type/by-id 17592186045422]
                    [:code-type/by-id 17592186045425]],
   :code/by-id
                   {17592186045430
                    {:db/id           17592186045430,
                     :entity/deleted? false,
                     :code/name       "Taha",
                     :code/type       [:code-type/by-id 17592186045423]}},
   :clinic/by-id   {Double/NaN {:db/id nil}}})

