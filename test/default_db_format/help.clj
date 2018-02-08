(ns default-db-format.help)

(defn many-join-becomes-list [st join]
  (update-in st join #(map identity %)))

(defn change-ident [new-table-key old-ident]
  (assoc old-ident 0 new-table-key))

(defn many-join-becomes-bad-idents [st join new-table-key]
  (update-in st join #(mapv (partial change-ident new-table-key) %)))

