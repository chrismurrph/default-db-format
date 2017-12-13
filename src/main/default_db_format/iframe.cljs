(ns default-db-format.iframe
  (:require [fulcro.client.primitives :as prim]
            [fulcro.client.dom :as dom]
            [goog.object :as gobj]))

(defn update-frame-content [this child]
  (let [frame-component (gobj/get this "frame-component")]
    (when frame-component
      (js/ReactDOM.render child frame-component))))

(defn start-frame [this]
  (let [frame-body (.-body (.-contentDocument (js/ReactDOM.findDOMNode this)))
        {:keys [child]} (prim/props this)
        e1 (.createElement js/document "div")]
    (when (= 0 (gobj/getValueByKeys frame-body #js ["children" "length"]))
      (.appendChild frame-body e1)
      (gobj/set this "frame-component" e1)
      (update-frame-content this child))))

(prim/defui IFrame
            Object
            (componentDidMount [this] (start-frame this))

            (componentDidUpdate [this _ _]
                                (let [child (:child (prim/props this))]
                                  (update-frame-content this child)))

            (render [this]
                    (dom/iframe
                      (-> (prim/props this)
                          (dissoc :child)
                          (assoc :onLoad #(start-frame this))
                          clj->js))))

(let [factory (prim/factory IFrame)]
  (defn ui-iframe [props child]
    (factory (assoc props :child child))))
