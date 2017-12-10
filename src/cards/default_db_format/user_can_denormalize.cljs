(ns default-db-format.user-can-denormalize
  (:require [fulcro.client.primitives :as prim]
            [fulcro.client.dom :as dom]
            [fulcro.client.cards :refer [defcard-fulcro fulcro-application]]
            [general.card-helpers :as card-helpers]
            [fulcro.client.primitives :as prim :refer [defui InitialAppState initial-state]]))

(prim/defsc Baby
            [this props _]
            {:ident [:baby/by-id :db/id]
             :query [:db/id :baby/first-name]}
            (dom/div nil "Blab blab"))

(prim/defsc Adult
            [this {:adult/keys [first-name babies]} _]
            {:initial-state
                    (fn [{:keys [babies-in]}]
                      {:db/id 1 :adult/first-name "Mama Shark" :adult/babies babies-in})
             :ident [:adult/by-id :db/id]
             :query [:db/id :adult/first-name {:adult/babies (prim/get-query Baby)}]}
            (dom/div nil (str "Good afternoon, my name is " first-name " with " (count babies))))

(def AdultRoot (card-helpers/make-root Adult ::adult))

(def initial-babies [{:db/id 2 :baby/first-name "Baby Shark 1"}
                     {:db/id 3 :baby/first-name "Baby Shark 2"}])
(defcard-fulcro say-hello
                AdultRoot
                (card-helpers/init-state-atom AdultRoot
                                              {:babies-in initial-babies})
                {:inspect-data true})
