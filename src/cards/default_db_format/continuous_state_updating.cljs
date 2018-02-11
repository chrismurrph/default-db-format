(ns default-db-format.continuous-state-updating
  (:require [fulcro.client.cards :refer [defcard-fulcro fulcro-application]]
            [fulcro.client.primitives :as prim :refer [defui defsc InitialAppState initial-state]]
            [fulcro.client.mutations :refer [defmutation]]
            [fulcro.client.dom :as dom]
            [default-db-format.dev :as dev]))

(declare tell-time-fulcro-app)

(defn get-reconciler []
  (-> tell-time-fulcro-app deref :reconciler))

(defn get-state []
  @(prim/app-state (get-reconciler)))

(def dump get-state)

(defmutation time-changed
             [{:keys [time]}]
             (action [{:keys [state]}]
                     ;; Putting it in a map because string is a simple scalar value that
                     ;; is ignored. However even that won't work as all keys that have not
                     ;; been found to be tables are assumed to be joins.
                     (swap! state assoc :ui/current-time {:time time})))

(defn at-timeout []
  (let [msg (str (js/Date.))]
    (prim/transact! (get-reconciler) `[(time-changed {:time ~msg})])))

(defsc Root [this {:keys [:a ui/current-time]}]
       {:query         [:a :ui/current-time]
        :initial-state {:a "some state"}}
       (dom/div nil
                (dom/p nil (str "Current time: " current-time))
                ))

(defcard-fulcro tell-time
                Root
                (dev/init-state-atom Root
                                     {})
                {:inspect-data true
                 :fulcro { :started-callback (fn [app] (js/setInterval at-timeout 5000))}})