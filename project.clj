(defproject default-db-format "0.1.0-SNAPSHOT"
  :description "Test to see if normalized data is in 'default db format'"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.omcljs/om "1.0.0-alpha30"]
                 [cljsjs/react "0.14.3-0"]
                 [cljsjs/react-dom "0.14.3-1"]
                 [devcards "0.2.1-4"]
                 ]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-1"]]

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    "target"]
                                    
  :source-paths ["src"]
                 
  :cljsbuild {:builds [{:id "devcards"
                        :source-paths ["src"]
                        :figwheel { :devcards true }
                        :compiler { :main       "cards.ui"
                                    :asset-path "js/devcards_out"
                                    :output-to  "resources/public/js/devcards.js"
                                    :output-dir "resources/public/js/devcards_out"
                                    :source-map-timestamp true }}]}

  ;; Remove when all working fine
  :figwheel { :css-dirs ["resources/public/css"] })