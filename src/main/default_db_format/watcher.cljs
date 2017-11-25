(ns default-db-format.watcher
  (:require
    [default-db-format.core :as core]
    [default-db-format.helpers :as help]
    [fulcro.client.primitives :as om]))

(def bad-result (atom nil))

;;
;; Intended to be called when Om Next state changes. Stays stuck on the first bad.
;; (Meaning it caches and keeps returning the first bad)
;; Returns enough so that caller can see if is first call to return a bad result.
;; Call subsequent to the first do nothing and furthermore the (cached) result is
;; not used by watch-state caller function.
;;
(defn check!
  ([config state]
   (if @bad-result
     [false @bad-result]
     [true (reset! bad-result (core/check config state))]))
  ([state]
   (check! help/default-config state)))

;;
;; Un-toggle hold on some/none bad state. After call to this function next call to check!
;; will actually do a check.
;;
(defn un-check! []
  (reset! bad-result nil))

;;
;; Changes to state happen frequently. The first problem found here may not end up
;; as a problem because further mutations will fix the state up later. Hence the
;; after-1500ms-forced-render does another check, quite likely on different state.
;;
;; If the timeout is not long enough then the errors will persist in the HUD even
;; although they have cleared from db state. To fix could have a number of times
;; retry for any given error message. Most likely I'll never get round to that - just
;; keep the timeout long enough!
;;
(defn watch-state
  ([config reconciler timeout]
   (let [timeout (or timeout 1500)
         state (om/app-state reconciler)]
     (add-watch state :watcher
                (fn [key atom old-state new-state]
                  (let [[first-time? check-result] (check! config new-state)]
                    (when (and first-time? (not (core/ok? check-result)))
                      (js/setTimeout (fn []
                                       (fulcro.client.util/force-render reconciler)
                                       (un-check!))
                                     timeout)))))))
  ([config reconciler]
    (watch-state config reconciler nil)))
