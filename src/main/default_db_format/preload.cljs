(ns default-db-format.preload
  (:require [default-db-format.tool :as tool]
            [default-db-format.prefs :as preferences]))

(let [ext-config (-> preferences/external-config deref)
      config (or ext-config {})]
  (tool/install config))
