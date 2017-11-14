(ns user
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.stacktrace :refer [print-stack-trace]]
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]))

;; ==================== SERVER ====================

(set-refresh-dirs "dev" "src" "test")


;; Use if don't want the system, when coding from the REPL.
(defn refresh [& args]
  (apply tools-ns/refresh args))
