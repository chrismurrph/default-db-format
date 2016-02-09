(ns default-db-format.components
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui BadByIds
  static om/Ident
  (ident [this props]
    [:bads/by-id (:id props)])
  Object
  (render [this]
    (let [{:keys [id bads-map]} (om/props this)]
      (dom/pre nil (apply str bads-map)))))
(def bad-by-ids-component (om/factory BadByIds {:keyfn :id}))

;#{:line/by-id
;  {:intersect {:id 302}, :colour {:r 255, :g 0, :b 0}}
;  :drop-info/by-id
;  {:x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}}
;
;PROPSs 1:
;{:result {:categories #{graph},
;          :known-names #{line drop-info graph-point},
;          :not-normalized #{:line/by-id {:intersect {:id 302}, :colour {:r 255, :g 0, :b 0}}
;                            :drop-info/by-id {:x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}}}}

(defui DisplayDb
       Object
       (render [this]
               (let [props (om/props this)
                     _ (println "PROPSs 1:" props)
                     {:keys [result]} props
                     not-normalized (into {} (:not-normalized result))
                     _ (println "not-normalized: " not-normalized)
                     ]
                 ;(dom/pre nil (apply str not-normalized))
                 (dom/div nil (for [by-id not-normalized
                                    :let [_ (println "by-id:" by-id)
                                          present-lower {:id (first by-id) :bads-map (second by-id)}
                                          _ (println "present lower:" present-lower)]]
                                (bad-by-ids-component present-lower))))))
(def display-db-component (om/factory DisplayDb {:keyfn :id}))
