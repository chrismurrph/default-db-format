(ns examples.gases
  (:require
    #?(:cljs [cljs.core.async :refer [chan]])
    #?(:clj [clojure.core.async :refer [chan]])))

(def gas-norm-state
  {:graph/drop-info [:drop-info/by-id 10200],
   :graph/lines
                    [[:line/by-id 100]
                     [:line/by-id 101]
                     [:line/by-id 102]
                     [:line/by-id 103]],
   :app/system-gases
                    [{:id 200, :short-name "Methane"}
                     {:id 201, :short-name "Oxygen"}
                     {:id 202, :short-name "Carbon Monoxide"}
                     {:id 203, :short-name "Carbon Dioxide"}],
   :graph/points
                    [[:graph-point/by-id 2000]
                     [:graph-point/by-id 2001]
                     [:graph-point/by-id 2002]
                     [:graph-point/by-id 2003]
                     [:graph-point/by-id 2004]
                     [:graph-point/by-id 2005]
                     [:graph-point/by-id 2006]
                     [:graph-point/by-id 2007]
                     [:graph-point/by-id 2008]
                     [:graph-point/by-id 2009]
                     [:graph-point/by-id 2010]
                     [:graph-point/by-id 2011]],
   :line/by-id
                    {100
                     {:id        100,
                      :intersect {:id 300},
                      :name      "Methane at 1",
                      :units     "%",
                      :colour    {:r 255, :g 0, :b 255},
                      :graph/points
                                 [[:graph-point/by-id 2000]
                                  [:graph-point/by-id 2001]
                                  [:graph-point/by-id 2002]]},
                     101
                     {:id        101,
                      :intersect {:id 301},
                      :name      "Oxygen at 1",
                      :units     "%",
                      :colour    {:r 0, :g 102, :b 0},
                      :graph/points
                                 [[:graph-point/by-id 2003]
                                  [:graph-point/by-id 2004]
                                  [:graph-point/by-id 2005]]},
                     102
                     {:id        102,
                      :intersect {:id 303},
                      :name      "Carbon Dioxide at 1",
                      :units     "%",
                      :colour    {:r 0, :g 51, :b 102},
                      :graph/points
                                 [[:graph-point/by-id 2006]
                                  [:graph-point/by-id 2007]
                                  [:graph-point/by-id 2008]]},
                     103
                     {:id        103,
                      :intersect {:id 302},
                      :name      "Carbon Monoxide at 1",
                      :units     "ppm",
                      :colour    {:r 255, :g 0, :b 0},
                      :graph/points
                                 [[:graph-point/by-id 2009]
                                  [:graph-point/by-id 2010]
                                  [:graph-point/by-id 2011]]}},
   :drop-info/by-id
                    {10200
                     {:id            10200,
                      :x             50,
                      :graph/lines
                                     [[:line/by-id 100]
                                      [:line/by-id 101]
                                      [:line/by-id 102]
                                      [:line/by-id 103]],
                      :x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}},
   :graph-point/by-id
                    {2000 {:id 2000, :x 10, :y 23},
                     2001 {:id 2001, :x 11, :y 24},
                     2002 {:id 2002, :x 12, :y 25},
                     2003 {:id 2003, :x 13, :y 26},
                     2004 {:id 2004, :x 14, :y 27},
                     2005 {:id 2005, :x 15, :y 28},
                     2006 {:id 2006, :x 16, :y 29},
                     2007 {:id 2007, :x 17, :y 30},
                     2008 {:id 2008, :x 18, :y 31},
                     2009 {:id 2009, :x 19, :y 32},
                     2010 {:id 2010, :x 20, :y 33},
                     2011 {:id 2011, :x 21, :y 34}}})

(def non-id-problem
  {:app/tubes
   [{:id 1000, :tube-num "Invercargill"}
    {:id 1001, :tube-num "Dunedin"}]})

(def include-non-id-problem
  (merge gas-norm-state non-id-problem))

(def from-real-project
  {:tube/gases
                            [[:gas-at-location/by-id 500]
                             [:gas-at-location/by-id 501]
                             [:gas-at-location/by-id 502]
                             [:gas-at-location/by-id 503]
                             [:gas-at-location/by-id 504]
                             [:gas-at-location/by-id 505]
                             [:gas-at-location/by-id 506]
                             [:gas-at-location/by-id 507]
                             [:gas-at-location/by-id 508]
                             [:gas-at-location/by-id 509]
                             [:gas-at-location/by-id 510]
                             [:gas-at-location/by-id 511]
                             [:gas-at-location/by-id 512]
                             [:gas-at-location/by-id 513]
                             [:gas-at-location/by-id 514]
                             [:gas-at-location/by-id 515]
                             [:gas-at-location/by-id 516]
                             [:gas-at-location/by-id 517]
                             [:gas-at-location/by-id 518]
                             [:gas-at-location/by-id 519]
                             [:gas-at-location/by-id 520]
                             [:gas-at-location/by-id 521]
                             [:gas-at-location/by-id 522]
                             [:gas-at-location/by-id 523]
                             [:gas-at-location/by-id 524]
                             [:gas-at-location/by-id 525]
                             [:gas-at-location/by-id 526]
                             [:gas-at-location/by-id 527]
                             [:gas-at-location/by-id 528]
                             [:gas-at-location/by-id 529]
                             [:gas-at-location/by-id 530]
                             [:gas-at-location/by-id 531]
                             [:gas-at-location/by-id 532]
                             [:gas-at-location/by-id 533]
                             [:gas-at-location/by-id 534]
                             [:gas-at-location/by-id 535]
                             [:gas-at-location/by-id 536]
                             [:gas-at-location/by-id 537]
                             [:gas-at-location/by-id 538]
                             [:gas-at-location/by-id 539]],
   :app/buttons
                            [[:button/by-id 1]
                             [:button/by-id 2]
                             [:button/by-id 3]
                             [:button/by-id 4]
                             [:button/by-id 5]
                             [:button/by-id 6]],
   :app/tubes
                            [[:tube/by-id 1000]
                             [:tube/by-id 1001]
                             [:tube/by-id 1002]
                             [:tube/by-id 1003]
                             [:tube/by-id 1004]
                             [:tube/by-id 1005]
                             [:tube/by-id 1006]
                             [:tube/by-id 1007]
                             [:tube/by-id 1008]
                             [:tube/by-id 1009]],
   :app/gases
                            [[:gas-of-system/by-id 150]
                             [:gas-of-system/by-id 151]
                             [:gas-of-system/by-id 152]
                             [:gas-of-system/by-id 153]],
   :graph/labels-visible?   false,
   :graph/hover-pos         nil,
   :gas-at-location/by-id
                            {512
                             {:id         512,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1003]},
                             513
                             {:id         513,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1003],
                              :selected   true},
                             514
                             {:id         514,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1003]},
                             515
                             {:id         515,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1003]},
                             516
                             {:id         516,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1004]},
                             517
                             {:id         517,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1004]},
                             518
                             {:id         518,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1004]},
                             519
                             {:id         519,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1004]},
                             520
                             {:id         520,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1005]},
                             521
                             {:id         521,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1005]},
                             522
                             {:id         522,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1005]},
                             523
                             {:id         523,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1005]},
                             524
                             {:id         524,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1006]},
                             525
                             {:id         525,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1006]},
                             526
                             {:id         526,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1006]},
                             527
                             {:id         527,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1006]},
                             528
                             {:id         528,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1007]},
                             529
                             {:id         529,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1007]},
                             530
                             {:id         530,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1007]},
                             531
                             {:id         531,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1007]},
                             500
                             {:id         500,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1000],
                              :selected   true},
                             532
                             {:id         532,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1008]},
                             501
                             {:id         501,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1000]},
                             533
                             {:id         533,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1008]},
                             502
                             {:id         502,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1000]},
                             534
                             {:id         534,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1008]},
                             503
                             {:id         503,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1000]},
                             535
                             {:id         535,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1008]},
                             504
                             {:id         504,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1001]},
                             536
                             {:id         536,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1009]},
                             505
                             {:id         505,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1001]},
                             537
                             {:id         537,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1009]},
                             506
                             {:id         506,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1001]},
                             538
                             {:id         538,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1009]},
                             507
                             {:id         507,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1001]},
                             539
                             {:id         539,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1009]},
                             508
                             {:id         508,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1002]},
                             509
                             {:id         509,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1002]},
                             510
                             {:id         510,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1002]},
                             511
                             {:id         511,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1002],
                              :selected   true}},
   :x-gas-details/by-id
                            {10100
                             {:id               10100,
                              :name             "Carbon Dioxide at 1",
                              :proportional-y   146.33422462612975,
                              :proportional-val 0.19667279430464207},
                             10101
                             {:id               10101,
                              :name             "Carbon Monoxide at 1",
                              :proportional-y   131.68775824757364,
                              :proportional-val 11.337551649514731},
                             10102
                             {:id               10102,
                              :name             "Oxygen at 1",
                              :proportional-y   161.68775824757364,
                              :proportional-val 10.337551649514731}},
   :graph/x-gas-details
                            [[:x-gas-details/by-id 10100]
                             [:x-gas-details/by-id 10101]
                             [:x-gas-details/by-id 10102]],
   :graph/args              nil,
   :graph/translators       {:horiz nil, :vert nil, :point nil},
   :graph/drop-info
                            {:id            10200,
                             :x             50,
                             :lines         [{:id 100} {:id 101} {:id 102} {:id 103}],
                             :current-label {:id 10000},
                             :x-gas-details [{:id 10100} {:id 10101} {:id 10102}]},
   :graph/labels            [[:label/by-id 10000]],
   :label/by-id
                            {10000 {:id 10000, :name "Carbon Monoxide at 1", :dec-places 1}},
   :button/by-id
                            {1 {:id 1, :name "Map", :description "Mine plan", :showing true},
                             2
                               {:id          2,
                                :name        "Trending",
                                :description "Live data, Trending",
                                :showing     true},
                             3
                               {:id          3,
                                :name        "Thresholds",
                                :description "Alarm Thresholds",
                                :showing     true},
                             4
                               {:id          4,
                                :name        "Reports",
                                :description "Event Reports",
                                :showing     true},
                             5
                               {:id          5,
                                :name        "Automatic",
                                :description "Automatic Tube Bundle",
                                :showing     true},
                             6 {:id 6, :name "Logs", :description "Warning Log", :showing true}},
   :gas-of-system/by-id
                            {150 {:id 150, :long-name "Methane", :short-name "CH₄"},
                             151 {:id 151, :long-name "Oxygen", :short-name "O₂"},
                             152 {:id 152, :long-name "Carbon Monoxide", :short-name "CO"},
                             153 {:id 153, :long-name "Carbon Dioxide", :short-name "CO₂"}},
   :graph-point/by-id
                            {2000 {:id 2000, :x 20, :y 23},
                             2001 {:id 2001, :x 21, :y 24},
                             2002 {:id 2002, :x 22, :y 25},
                             2003 {:id 2003, :x 33, :y 26},
                             2004 {:id 2004, :x 34, :y 27},
                             2005 {:id 2005, :x 35, :y 28},
                             2006 {:id 2006, :x 46, :y 29},
                             2007 {:id 2007, :x 47, :y 30},
                             2008 {:id 2008, :x 48, :y 31},
                             2009 {:id 2009, :x 59, :y 32},
                             2010 {:id 2010, :x 50, :y 33},
                             2011 {:id 2011, :x 51, :y 34}},
   :graph/init              {:height 250, :width 640},
   :graph/lines
                            [{:id           100,
                              :name         "Methane at 1",
                              :units        "%",
                              :colour       {:r 255, :g 0, :b 255},
                              :intersect    {:id 500},
                              :graph/points [{:id 2000} {:id 2001} {:id 2002}]}
                             {:id           101,
                              :name         "Oxygen at 1",
                              :units        "%",
                              :colour       {:r 0, :g 102, :b 0},
                              :intersect    {:id 501},
                              :graph/points [{:id 2003} {:id 2004} {:id 2005}]}
                             {:id           102,
                              :name         "Carbon Dioxide at 1",
                              :units        "%",
                              :colour       {:r 0, :g 51, :b 102},
                              :intersect    {:id 503},
                              :graph/points [{:id 2006} {:id 2007} {:id 2008}]}
                             {:id           103,
                              :name         "Carbon Monoxide at 1",
                              :units        "ppm",
                              :colour       {:r 255, :g 0, :b 0},
                              :intersect    {:id 502},
                              :graph/points [{:id 2009} {:id 2010} {:id 2011}]}],
   :graph/last-mouse-moment nil,
   :app/selected-button     [:button/by-id 3],
   :graph/plumb-line
                            {:id              10201,
                             :height          30,
                             :visible?        true,
                             :x-position      10,
                             :in-sticky-time? false},
   :graph/points
                            [[:graph-point/by-id 2000]
                             [:graph-point/by-id 2001]
                             [:graph-point/by-id 2002]
                             [:graph-point/by-id 2003]
                             [:graph-point/by-id 2004]
                             [:graph-point/by-id 2005]
                             [:graph-point/by-id 2006]
                             [:graph-point/by-id 2007]
                             [:graph-point/by-id 2008]
                             [:graph-point/by-id 2009]
                             [:graph-point/by-id 2010]
                             [:graph-point/by-id 2011]],
   :tube/by-id
                            {1000
                             {:id       1000,
                              :tube-num 1,
                              :tube/gases
                                        [[:gas-at-location/by-id 500]
                                         [:gas-at-location/by-id 501]
                                         [:gas-at-location/by-id 502]
                                         [:gas-at-location/by-id 503]]},
                             1001
                             {:id       1001,
                              :tube-num 2,
                              :tube/gases
                                        [[:gas-at-location/by-id 504]
                                         [:gas-at-location/by-id 505]
                                         [:gas-at-location/by-id 506]
                                         [:gas-at-location/by-id 507]]},
                             1002
                             {:id       1002,
                              :tube-num 3,
                              :tube/gases
                                        [[:gas-at-location/by-id 508]
                                         [:gas-at-location/by-id 509]
                                         [:gas-at-location/by-id 510]
                                         [:gas-at-location/by-id 511]]},
                             1003
                             {:id       1003,
                              :tube-num 4,
                              :tube/gases
                                        [[:gas-at-location/by-id 512]
                                         [:gas-at-location/by-id 513]
                                         [:gas-at-location/by-id 514]
                                         [:gas-at-location/by-id 515]]},
                             1004
                             {:id       1004,
                              :tube-num 5,
                              :tube/gases
                                        [[:gas-at-location/by-id 516]
                                         [:gas-at-location/by-id 517]
                                         [:gas-at-location/by-id 518]
                                         [:gas-at-location/by-id 519]]},
                             1005
                             {:id       1005,
                              :tube-num 6,
                              :tube/gases
                                        [[:gas-at-location/by-id 520]
                                         [:gas-at-location/by-id 521]
                                         [:gas-at-location/by-id 522]
                                         [:gas-at-location/by-id 523]]},
                             1006
                             {:id       1006,
                              :tube-num 7,
                              :tube/gases
                                        [[:gas-at-location/by-id 524]
                                         [:gas-at-location/by-id 525]
                                         [:gas-at-location/by-id 526]
                                         [:gas-at-location/by-id 527]]},
                             1007
                             {:id       1007,
                              :tube-num 8,
                              :tube/gases
                                        [[:gas-at-location/by-id 528]
                                         [:gas-at-location/by-id 529]
                                         [:gas-at-location/by-id 530]
                                         [:gas-at-location/by-id 531]]},
                             1008
                             {:id       1008,
                              :tube-num 9,
                              :tube/gases
                                        [[:gas-at-location/by-id 532]
                                         [:gas-at-location/by-id 533]
                                         [:gas-at-location/by-id 534]
                                         [:gas-at-location/by-id 535]]},
                             1009
                             {:id       1009,
                              :tube-num 10,
                              :tube/gases
                                        [[:gas-at-location/by-id 536]
                                         [:gas-at-location/by-id 537]
                                         [:gas-at-location/by-id 538]
                                         [:gas-at-location/by-id 539]]}}})

(def real-project-fixed-component-idents
  {:tube/gases
                            [[:gas-at-location/by-id 500]
                             [:gas-at-location/by-id 501]
                             [:gas-at-location/by-id 502]
                             [:gas-at-location/by-id 503]
                             [:gas-at-location/by-id 504]
                             [:gas-at-location/by-id 505]
                             [:gas-at-location/by-id 506]
                             [:gas-at-location/by-id 507]
                             [:gas-at-location/by-id 508]
                             [:gas-at-location/by-id 509]
                             [:gas-at-location/by-id 510]
                             [:gas-at-location/by-id 511]
                             [:gas-at-location/by-id 512]
                             [:gas-at-location/by-id 513]
                             [:gas-at-location/by-id 514]
                             [:gas-at-location/by-id 515]
                             [:gas-at-location/by-id 516]
                             [:gas-at-location/by-id 517]
                             [:gas-at-location/by-id 518]
                             [:gas-at-location/by-id 519]
                             [:gas-at-location/by-id 520]
                             [:gas-at-location/by-id 521]
                             [:gas-at-location/by-id 522]
                             [:gas-at-location/by-id 523]
                             [:gas-at-location/by-id 524]
                             [:gas-at-location/by-id 525]
                             [:gas-at-location/by-id 526]
                             [:gas-at-location/by-id 527]
                             [:gas-at-location/by-id 528]
                             [:gas-at-location/by-id 529]
                             [:gas-at-location/by-id 530]
                             [:gas-at-location/by-id 531]
                             [:gas-at-location/by-id 532]
                             [:gas-at-location/by-id 533]
                             [:gas-at-location/by-id 534]
                             [:gas-at-location/by-id 535]
                             [:gas-at-location/by-id 536]
                             [:gas-at-location/by-id 537]
                             [:gas-at-location/by-id 538]
                             [:gas-at-location/by-id 539]],
   :app/buttons
                            [[:button/id 1]
                             [:button/id 2]
                             [:button/id 3]
                             [:button/id 4]
                             [:button/id 5]
                             [:button/id 6]],
   :app/tubes
                            [[:tube/by-id 1000]
                             [:tube/by-id 1001]
                             [:tube/by-id 1002]
                             [:tube/by-id 1003]
                             [:tube/by-id 1004]
                             [:tube/by-id 1005]
                             [:tube/by-id 1006]
                             [:tube/by-id 1007]
                             [:tube/by-id 1008]
                             [:tube/by-id 1009]],
   :app/gases
                            [[:gas-of-system/by-id 150]
                             [:gas-of-system/by-id 151]
                             [:gas-of-system/by-id 152]
                             [:gas-of-system/by-id 153]],
   :graph/labels-visible?   false,
   :graph/hover-pos         nil,
   :gas-at-location/by-id
                            {512
                             {:id         512,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1003]},
                             513
                             {:id         513,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1003],
                              :selected   true},
                             514
                             {:id         514,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1003]},
                             515
                             {:id         515,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1003]},
                             516
                             {:id         516,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1004]},
                             517
                             {:id         517,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1004]},
                             518
                             {:id         518,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1004]},
                             519
                             {:id         519,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1004]},
                             520
                             {:id         520,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1005]},
                             521
                             {:id         521,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1005]},
                             522
                             {:id         522,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1005]},
                             523
                             {:id         523,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1005]},
                             524
                             {:id         524,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1006]},
                             525
                             {:id         525,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1006]},
                             526
                             {:id         526,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1006]},
                             527
                             {:id         527,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1006]},
                             528
                             {:id         528,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1007]},
                             529
                             {:id         529,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1007]},
                             530
                             {:id         530,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1007]},
                             531
                             {:id         531,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1007]},
                             500
                             {:id         500,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1000],
                              :selected   true},
                             532
                             {:id         532,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1008]},
                             501
                             {:id         501,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1000]},
                             533
                             {:id         533,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1008]},
                             502
                             {:id         502,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1000]},
                             534
                             {:id         534,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1008]},
                             503
                             {:id         503,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1000]},
                             535
                             {:id         535,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1008]},
                             504
                             {:id         504,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1001]},
                             536
                             {:id         536,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1009]},
                             505
                             {:id         505,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1001]},
                             537
                             {:id         537,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1009]},
                             506
                             {:id         506,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1001]},
                             538
                             {:id         538,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1009]},
                             507
                             {:id         507,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1001]},
                             539
                             {:id         539,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1009]},
                             508
                             {:id         508,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1002]},
                             509
                             {:id         509,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1002]},
                             510
                             {:id         510,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1002]},
                             511
                             {:id         511,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1002],
                              :selected   true}},
   :x-gas-details/by-id
                            {10100
                             {:id               10100,
                              :name             "Carbon Dioxide at 1",
                              :proportional-y   146.33422462612975,
                              :proportional-val 0.19667279430464207},
                             10101
                             {:id               10101,
                              :name             "Carbon Monoxide at 1",
                              :proportional-y   131.68775824757364,
                              :proportional-val 11.337551649514731},
                             10102
                             {:id               10102,
                              :name             "Oxygen at 1",
                              :proportional-y   161.68775824757364,
                              :proportional-val 10.337551649514731}},
   :graph/x-gas-details
                            [[:x-gas-details/by-id 10100]
                             [:x-gas-details/by-id 10101]
                             [:x-gas-details/by-id 10102]],
   :graph/args              nil,
   :graph/translators       {:horiz nil, :vert nil, :point nil},
   :graph/drop-info         [:drop-info/by-id 10200],
   :line/by-id
                            {100
                             {:id        100,
                              :name      "Methane at 1",
                              :units     "%",
                              :colour    {:r 255, :g 0, :b 255},
                              :intersect [:gas-at-location/by-id 500],
                              :graph/points
                                         [[:graph-point/by-id 2000]
                                          [:graph-point/by-id 2001]
                                          [:graph-point/by-id 2002]]},
                             101
                             {:id        101,
                              :name      "Oxygen at 1",
                              :units     "%",
                              :colour    {:r 0, :g 102, :b 0},
                              :intersect [:gas-at-location/by-id 501],
                              :graph/points
                                         [[:graph-point/by-id 2003]
                                          [:graph-point/by-id 2004]
                                          [:graph-point/by-id 2005]]},
                             102
                             {:id        102,
                              :name      "Carbon Dioxide at 1",
                              :units     "%",
                              :colour    {:r 0, :g 51, :b 102},
                              :intersect [:gas-at-location/by-id 503],
                              :graph/points
                                         [[:graph-point/by-id 2006]
                                          [:graph-point/by-id 2007]
                                          [:graph-point/by-id 2008]]},
                             103
                             {:id        103,
                              :name      "Carbon Monoxide at 1",
                              :units     "ppm",
                              :colour    {:r 255, :g 0, :b 0},
                              :intersect [:gas-at-location/by-id 502],
                              :graph/points
                                         [[:graph-point/by-id 2009]
                                          [:graph-point/by-id 2010]
                                          [:graph-point/by-id 2011]]}},
   :graph/labels            [[:label/by-id 10000]],
   :label/by-id
                            {10000 {:id 10000, :name "Carbon Monoxide at 1", :dec-places 1}},
   :button/id
                            {1 {:id 1, :name "Map", :description "Mine plan", :showing true},
                             2
                               {:id          2,
                                :name        "Trending",
                                :description "Live data, Trending",
                                :showing     true},
                             3
                               {:id          3,
                                :name        "Thresholds",
                                :description "Alarm Thresholds",
                                :showing     true},
                             4
                               {:id          4,
                                :name        "Reports",
                                :description "Event Reports",
                                :showing     true},
                             5
                               {:id          5,
                                :name        "Automatic",
                                :description "Automatic Tube Bundle",
                                :showing     true},
                             6 {:id 6, :name "Logs", :description "Warning Log", :showing true}},
   :plumb-line/by-id
                            {10201
                             {:id              10201,
                              :height          30,
                              :visible?        true,
                              :x-position      10,
                              :in-sticky-time? false}},
   :gas-of-system/by-id
                            {150 {:id 150, :long-name "Methane", :short-name "CH₄"},
                             151 {:id 151, :long-name "Oxygen", :short-name "O₂"},
                             152 {:id 152, :long-name "Carbon Monoxide", :short-name "CO"},
                             153 {:id 153, :long-name "Carbon Dioxide", :short-name "CO₂"}},
   :graph-point/by-id
                            {2000 {:id 2000, :x 20, :y 23},
                             2001 {:id 2001, :x 21, :y 24},
                             2002 {:id 2002, :x 22, :y 25},
                             2003 {:id 2003, :x 33, :y 26},
                             2004 {:id 2004, :x 34, :y 27},
                             2005 {:id 2005, :x 35, :y 28},
                             2006 {:id 2006, :x 46, :y 29},
                             2007 {:id 2007, :x 47, :y 30},
                             2008 {:id 2008, :x 48, :y 31},
                             2009 {:id 2009, :x 59, :y 32},
                             2010 {:id 2010, :x 50, :y 33},
                             2011 {:id 2011, :x 51, :y 34}},
   :drop-info/by-id
                            {10200
                             {:id            10200,
                              :x             50,
                              :lines
                                             [[:line/by-id 100]
                                              [:line/by-id 101]
                                              [:line/by-id 102]
                                              [:line/by-id 103]],
                              :current-label [:label/by-id 10000],
                              :x-gas-details
                                             [[:x-gas-details/by-id 10100]
                                              [:x-gas-details/by-id 10101]
                                              [:x-gas-details/by-id 10102]]}},
   :graph/init              {:height 250, :width 640},
   :graph/lines
                            [[:line/by-id 100]
                             [:line/by-id 101]
                             [:line/by-id 102]
                             [:line/by-id 103]],
   :graph/last-mouse-moment nil,
   :app/selected-button     [:button/by-id 3],
   :graph/plumb-line        [:plumb-line/by-id 10201],
   :graph/points
                            [[:graph-point/by-id 2000]
                             [:graph-point/by-id 2001]
                             [:graph-point/by-id 2002]
                             [:graph-point/by-id 2003]
                             [:graph-point/by-id 2004]
                             [:graph-point/by-id 2005]
                             [:graph-point/by-id 2006]
                             [:graph-point/by-id 2007]
                             [:graph-point/by-id 2008]
                             [:graph-point/by-id 2009]
                             [:graph-point/by-id 2010]
                             [:graph-point/by-id 2011]],
   :tube/by-id
                            {1000
                             {:id       1000,
                              :tube-num 1,
                              :tube/gases
                                        [[:gas-at-location/by-id 500]
                                         [:gas-at-location/by-id 501]
                                         [:gas-at-location/by-id 502]
                                         [:gas-at-location/by-id 503]]},
                             1001
                             {:id       1001,
                              :tube-num 2,
                              :tube/gases
                                        [[:gas-at-location/by-id 504]
                                         [:gas-at-location/by-id 505]
                                         [:gas-at-location/by-id 506]
                                         [:gas-at-location/by-id 507]]},
                             1002
                             {:id       1002,
                              :tube-num 3,
                              :tube/gases
                                        [[:gas-at-location/by-id 508]
                                         [:gas-at-location/by-id 509]
                                         [:gas-at-location/by-id 510]
                                         [:gas-at-location/by-id 511]]},
                             1003
                             {:id       1003,
                              :tube-num 4,
                              :tube/gases
                                        [[:gas-at-location/by-id 512]
                                         [:gas-at-location/by-id 513]
                                         [:gas-at-location/by-id 514]
                                         [:gas-at-location/by-id 515]]},
                             1004
                             {:id       1004,
                              :tube-num 5,
                              :tube/gases
                                        [[:gas-at-location/by-id 516]
                                         [:gas-at-location/by-id 517]
                                         [:gas-at-location/by-id 518]
                                         [:gas-at-location/by-id 519]]},
                             1005
                             {:id       1005,
                              :tube-num 6,
                              :tube/gases
                                        [[:gas-at-location/by-id 520]
                                         [:gas-at-location/by-id 521]
                                         [:gas-at-location/by-id 522]
                                         [:gas-at-location/by-id 523]]},
                             1006
                             {:id       1006,
                              :tube-num 7,
                              :tube/gases
                                        [[:gas-at-location/by-id 524]
                                         [:gas-at-location/by-id 525]
                                         [:gas-at-location/by-id 526]
                                         [:gas-at-location/by-id 527]]},
                             1007
                             {:id       1007,
                              :tube-num 8,
                              :tube/gases
                                        [[:gas-at-location/by-id 528]
                                         [:gas-at-location/by-id 529]
                                         [:gas-at-location/by-id 530]
                                         [:gas-at-location/by-id 531]]},
                             1008
                             {:id       1008,
                              :tube-num 9,
                              :tube/gases
                                        [[:gas-at-location/by-id 532]
                                         [:gas-at-location/by-id 533]
                                         [:gas-at-location/by-id 534]
                                         [:gas-at-location/by-id 535]]},
                             1009
                             {:id       1009,
                              :tube-num 10,
                              :tube/gases
                                        [[:gas-at-location/by-id 536]
                                         [:gas-at-location/by-id 537]
                                         [:gas-at-location/by-id 538]
                                         [:gas-at-location/by-id 539]]}}})

(def real-project-empty-points
  {:tube/gases
                            [[:gas-at-location/by-id 500]
                             [:gas-at-location/by-id 501]
                             [:gas-at-location/by-id 502]
                             [:gas-at-location/by-id 503]
                             [:gas-at-location/by-id 504]
                             [:gas-at-location/by-id 505]
                             [:gas-at-location/by-id 506]
                             [:gas-at-location/by-id 507]
                             [:gas-at-location/by-id 508]
                             [:gas-at-location/by-id 509]
                             [:gas-at-location/by-id 510]
                             [:gas-at-location/by-id 511]
                             [:gas-at-location/by-id 512]
                             [:gas-at-location/by-id 513]
                             [:gas-at-location/by-id 514]
                             [:gas-at-location/by-id 515]
                             [:gas-at-location/by-id 516]
                             [:gas-at-location/by-id 517]
                             [:gas-at-location/by-id 518]
                             [:gas-at-location/by-id 519]
                             [:gas-at-location/by-id 520]
                             [:gas-at-location/by-id 521]
                             [:gas-at-location/by-id 522]
                             [:gas-at-location/by-id 523]
                             [:gas-at-location/by-id 524]
                             [:gas-at-location/by-id 525]
                             [:gas-at-location/by-id 526]
                             [:gas-at-location/by-id 527]
                             [:gas-at-location/by-id 528]
                             [:gas-at-location/by-id 529]
                             [:gas-at-location/by-id 530]
                             [:gas-at-location/by-id 531]
                             [:gas-at-location/by-id 532]
                             [:gas-at-location/by-id 533]
                             [:gas-at-location/by-id 534]
                             [:gas-at-location/by-id 535]
                             [:gas-at-location/by-id 536]
                             [:gas-at-location/by-id 537]
                             [:gas-at-location/by-id 538]
                             [:gas-at-location/by-id 539]],
   :app/buttons
                            [[:button/by-id 1]
                             [:button/by-id 2]
                             [:button/by-id 3]
                             [:button/by-id 4]
                             [:button/by-id 5]
                             [:button/by-id 6]],
   :app/tubes
                            [[:tube/by-id 1000]
                             [:tube/by-id 1001]
                             [:tube/by-id 1002]
                             [:tube/by-id 1003]
                             [:tube/by-id 1004]
                             [:tube/by-id 1005]
                             [:tube/by-id 1006]
                             [:tube/by-id 1007]
                             [:tube/by-id 1008]
                             [:tube/by-id 1009]],
   :app/gases
                            [[:gas-of-system/by-id 150]
                             [:gas-of-system/by-id 151]
                             [:gas-of-system/by-id 152]
                             [:gas-of-system/by-id 153]],
   :graph/labels-visible?   false,
   :graph/hover-pos         nil,
   :gas-at-location/by-id
                            {512
                             {:id         512,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1003]},
                             513
                             {:id         513,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1003],
                              :selected   true},
                             514
                             {:id         514,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1003]},
                             515
                             {:id         515,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1003]},
                             516
                             {:id         516,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1004]},
                             517
                             {:id         517,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1004]},
                             518
                             {:id         518,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1004]},
                             519
                             {:id         519,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1004]},
                             520
                             {:id         520,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1005]},
                             521
                             {:id         521,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1005]},
                             522
                             {:id         522,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1005]},
                             523
                             {:id         523,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1005]},
                             524
                             {:id         524,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1006]},
                             525
                             {:id         525,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1006]},
                             526
                             {:id         526,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1006]},
                             527
                             {:id         527,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1006]},
                             528
                             {:id         528,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1007]},
                             529
                             {:id         529,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1007]},
                             530
                             {:id         530,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1007]},
                             531
                             {:id         531,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1007]},
                             500
                             {:id         500,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1000],
                              :selected   true},
                             532
                             {:id         532,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1008]},
                             501
                             {:id         501,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1000]},
                             533
                             {:id         533,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1008]},
                             502
                             {:id         502,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1000]},
                             534
                             {:id         534,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1008]},
                             503
                             {:id         503,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1000]},
                             535
                             {:id         535,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1008]},
                             504
                             {:id         504,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1001]},
                             536
                             {:id         536,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1009]},
                             505
                             {:id         505,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1001]},
                             537
                             {:id         537,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1009]},
                             506
                             {:id         506,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1001]},
                             538
                             {:id         538,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1009]},
                             507
                             {:id         507,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1001]},
                             539
                             {:id         539,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1009]},
                             508
                             {:id         508,
                              :system-gas [:gas-of-system/by-id 150],
                              :tube       [:tube/by-id 1002]},
                             509
                             {:id         509,
                              :system-gas [:gas-of-system/by-id 151],
                              :tube       [:tube/by-id 1002]},
                             510
                             {:id         510,
                              :system-gas [:gas-of-system/by-id 152],
                              :tube       [:tube/by-id 1002]},
                             511
                             {:id         511,
                              :system-gas [:gas-of-system/by-id 153],
                              :tube       [:tube/by-id 1002],
                              :selected   true}},
   :x-gas-details/by-id
                            {10100
                             {:id               10100,
                              :name             "Carbon Dioxide at 1",
                              :proportional-y   146.33422462612975,
                              :proportional-val 0.19667279430464207},
                             10101
                             {:id               10101,
                              :name             "Carbon Monoxide at 1",
                              :proportional-y   131.68775824757364,
                              :proportional-val 11.337551649514731},
                             10102
                             {:id               10102,
                              :name             "Oxygen at 1",
                              :proportional-y   161.68775824757364,
                              :proportional-val 10.337551649514731}},
   :graph/x-gas-details
                            [[:x-gas-details/by-id 10100]
                             [:x-gas-details/by-id 10101]
                             [:x-gas-details/by-id 10102]],
   :graph/args              nil,
   :graph/translators       {:horiz nil, :vert nil, :point nil},
   :graph/drop-info         [:drop-info/by-id 10200],
   :line/by-id
                            {100
                             {:id        100,
                              :name      "Methane at 1",
                              :units     "%",
                              :colour    {:r 255, :g 0, :b 255},
                              :intersect [:gas-at-location/by-id 500],
                              :graph/points
                                         []},
                             101
                             {:id        101,
                              :name      "Oxygen at 1",
                              :units     "%",
                              :colour    {:r 0, :g 102, :b 0},
                              :intersect [:gas-at-location/by-id 501],
                              :graph/points
                                         []},
                             102
                             {:id        102,
                              :name      "Carbon Dioxide at 1",
                              :units     "%",
                              :colour    {:r 0, :g 51, :b 102},
                              :intersect [:gas-at-location/by-id 503],
                              :graph/points
                                         []},
                             103
                             {:id        103,
                              :name      "Carbon Monoxide at 1",
                              :units     "ppm",
                              :colour    {:r 255, :g 0, :b 0},
                              :intersect [:gas-at-location/by-id 502],
                              :graph/points
                                         []}},
   :graph/labels            [[:label/by-id 10000]],
   :label/by-id
                            {10000 {:id 10000, :name "Carbon Monoxide at 1", :dec-places 1}},
   :button/by-id
                            {1 {:id 1, :name "Map", :description "Mine plan", :showing true},
                             2
                               {:id          2,
                                :name        "Trending",
                                :description "Live data, Trending",
                                :showing     true},
                             3
                               {:id          3,
                                :name        "Thresholds",
                                :description "Alarm Thresholds",
                                :showing     true},
                             4
                               {:id          4,
                                :name        "Reports",
                                :description "Event Reports",
                                :showing     true},
                             5
                               {:id          5,
                                :name        "Automatic",
                                :description "Automatic Tube Bundle",
                                :showing     true},
                             6 {:id 6, :name "Logs", :description "Warning Log", :showing true}},
   :plumb-line/by-id
                            {10201
                             {:id              10201,
                              :height          30,
                              :visible?        true,
                              :x-position      10,
                              :in-sticky-time? false}},
   :gas-of-system/by-id
                            {150 {:id 150, :long-name "Methane", :short-name "CH₄"},
                             151 {:id 151, :long-name "Oxygen", :short-name "O₂"},
                             152 {:id 152, :long-name "Carbon Monoxide", :short-name "CO"},
                             153 {:id 153, :long-name "Carbon Dioxide", :short-name "CO₂"}},
   :drop-info/by-id
                            {10200
                             {:id            10200,
                              :x             50,
                              :lines
                                             [[:line/by-id 100]
                                              [:line/by-id 101]
                                              [:line/by-id 102]
                                              [:line/by-id 103]],
                              :current-label [:label/by-id 10000],
                              :x-gas-details
                                             [[:x-gas-details/by-id 10100]
                                              [:x-gas-details/by-id 10101]
                                              [:x-gas-details/by-id 10102]]}},
   :graph/init              {:height 250, :width 640},
   :graph/lines
                            [[:line/by-id 100]
                             [:line/by-id 101]
                             [:line/by-id 102]
                             [:line/by-id 103]],
   :graph/last-mouse-moment nil,
   :app/selected-button     [:button/by-id 3],
   :graph/plumb-line        [:plumb-line/by-id 10201],
   :graph/points
                            [],
   :tube/by-id
                            {1000
                             {:id       1000,
                              :tube-num 1,
                              :tube/gases
                                        [[:gas-at-location/by-id 500]
                                         [:gas-at-location/by-id 501]
                                         [:gas-at-location/by-id 502]
                                         [:gas-at-location/by-id 503]]},
                             1001
                             {:id       1001,
                              :tube-num 2,
                              :tube/gases
                                        [[:gas-at-location/by-id 504]
                                         [:gas-at-location/by-id 505]
                                         [:gas-at-location/by-id 506]
                                         [:gas-at-location/by-id 507]]},
                             1002
                             {:id       1002,
                              :tube-num 3,
                              :tube/gases
                                        [[:gas-at-location/by-id 508]
                                         [:gas-at-location/by-id 509]
                                         [:gas-at-location/by-id 510]
                                         [:gas-at-location/by-id 511]]},
                             1003
                             {:id       1003,
                              :tube-num 4,
                              :tube/gases
                                        [[:gas-at-location/by-id 512]
                                         [:gas-at-location/by-id 513]
                                         [:gas-at-location/by-id 514]
                                         [:gas-at-location/by-id 515]]},
                             1004
                             {:id       1004,
                              :tube-num 5,
                              :tube/gases
                                        [[:gas-at-location/by-id 516]
                                         [:gas-at-location/by-id 517]
                                         [:gas-at-location/by-id 518]
                                         [:gas-at-location/by-id 519]]},
                             1005
                             {:id       1005,
                              :tube-num 6,
                              :tube/gases
                                        [[:gas-at-location/by-id 520]
                                         [:gas-at-location/by-id 521]
                                         [:gas-at-location/by-id 522]
                                         [:gas-at-location/by-id 523]]},
                             1006
                             {:id       1006,
                              :tube-num 7,
                              :tube/gases
                                        [[:gas-at-location/by-id 524]
                                         [:gas-at-location/by-id 525]
                                         [:gas-at-location/by-id 526]
                                         [:gas-at-location/by-id 527]]},
                             1007
                             {:id       1007,
                              :tube-num 8,
                              :tube/gases
                                        [[:gas-at-location/by-id 528]
                                         [:gas-at-location/by-id 529]
                                         [:gas-at-location/by-id 530]
                                         [:gas-at-location/by-id 531]]},
                             1008
                             {:id       1008,
                              :tube-num 9,
                              :tube/gases
                                        [[:gas-at-location/by-id 532]
                                         [:gas-at-location/by-id 533]
                                         [:gas-at-location/by-id 534]
                                         [:gas-at-location/by-id 535]]},
                             1009
                             {:id       1009,
                              :tube-num 10,
                              :tube/gases
                                        [[:gas-at-location/by-id 536]
                                         [:gas-at-location/by-id 537]
                                         [:gas-at-location/by-id 538]
                                         [:gas-at-location/by-id 539]]}}})

(def with-object {:graph/misc           [:misc/by-id 10400],
                  :tube/gases
                                        [[:gas-at-location/by-id 500]
                                         [:gas-at-location/by-id 501]
                                         [:gas-at-location/by-id 502]
                                         [:gas-at-location/by-id 503]
                                         [:gas-at-location/by-id 504]
                                         [:gas-at-location/by-id 505]
                                         [:gas-at-location/by-id 506]
                                         [:gas-at-location/by-id 507]
                                         [:gas-at-location/by-id 508]
                                         [:gas-at-location/by-id 509]
                                         [:gas-at-location/by-id 510]
                                         [:gas-at-location/by-id 511]
                                         [:gas-at-location/by-id 512]
                                         [:gas-at-location/by-id 513]
                                         [:gas-at-location/by-id 514]
                                         [:gas-at-location/by-id 515]
                                         [:gas-at-location/by-id 516]
                                         [:gas-at-location/by-id 517]
                                         [:gas-at-location/by-id 518]
                                         [:gas-at-location/by-id 519]
                                         [:gas-at-location/by-id 520]
                                         [:gas-at-location/by-id 521]
                                         [:gas-at-location/by-id 522]
                                         [:gas-at-location/by-id 523]
                                         [:gas-at-location/by-id 524]
                                         [:gas-at-location/by-id 525]
                                         [:gas-at-location/by-id 526]
                                         [:gas-at-location/by-id 527]
                                         [:gas-at-location/by-id 528]
                                         [:gas-at-location/by-id 529]
                                         [:gas-at-location/by-id 530]
                                         [:gas-at-location/by-id 531]
                                         [:gas-at-location/by-id 532]
                                         [:gas-at-location/by-id 533]
                                         [:gas-at-location/by-id 534]
                                         [:gas-at-location/by-id 535]
                                         [:gas-at-location/by-id 536]
                                         [:gas-at-location/by-id 537]
                                         [:gas-at-location/by-id 538]
                                         [:gas-at-location/by-id 539]],
                  :app/buttons
                                        [[:button/by-id 1]
                                         [:button/by-id 2]
                                         [:button/by-id 3]
                                         [:button/by-id 4]
                                         [:button/by-id 5]
                                         [:button/by-id 6]
                                         [:button/by-id 7]],
                  :app/tubes
                                        [[:tube/by-id 1000]
                                         [:tube/by-id 1001]
                                         [:tube/by-id 1002]
                                         [:tube/by-id 1003]
                                         [:tube/by-id 1004]
                                         [:tube/by-id 1005]
                                         [:tube/by-id 1006]
                                         [:tube/by-id 1007]
                                         [:tube/by-id 1008]
                                         [:tube/by-id 1009]],
                  :app/gases
                                        [[:gas-of-system/by-id 150]
                                         [:gas-of-system/by-id 151]
                                         [:gas-of-system/by-id 152]
                                         [:gas-of-system/by-id 153]],
                  :gas-at-location/by-id
                                        {512
                                         {:id         512,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1003]},
                                         513
                                         {:id         513,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1003],
                                          :selected   true},
                                         514
                                         {:id         514,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1003]},
                                         515
                                         {:id         515,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1003]},
                                         516
                                         {:id         516,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1004]},
                                         517
                                         {:id         517,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1004]},
                                         518
                                         {:id         518,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1004]},
                                         519
                                         {:id         519,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1004]},
                                         520
                                         {:id         520,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1005]},
                                         521
                                         {:id         521,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1005]},
                                         522
                                         {:id         522,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1005]},
                                         523
                                         {:id         523,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1005]},
                                         524
                                         {:id         524,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1006]},
                                         525
                                         {:id         525,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1006]},
                                         526
                                         {:id         526,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1006]},
                                         527
                                         {:id         527,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1006]},
                                         528
                                         {:id         528,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1007]},
                                         529
                                         {:id         529,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1007]},
                                         530
                                         {:id         530,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1007]},
                                         531
                                         {:id         531,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1007]},
                                         500
                                         {:id         500,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1000],
                                          :selected   true},
                                         532
                                         {:id         532,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1008]},
                                         501
                                         {:id         501,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1000]},
                                         533
                                         {:id         533,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1008]},
                                         502
                                         {:id         502,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1000]},
                                         534
                                         {:id         534,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1008]},
                                         503
                                         {:id         503,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1000]},
                                         535
                                         {:id         535,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1008]},
                                         504
                                         {:id         504,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1001]},
                                         536
                                         {:id         536,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1009]},
                                         505
                                         {:id         505,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1001]},
                                         537
                                         {:id         537,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1009]},
                                         506
                                         {:id         506,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1001]},
                                         538
                                         {:id         538,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1009]},
                                         507
                                         {:id         507,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1001]},
                                         539
                                         {:id         539,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1009]},
                                         508
                                         {:id         508,
                                          :system-gas [:gas-of-system/by-id 150],
                                          :tube       [:tube/by-id 1002]},
                                         509
                                         {:id         509,
                                          :system-gas [:gas-of-system/by-id 151],
                                          :tube       [:tube/by-id 1002]},
                                         510
                                         {:id         510,
                                          :system-gas [:gas-of-system/by-id 152],
                                          :tube       [:tube/by-id 1002]},
                                         511
                                         {:id         511,
                                          :system-gas [:gas-of-system/by-id 153],
                                          :tube       [:tube/by-id 1002],
                                          :selected   true}},
                  :x-gas-details/by-id
                                        {10100
                                         {:id               10100,
                                          :name             "Carbon Dioxide at 2",
                                          :proportional-y   146.33422462612975,
                                          :proportional-val 0.19667279430464207},
                                         10101
                                         {:id               10101,
                                          :name             "Carbon Monoxide at 3",
                                          :proportional-y   131.68775824757364,
                                          :proportional-val 11.337551649514731},
                                         10102
                                         {:id               10102,
                                          :name             "Oxygen at 4",
                                          :proportional-y   161.68775824757364,
                                          :proportional-val 10.337551649514731}},
                  :graph/x-gas-details
                                        [[:x-gas-details/by-id 10100]
                                         [:x-gas-details/by-id 10101]
                                         [:x-gas-details/by-id 10102]],
                  :graph/drop-info      [:drop-info/by-id 10200],
                  :line/by-id
                                        {100
                                         {:id           100,
                                          :name         "Methane at 1",
                                          :units        "%",
                                          :colour       {:r 255, :g 0, :b 255},
                                          :intersect    [:gas-at-location/by-id 500],
                                          :graph/points []},
                                         101
                                         {:id           101,
                                          :name         "Oxygen at 4",
                                          :units        "%",
                                          :colour       {:r 0, :g 102, :b 0},
                                          :intersect    [:gas-at-location/by-id 501],
                                          :graph/points []},
                                         102
                                         {:id           102,
                                          :name         "Carbon Dioxide at 2",
                                          :units        "%",
                                          :colour       {:r 0, :g 51, :b 102},
                                          :intersect    [:gas-at-location/by-id 503],
                                          :graph/points []},
                                         103
                                         {:id           103,
                                          :name         "Carbon Monoxide at 3",
                                          :units        "ppm",
                                          :colour       {:r 255, :g 0, :b 0},
                                          :intersect    [:gas-at-location/by-id 502],
                                          :graph/points []}},
                  :label/by-id          {10000 {:id 10000}},
                  :button/by-id
                                        {1 {:id 1, :name "Map", :description "Mine plan", :showing true},
                                         2
                                           {:id          2,
                                            :name        "Trending",
                                            :description "Live data, Trending",
                                            :showing     true},
                                         3
                                           {:id          3,
                                            :name        "Thresholds",
                                            :description "Alarm Thresholds",
                                            :showing     true},
                                         4
                                           {:id          4,
                                            :name        "Reports",
                                            :description "Event Reports",
                                            :showing     true},
                                         5
                                           {:id          5,
                                            :name        "Automatic",
                                            :description "Automatic Tube Bundle",
                                            :showing     true},
                                         6 {:id 6, :name "Logs", :description "Warning Log", :showing true},
                                         7
                                           {:id          7,
                                            :name        "Debug",
                                            :description "Debug while developing",
                                            :showing     true}},
                  :plumb-line/by-id
                                        {10201
                                         {:id 10201, :visible? true, :x-position 10, :in-sticky-time? false}},
                  :graph/trending-graph [:trending-graph/by-id 10300],
                  :gas-of-system/by-id
                                        {150 {:id 150, :long-name "Methane", :short-name "CH₄"},
                                         151 {:id 151, :long-name "Oxygen", :short-name "O₂"},
                                         152 {:id 152, :long-name "Carbon Monoxide", :short-name "CO"},
                                         153 {:id 153, :long-name "Carbon Dioxide", :short-name "CO₂"}},
                  :misc/by-id
                                        {10400
                                         {:id     10400,
                                          :comms  (chan),
                                          :width  640,
                                          :height 250}},
                  :drop-info/by-id
                                        {10200
                                         {:id            10200,
                                          :x             50,
                                          :lines
                                                         [[:line/by-id 100]
                                                          [:line/by-id 101]
                                                          [:line/by-id 102]
                                                          [:line/by-id 103]],
                                          :current-label [:label/by-id 10000],
                                          :x-gas-details
                                                         [[:x-gas-details/by-id 10100]
                                                          [:x-gas-details/by-id 10101]
                                                          [:x-gas-details/by-id 10102]]}},
                  :graph/lines
                                        [[:line/by-id 100]
                                         [:line/by-id 101]
                                         [:line/by-id 102]
                                         [:line/by-id 103]],
                  :app/selected-button  [:button/by-id 3],
                  :trending-graph/by-id
                                        {10300
                                         {:graph/misc        [:misc/by-id 10400],
                                          :width             640,
                                          :graph/translators {:horiz-fn nil, :vert-fn nil, :point-fn nil},
                                          :graph/drop-info   [:drop-info/by-id 10200],
                                          :labels-visible?   false,
                                          :id                10300,
                                          :receiving?        false,
                                          :graph/lines
                                                             [[:line/by-id 100]
                                                              [:line/by-id 101]
                                                              [:line/by-id 102]
                                                              [:line/by-id 103]],
                                          :graph/plumb-line  [:plumb-line/by-id 10201],
                                          :height            250}},
                  :graph/plumb-line     [:plumb-line/by-id 10201],
                  :graph/points         [],
                  :tube/by-id
                                        {1000
                                         {:id       1000,
                                          :tube-num 1,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 500]
                                                     [:gas-at-location/by-id 501]
                                                     [:gas-at-location/by-id 502]
                                                     [:gas-at-location/by-id 503]]},
                                         1001
                                         {:id       1001,
                                          :tube-num 2,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 504]
                                                     [:gas-at-location/by-id 505]
                                                     [:gas-at-location/by-id 506]
                                                     [:gas-at-location/by-id 507]]},
                                         1002
                                         {:id       1002,
                                          :tube-num 3,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 508]
                                                     [:gas-at-location/by-id 509]
                                                     [:gas-at-location/by-id 510]
                                                     [:gas-at-location/by-id 511]]},
                                         1003
                                         {:id       1003,
                                          :tube-num 4,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 512]
                                                     [:gas-at-location/by-id 513]
                                                     [:gas-at-location/by-id 514]
                                                     [:gas-at-location/by-id 515]]},
                                         1004
                                         {:id       1004,
                                          :tube-num 5,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 516]
                                                     [:gas-at-location/by-id 517]
                                                     [:gas-at-location/by-id 518]
                                                     [:gas-at-location/by-id 519]]},
                                         1005
                                         {:id       1005,
                                          :tube-num 6,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 520]
                                                     [:gas-at-location/by-id 521]
                                                     [:gas-at-location/by-id 522]
                                                     [:gas-at-location/by-id 523]]},
                                         1006
                                         {:id       1006,
                                          :tube-num 7,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 524]
                                                     [:gas-at-location/by-id 525]
                                                     [:gas-at-location/by-id 526]
                                                     [:gas-at-location/by-id 527]]},
                                         1007
                                         {:id       1007,
                                          :tube-num 8,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 528]
                                                     [:gas-at-location/by-id 529]
                                                     [:gas-at-location/by-id 530]
                                                     [:gas-at-location/by-id 531]]},
                                         1008
                                         {:id       1008,
                                          :tube-num 9,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 532]
                                                     [:gas-at-location/by-id 533]
                                                     [:gas-at-location/by-id 534]
                                                     [:gas-at-location/by-id 535]]},
                                         1009
                                         {:id       1009,
                                          :tube-num 10,
                                          :tube/gases
                                                    [[:gas-at-location/by-id 536]
                                                     [:gas-at-location/by-id 537]
                                                     [:gas-at-location/by-id 538]
                                                     [:gas-at-location/by-id 539]]}}})