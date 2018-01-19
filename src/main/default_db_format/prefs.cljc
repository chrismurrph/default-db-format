(ns default-db-format.prefs
  #?(:cljs (:require-macros [default-db-format.prefs :refer [emit-external-config]]))
  (:require
    #?(:clj
        [default-db-format.helpers :as help])
    #?(:clj
        [cljs.env])
    #?(:clj
        [clojure.java.io :as io])
    #?(:clj
        [clojure.tools.reader.edn :as edn])))

#?(:clj
   (defn read-external-config []
     (if cljs.env/*compiler*
       (or
         (get-in @cljs.env/*compiler* [:options :external-config :default-db-format/config]) ; https://github.com/bhauman/lein-figwheel/commit/80f7306bf5e6bd1330287a6f3cc259ff645d899b
         (get-in @cljs.env/*compiler* [:options :tooling-config :default-db-format/config]))))) ; :tooling-config is deprecated

;;
;; If we didn't use edn/read-string then we could bring in a function.
;;
#?(:clj
   (defn read-from-edn []
     (some-> "config/default-db-format.edn"
             io/resource
             slurp
             edn/read-string)))

#?(:clj
   (defmacro emit-external-config []
     `'~(merge (or (read-external-config) {})
               {:edn (merge help/default-config (read-from-edn))})))

#?(:cljs
   (def external-config (delay (emit-external-config))))
