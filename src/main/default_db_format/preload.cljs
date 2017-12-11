(ns default-db-format.preload
  (:require [default-db-format.tool :as tool]
            [default-db-format.prefs :as prefs]))

(tool/install (or @prefs/external-config {}))
