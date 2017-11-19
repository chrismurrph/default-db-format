(defproject default-db-format "0.1.1-SNAPSHOT"
  :description "Visual feedback if normalized data is not in 'default db format'."
  :url "https://github.com/chrismurrph/default-db-format"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [org.clojure/clojurescript "1.9.854" :scope "provided"]
                 ;;[org.omcljs/om "1.0.0-beta1" :scope "provided"]
                 ;;[fulcrologic/fulcro "1.1.1" :scope "provided"]
                 [fulcrologic/fulcro "2.0.0-SNAPSHOT" :scope "provided"]
                 [org.clojure/tools.namespace "0.3.0-alpha4"]
                 [org.clojure/core.async "0.3.443"]
                 ]

  :jar-exclusions [#"examples" #"test_helpers.clj"]

  :scm {:name "git"
        :url  "https://github.com/chrismurrph/default-db-format"}

  :plugins [[lein-cljsbuild "1.1.2"]]

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    "target"]

  :source-paths ["src" "dev" "script" "test"])