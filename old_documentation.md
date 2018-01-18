##### OLD DOCUMENTATION

Apart from hash maps, *false negatives* can still occur if you keep complex objects in your state. To remedy this **default-db-format** has been hard coded to accept common complex objects, for example `(chan)` and dates. But for other complex types the user has the ultimate say because a predicate function can be supplied. This function accepts the value and is supposed to return logical true if it is an acceptable complex object, logical false otherwise. If you wanted to allow dates you could supply this map entry:

````clojure
:acceptable-table-value-fn?
(fn [v] (= "function Date" (subs (str (type v)) 0 13)))
````

Just to be clear: dates have already been hard-coded, and in a less flaky way - this was just an example. Another use for `:acceptable-table-value-fn?` is to peek at unrecognised values, in which case be sure your function returns logical false. For example:

````clojure
(fn [v] (println "BAD value:" v "," (str (type v))))
````

Continuing on with config options,`:links` are those that need to be ignored because you don't want them to participate in normalization.

`:by-id-kw` is how Idents are recognised. For instance if it is "by-id" then `:line/by-id` and `:graph-point/by-id` will be recognized in first position in an Ident. If all your Idents are `by-id` then you don't have to specify anything since "by-id" is the default. If your program has multiple ways of expressing an Ident then provide a set or a vector of strings rather than a single string.

`:routing-ns` is a recent addition used for Idents that are used in union queries. While the default of "by-id" for `:by-id-kw` is borrowing from an accepted convention, "routed" for `:routing-ns` is just made up! The routing namespace is what comes before the slash for a routing Ident (e.g. `[:routed/banking :top]`). Again you can provide a vector or a set rather than a single string.

#### Definitions

A **false positive** is where `check` leads you to think the state has attained 'default db format' when it has not. It is where the program says: "I didn't see an Ident or a recognised value, so that's a problem", when you don't want it to - when the program should have recognised the value.

With a **false negative** an issue will be reported, despite the fact that 'default db format' has been attained.

For examples of **default db format** take a look at any of the source files in the `examples` package. Note that there are two types of map entries: refs (also known as joins or edges) and tables. The value of a ref entry is either an Ident or a vector of Idents. Table entries are where the actual data values are kept, using Idents to refer to other data values. Here the keys are easily  recognisable because they are all `whatever\by-id`.
