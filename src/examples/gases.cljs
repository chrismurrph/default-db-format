(ns examples.gases)

(def gas-denorm-state
  {:graph/drop-info [:drop-info/by-id 10200],
   :graph/lines
                    [[:line/by-id 100]
                     [:line/by-id 101]
                     [:line/by-id 102]
                     [:line/by-id 103]],
   :system-gases
                    [{:id 200, :short-name "Methane"}
                     {:id 201, :short-name "Oxygen"}
                     {:id 202, :short-name "Carbon Monoxide"}
                     {:id 203, :short-name "Carbon Dioxide"}],
   ;:app/tubes
   ;                 [{:id 1000, :tube-num "Invercargill"}
   ;                  {:id 1001, :tube-num "Dunedin"}],
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

