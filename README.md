# default-db-format
Checks that your Om Next client state is in **default db format**

#### Current release:

[![Clojars Project](https://img.shields.io/clojars/v/default-db-format.svg)](https://clojars.org/default-db-format)

##### Getting Started

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
   
That's all you need to get started.
   
##### Inputs

All `check` does is see that there are Idents everywhere there possibly could be, which is everywhere, in a
flat structure. You may have special value objects in your data. In general unless this program is told about these it
will incorrectly report a problem (*false negative*). Simple hashmaps are supported as value objects as long as they are
specified in the config. Thus in the example code above `:okay-value-maps` is a set with `[:r :g :b]` in it. Despite being a
vector, it is used to recognise maps. Thus for example `{:r 255 :g 255 :b 255}` will no longer be interpreted as a
missing Ident.

Apart from hashmaps, *false negatives* can still occur if you keep complex objects in your state. To remedy this **default-db-format**
 has been hard coded to accept common complex objects, for example `(chan)` and dates. But for other complex types the user has the ultimate say
 because a predicate function can be supplied. It is given the value and is supposed to return logical true for the particular complex
 object you want to accept, logical false otherwise. If you wanted to allow
 dates you could supply `:acceptable-table-value-fn? (fn [v] (= "function Date" (subs (str (type v)) 0 13)))` as a map entry 
 to the config. (Just to be clear: dates have already been hard-coded, just an example). Obviously if many tests are required you can wrap
 them in an `or`, thus ensuring that `false` is returned unless one of them passes. You can also use `:acceptable-table-value-fn?` just to peek at
 unrecognised values, in which case be sure your function returns logical false. For example: `(fn [v] (println "BAD value:" v "," (str (type v))))`.
    
`:excluded-keys` are top level keys that need to be ignored because you don't want them to participate in normalization.

`:by-id-kw` is how Idents are recognised. For instance if it is `by-id` then `:line/by-id` and `:graph-point/by-id` will be recognized in first position
in an Ident. If all your Idents are `by-id` then you don't have to specify anything since `by-id` is the
default. If your program has multiple ways of expressing an Ident then provide a set or a vector of Strings rather than a single String.
  
##### Outputs  

The output from `check` is a map that is understood by the components that make up the HUD. As such you won't normally
be dealing with them directly. But we document them here anyway.

`:failed-assumption` will be output when **default-db-format**'s input validation criteria are not met.

Take a look at any normalized state graph and you will see two types of top level keys. Each type of key has a
different data shape beneath it. The two types of errors reflect not finding Idents in these two different shapes.
That is how we get `:not-normalized-ref-entries` and `:not-normalized-table-entries`. The descriptions used by the 
components will reflect these two types of errors.
  
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
  
The inputs used here (see `check-config` above) serve to describe your app's state. So a good place to
 put them is in the same file as your app's initial state.

##### Tip

Mutations are supposed to be side effect free. Thus it can be difficult to debug them with `println`
 and `assert` statements. Om Next will happily continue without printing `println` statements, not reporting
 failed assertions, even ignoring mutation compilation errors!

Thus it is often worth the effort to take a copy (as in copy/paste from browser console
 the result of `(pprint @my-reconciler)`) of your normalized state and code your mutations against that - completely
 outside of your Om Next application. Then bring them back in when they are working and side effect free.

##### Hacking/Improving
  
The `examples` package contains files with `def`s in them where each `def` is normalized state that has 
been manually copied out from an application. Each `def` is used in one or more devcards, which can
be found in the `cards.cards` namespace. The supposedly normalized state was obtained
by calling `(pprint @my-reconciler)` and copy/pasting the output from the Chrome developer console into
each `def`. Once that is done a devcard much like one of the existing ones can be created. Every devcard has
at least one test associated with it. In cases where a devcard would not be interesting there are tests 
instead. 

The only known way to run this project is through a Cursive REPL. Start the REPL from within
IntelliJ and point the browser at [the cards/tests html page](http://localhost:3449/cards.html#!/cards.cards).

To setup the Cursive REPL you need to follow these [steps](https://github.com/bhauman/lein-figwheel/wiki/Running-figwheel-in-a-Cursive-Clojure-REPL#create-a-clojuremain-cursive-repl-configuration).    
    
##### Definitions
    
A **false positive** is where `check` leads you think the state has attained 'default db format' when it has not.
It is where the program says: "I didn't see an Ident or a recognised value, so that's a problem",
when you don't want it to - when the program should have recognised the value.

With a **false negative** an issue will be reported, despite the fact that 'default db format' has been attained.

For examples of **default db format** take a look at any of the source files in the `examples` package. Note that there
 are two types of map entries: refs and tables. The value of a ref entry is either an Ident or a vector of Idents. Table
 entries are where the actual data values are kept, using Idents to refer to other data values. Here the keys are easily 
 recognisable because they are all `something\by-id`. See [here](https://github.com/omcljs/om/wiki/Components,-Identity-&-Normalization)
 and [here](http://untangled-web.github.io/untangled/tutorial.html#!/untangled_tutorial.G_Mutation) to gain a better
 understanding of normalization.

##### Internal version

The current internal version is 24. Makes sense for when dealing with snapshots. 24 goes with "0.1.1-SNAPSHOT". It is displayed by
 the HUD. Version history:

 *  24. Accepting one or many (sequential or set) for these three inputs: okay-value-maps, by-id-kw and excluded-keys
 *  23. Guards against parameters to `check` being put in wrong order, and hard-coding google date as data
 *  22. Fixed bug where a `:keyword` was not recognised as data
 *  21. Released version (announced on Om Slack group)

    