(set-env!
 :source-paths    #{"src/cljs"}
 :resource-paths  #{"resources"}
 :init-ns 'default-db-format.core
 :dependencies '[[adzerk/boot-cljs          "1.7.48-6"]
                 [adzerk/boot-cljs-repl     "0.2.0"]
                 [adzerk/boot-reload        "0.4.1"]
                 [pandeiro/boot-http        "0.6.3"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.omcljs/om "1.0.0-alpha30"]]
)

;;;
;;
;; DON'T USE. See lein (project.clj) instead.
;;
;;;

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]])

;; Doesn't work and too hard. Not that it is the source that needs to go into the library
;; anyway. boot for this task will have to wait for an example. Going to lein for this!
;;
;;(deftask build-jar
;;  "Build jar and install to local repo."
;;  []
;;  (comp (sift :add-source #{"src/cljs/default_db_format/core.cljs"}) (pom) (jar) (install)))

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