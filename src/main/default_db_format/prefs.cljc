(ns default-db-format.prefs
  #?(:cljs (:require-macros [default-db-format.prefs :refer [emit-external-config]]))
  (:require #?(:clj [cljs.env])))

#?(:clj
   (defn read-external-config []
     (if cljs.env/*compiler*
       (or
         (get-in @cljs.env/*compiler* [:options :external-config :default-db-format/config]) ; https://github.com/bhauman/lein-figwheel/commit/80f7306bf5e6bd1330287a6f3cc259ff645d899b
         (get-in @cljs.env/*compiler* [:options :tooling-config :default-db-format/config]))))) ; :tooling-config is deprecated

#?(:clj
   (defmacro emit-external-config []
     `'~(or (read-external-config) {})))

#?(:cljs
   (def external-config (delay (emit-external-config))))
