(set-env!
 :source-paths    #{"src/cljs"}
 :resource-paths  #{"resources"}
 :init-ns 'default-db-format.core
 :dependencies '[[adzerk/boot-cljs          "1.7.48-6"   :scope "test"]
                 [adzerk/boot-cljs-repl     "0.2.0"      :scope "test"]
                 [adzerk/boot-reload        "0.4.1"      :scope "test"]
                 [pandeiro/boot-http        "0.6.3"      :scope "test"]
                 [org.clojure/clojurescript "1.7.170"]
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]
                 [org.omcljs/om "1.0.0-alpha30"]]
)

;;
;; From Clojure (not script) project
;;(set-env! :resource-paths #{"src"} 
;;          :dependencies '[[org.clojure/core.async "0.1.346.0-17112a-alpha"]])


(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(task-options!
  pom {:project 'default-db-format
       :version "0.1.0"})

(deftask build []
  (comp ;(speak)

        (cljs)
        (target :dir #{"target"})
        ))

(deftask run []
  (comp (serve :dir "target")
        (watch)
        (reload)
        (cljs-repl)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none :source-map true}
                 reload {:on-jsload 'default-db-format.core/init})
  identity)

(deftask dev
  "Running/building a la Figwheel. Not perfect
  b/c you have to refresh browser to notice a change"
  []
  (comp (development)
        (run)))


(deftask testing []
  (set-env! :source-paths #(conj % "test/cljs"))
  identity)

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask test []
  (comp (testing)
        (test-cljs :js-env :phantom
                   :exit?  true)))

(deftask auto-test []
  (comp (testing)
        (watch)
        (test-cljs :js-env :phantom)))
