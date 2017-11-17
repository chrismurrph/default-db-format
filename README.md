# default-db-format
Checks that your Om Next client state is in **default db format**

#### Current release:

[![Clojars Project](https://img.shields.io/clojars/v/default-db-format.svg)](https://clojars.org/default-db-format)

##### Description

One way of working with Om Next is to have it normalize your app's initial state. Here we check that this initial normalization - into 'default db format' - succeeds. And check that the state stays normalized in the face of your code's mutations.
  
Any issues and a heads-up display (HUD) will pop up.

One way of using this library is to put some code into your root component's render method:

````clojure
(:require [default-db-format.core :as db-format])

(render [this]
  (let [rec (some-> app deref :reconciler deref)]
    (dom/div nil
             (when rec 
               (db-format/show-hud (db-format/check rec)))
             ...)))
````

##### Inputs

`check` makes sure that every join is an Ident or a vector of Idents. Conversely the one thing you don't want to see in a join is `{:db/id whatever}`. 

You may have denormalized value objects in your data. Unless this library is told about these it will either incorrectly report a problem or let the data pass when it ought not. Simple hash maps are supported as value objects as long as they are specified in the config. Thus in the forthcoming example code `:okay-value-maps` is a set with `[:r :g :b]` in it. It is a vector that is used to recognise maps. Thus for example `{:g 255 :r 255 :b 255}` will no longer be interpreted as a missing Ident. Vectors are also supported as value objects with `:okay-value-vectors`.

Apart from hash maps, *false negatives* can still occur if you keep complex objects in your state. To remedy this **default-db-format** has been hard coded to accept common complex objects, for example `(chan)` and dates. But for other complex types the user has the ultimate say because a predicate function can be supplied. This function accepts the value and is supposed to return logical true if it is an acceptable particular complex, logical false otherwise. If you wanted to allow dates you could supply this map entry: 

````clojure
:acceptable-table-value-fn? 
(fn [v] (= "function Date" (subs (str (type v)) 0 13)))
````

Just to be clear: dates have already been hard-coded, and in a less flaky way - this was just an example. Another use for `:acceptable-table-value-fn?` is to peek at unrecognised values, in which case be sure your function returns logical false. For example: 

````clojure
(fn [v] (println "BAD value:" v "," (str (type v))))
````
    
Continuing on with config options,`:excluded-keys` are those that need to be ignored because you don't want them to participate in normalization.

`:by-id-kw` is how Idents are recognised. For instance if it is "by-id" then `:line/by-id` and `:graph-point/by-id` will be recognized in first position in an Ident. If all your Idents are `by-id` then you don't have to specify anything since "by-id" is the default. If your program has multiple ways of expressing an Ident then provide a set or a vector of strings rather than a single string.

`:routing-ns` is a recent addition used for Idents that are used in union queries. While the default of "by-id" for `:by-id-kw` is borrowing from an accepted convention, "routed" for `:routing-ns` is just made up! The routing namespace is what comes before the slash for a routing Ident (e.g. `[:routed/banking :top]`). Again you can provide a vector or a set rather than a single string.

One thing that may be surprising on first use of **default-db-format** is that all app state keywords must be namespaced, even `:excluded-keys`. The HUD will guide you towards namespacing them all. 
    
##### *Everything* Example

````clojure
(:require [default-db-format.core :as db-format]
          [cljs.pprint :refer [pprint]])
  
(def excluded-keys #{:om.next/tables
                     :fulcro.client.routing/routing-tree
                     :fulcro/ready-to-load
                     :fulcro.ui.forms/form
                     :ui/react-key
                     :ui/locale
                     :ui/loading-data
                     :root/top-router
                     :root/components
                     })
(def okay-val-maps #{})
(def okay-val-vectors #{[:report/balance-sheet :report/big-items-first :report/profit-and-loss :report/trial-balance]})
(def check-config {:excluded-keys      excluded-keys
                   :okay-value-maps    okay-val-maps
                   :okay-value-vectors okay-val-vectors
                   :by-id-kw           "by-id"
                   :routing-ns         "routed"})
  
(defn check-default-db [state]
  (assert (map? state))
  (let [version db-format/version
        check-result (db-format/check check-config state)
        ok? (db-format/ok? check-result)
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        message (if ok?
                  (str "GOOD: state fully " msg-boiler)
                  (str "BAD: state not fully " msg-boiler))]
    (println message)
    (when (not ok?)
      (pprint check-result) ;; <- check-result is a summary of state, so print 'one or *the other*'
      ;(pprint state))      ;; <- *the other*
      (db-format/show-hud check-result)))) ;; <- must be last, displays check-result in browser

(declare app)
````

The call to `check-default-db` can be in your root component's render method:

````clojure
(render [this]
  (let [rec (some-> app deref :reconciler deref)]
    (dom/div nil
             (when rec (check-default-db rec))
             ...)))
````

The `show-hud` function returns an Om Next component, or `nil` when there are no issues. 
`check-default-db` is also a *component function* since it returns what `show-hud` returns.
  
The intended workflow is that feedback from the HUD will alert you to do one or more of these fixes:
 
 1. modify your application's initial state.
 2. alter the configuration hashmap (`check-config` in the example above) that is given to `check`.
 3. re-code the bad mutation you just wrote. 
 
 Using the root component's render function is serving as a crude watch on app state. A better way (not documented because it hasn't yet been tried) would be to directly watch the app state atom and when `check` recognises a problem force a render by changing one of the root props, for instance `ui/react-key` in a Fulcro application. 
 
 As an example of potential confusion from using the root component's render function, consider the initial load causing a denormalization problem. If the updated keys are not currently on the screen then there won't be a render. And then when the end user (or a timer event) causes a render the HUD will display - but again what is on the screen may have no relationship to the denormalization problem.
  
##### Definitions
    
A **false positive** is where `check` leads you think the state has attained 'default db format' when it has not. It is where the program says: "I didn't see an Ident or a recognised value, so that's a problem", when you don't want it to - when the program should have recognised the value.

With a **false negative** an issue will be reported, despite the fact that 'default db format' has been attained.

For examples of **default db format** take a look at any of the source files in the `examples` package. Note that there are two types of map entries: refs (also known as joins or edges) and tables. The value of a ref entry is either an Ident or a vector of Idents. Table entries are where the actual data values are kept, using Idents to refer to other data values. Here the keys are easily  recognisable because they are all `whatever\by-id`.

##### Internal version

The current internal version is **28**. Makes sense for when dealing with snapshots. 28 goes with "0.1.1-SNAPSHOT". 28 is displayed by the HUD. Version history:

 *  **28** Works with Fulcro and on Clojars 
 *  **27** Om now *provided* and this one will be in Clojars
 *  **26** Any function now accepted
 *  **25** If ALL the keys are being ignored then `check` should pass (25 not released to Clojars)
 *  **24** Accepting one or many (sequential or set) for these three inputs: okay-value-maps, by-id-kw and excluded-keys
 *  **23** Guards against parameters to `check` being put in wrong order, and hard-coding google date as data
 *  **22** Fixed bug where a `:keyword` was not recognised as data
 *  **21** Released version (announced on Om Slack group)

    