(defproject default-db-format "0.1.0-SNAPSHOT"
  :description "Visual feedback if normalized data is not in 'default db format'."
  :url "https://github.com/chrismurrph/default-db-format"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}  
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.omcljs/om "1.0.0-alpha30"]
                 [cljsjs/react "0.14.3-0"]
                 [cljsjs/react-dom "0.14.3-1"]
                 [devcards "0.2.1-4"]
                 [figwheel-sidecar "0.5.0-SNAPSHOT"]
                 ]

  :jar-exclusions [#"cards" #"examples" #"index.html" #"cards.html" #"public" #"repl.clj"]

  :scm {:name "git"
        :url "https://github.com/chrismurrph/default-db-format"}

  :plugins [[lein-cljsbuild "1.1.2"]]

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    "target"]
                                    
  :source-paths ["src" "script"]
                 
  :cljsbuild {:builds [{:id "devcards"
                        :source-paths ["src"]
                        :figwheel { :devcards true }
                        :compiler { :main       "cards.ui"
                                    :asset-path "js/devcards_out"
                                    :output-to  "resources/public/js/devcards.js"
                                    :output-dir "resources/public/js/devcards_out"
                                    :source-map-timestamp true }}]})