# default-db-format
Checks that your Om Next client state is in **default db format**

#### Current release:

[![Clojars Project](https://img.shields.io/clojars/v/default-db-format.svg)](https://clojars.org/default-db-format)

One way of working with Om Next is to have it normalize your app's initial state. 
Here we check that this initial 
normalization - into 'default db format' - succeeds. And check that the state stays 
normalized in the face of your code's mutations.
  
Any issues and a heads-up display (HUD) will pop up.

You need to put some code into your root component's render method:

````clojure
(:require [default-db-format.core :as db-format])
          
(render [this]
  (let [app-props (om/props this)]
    (dom/div nil
             (db-format/show-hud (db-format/check @my-reconciler))                       
             ...)))
````
   
That's the end of the *getting started* documentation.

##### *Everything* Example

````clojure
(:require [default-db-format.core :as db-format]
          [cljs.pprint :refer [pprint]])
  
(def irrelevant-keys #{:graph/labels-visible?
                       :graph/hover-pos
                       :om.next/queries
                       })
(def okay-val-maps #{[:r :g :b]})
(def check-config {:excluded-keys irrelevant-keys
                   :okay-value-maps okay-val-maps
                   :by-id-kw "by-id"})
  
(defn check-default-db [state]
  (let [version db-format/version
        check-result (db-format/check check-config state)
        ok? (db-format/ok? check-result)
        msg-boiler (str "normalized (default-db-format ver: " version ")")
        message (if ok?
                  (str "GOOD: state fully " msg-boiler)
                  (str "BAD: state not fully " msg-boiler))]
    (println message)
    (when (not ok?)
      (pprint check-result) ;; <- check-result is a summary of state, so print one or the other
      ;(pprint state)
      (halt-receiving)) ;; <- project specific function that stops continuous state updates
    (db-format/show-hud check-result))) ;; <- must be last, displays check-result
````

`check` also has a one parameter varity that can be called without configuration (`check-config` above) - the HUD will let 
you know where assumptions have not been met. Example assumption - all top level keys be **namespaced** i.e. have a 'slash' in them.
Note that with no configuration for `:by-id-kw` it will be defaulted to "by-id" anyway - hence superfluously set above.

The call to the function we just wrote should be in your root component's render method:

````clojure
(render [this]
  (let [app-props (om/props this)]
    (dom/div nil
             (check-default-db @my-reconciler)
             ...)))
````

The `show-hud` function returns an Om Next component, or nil when there are no issues. 
`check-default-db` is also a *component function* since it returns what `show-hud` returns.
  
The intended workflow is that feedback from the HUD will alert you to do one or more of:
 
 1. modify your application's initial state.
 2. alter the configuration hashmap (`check-config` in the example above) that is given to `check`.
 3. re-code the bad mutation you just wrote.
      
##### Inputs

All `check` does is see that there are Idents everywhere there possibly could be, which is everywhere, in a
flat structure. You may have special value objects in your data. In general unless this program is told about these it
will report a problem and report a *false negative*. Simple hashmaps are supported as value objects as long as they are
specified in the config. Thus in the example code above `:okay-value-maps` is a set with `[:r :g :b]` in it. Despite being a
vector, it is used to recognise maps. Thus for example `{:r 255 :g 255 :b 255}` will no longer be interpreted as a
missing Ident.

A *false negative* is where the program says: "I didn't see an Ident or a recognised value, so that's a problem", when you
 don't want it to - when the program should have recognised the value. This *false negative* can still occur if you keep 
 complex objects in your state. To remedy this **default-db-format**
 has been hard coded to accept common complex objects, for example `(chan)` and dates. But the user has the ultimate say
 because a predicate function can be supplied. It is given the value and is supposed to return a truthy value for the particular complex
 object you want to accept, `false/nil` otherwise. If you wanted to allow 
 dates you could supply `:acceptable-table-value-fn? (fn [v] (= "function Date" (subs (str (type v)) 0 13)))` as a map entry 
 to the config. (Just to be clear: this has already done, just an example). Obviously if many tests are required you can wrap
 them in an `or`, thus ensuring that `false` is returned unless one of them passes. You can also use `:acceptable-table-value-fn?` to peek into
 unrecognised values, in which case be sure it returns a falsey value.
    
`:excluded-keys` are top level keys you want this program to ignore and `:by-id-kw` is how this program recognises
Idents. Your program's component's `ident` methods are all assumed to express their identity  in the same way.
For instance if it is `by-id` then `:line/by-id` and `:graph-point/by-id` will be recognized in first position in an Ident.     
  
##### Outputs  

The output from `check` is a map that is understood by the components that make up the HUD.

`:failed-assumption` will be output when default-db-format's input validation criteria are not met.

Take a look at any normalized state graph and you will see two types of top level keys. Each type of key has a
different data shape beneath it. The two types of errors reflect not finding Idents in these two different shapes.
That is how we get `:not-normalized-ref-entries` and `:not-normalized-table-entries`. The descriptions used by the 
components will reflect these two types of errors.  
  
##### Hacking/Improving
  
The `examples` package contains files with `def`s in them where each `def` is normalized state that has 
been manually copied out from an application. Each `def` is used in one or more devcards, which can
be found in the `cards.cards` namespace. In most cases I got the supposedly normalized state
using `(pprint @my-reconciler)` and copied and pasted the output from the Chrome developer console into 
a new `def`. Then constructed another devcard much like one of the existing ones. Every devcard has 
at least one test associated with it. In cases where a devcard would not be interesting there are tests 
instead. 

The only way I know to run this project is through a Cursive REPL. I start the REPL from within 
IntelliJ and point the browser at [the cards/tests html page](http://localhost:3449/cards.html#!/cards.cards).

To setup the Cursive REPL you need to follow these [steps](https://github.com/bhauman/lein-figwheel/wiki/Running-figwheel-in-a-Cursive-Clojure-REPL#create-a-clojuremain-cursive-repl-configuration).    
    
##### Definitions
    
A **false positive** is where `check` leads you think the state has attained 'default db format' when it has not. 

With a **false negative** issues will be reported, despite the fact that 'default db format' has been attained.

For examples of **default db format** take a look at any of the source files in the `examples` package. Note that there
 are two types of map entries: refs and tables. The value of a ref entry is either an Ident or a vector of Idents. Table
 entries are where the actual data values are kept, using Idents to refer to other data values. Here the keys are easily 
 recognisable because they are all `something\by-id`. See [here](https://github.com/omcljs/om/wiki/Components,-Identity-&-Normalization)
 for an explanation of normalization. 

##### Internal version

The current internal version is 21. Makes sense for when dealing with snapshots. 21 goes with "0.1.1-SNAPSHOT". It is displayed by
 the HUD.
    