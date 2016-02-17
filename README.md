# default-db-format
Checks that your Om Next client state is in **default db format**

#### Current release:

###### [default-db-format "0.1.0-SNAPSHOT"] @clojars.org

One way of working with Om Next is to have initial state that is not normalized,
and let Om Next do the normalization for you. Here we check that this initial
normalization - into 'default db format' - succeeds. And check that the state stays 
fully normalized in the face of your code's mutations.
  
Any issues and a heads-up display (HUD) will pop up.

You need to put some code into your root component's render method:

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
      (pprint check-result)
      ;(pprint state)
      (halt-receiving))
    (db-format/show-hud check-result)))  
````

`check` also has a one parameter varity that can be called without any configuration.
This is a good way to start out as the HUD will let you know where its
assumptions have not been met. The most onerous assumption being that all
top level keys be **namespaced** i.e. have a 'slash' in them.

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
  
The intended workflow is that output from the HUD will cause you to either modify your application's
denormalized state or alter the configuration hashmap (`check-config` in the example above) that is 
given to `check`.
  
If you see a false positive or false negative see the last section: ***Hacking***  
  
##### Inputs

All `check` does is see that there are **Idents** everywhere there possibly could be, which is everywhere, in a very
flat structure. You may have special value objects in your data. Unless this program is told about these it
will report a problem and output a false negative. At the moment only simple hashmaps as value objects is 
supported. In the example code above `:okay-value-maps` is a set with `[:r :g :b]` in it. Despite the fact that it is a
vector it is used to recognise maps. Thus for example `{:r 255 :g 255 :b 255}` will no longer be interpreted as a
missing **Ident**.
    
`:excluded-keys` are top level keys you want this program to ignore and `:by-id-kw` is how this program recognises
**Idents**. Your program's component's `ident` methods are all assumed to express their identity  in the same way.
For instance if it is `by-id` then `:line/by-id` and `:graph-point/by-id` will be recognized in first place in an **Ident**.     
  
##### Outputs  

The output from `check` is a map that is understood by the components that make up the HUD.

`:failed-assumption` will be output when default-db-format's input validation criteria are not met.

Take a look at any normalized state graph and you will see two types of top level keys. Each type of key has a
different data shape beneath it. The two types of errors reflect not finding **Idents** in these two different shapes.
That is how we get `:not-normalized-not-ids` and `:not-normalized-ids`. The descriptions used by the 
components will reflect these two types of errors.  
  
##### Hacking/Improving
  
The `examples` package contains files with `def`s in them where each `def` is normalized state that has 
been manually copied out from an application. Each `def` is used in one or more devcards, which can
be found in the `cards.cards` namespace. One way to manually get the supposedly normalized state
is to `(pprint @my-reconciler)` and copy and paste the output from the Chrome developer console into 
a new `def`. Then construct another devcard much like one of the existing ones.
   
There is currently only one way to ***run*** this project:
    
    lein figwheel devcards
    
##### Definitions
    
A **false positive** is where `check` leads you think the state has attained 'default db format' when it has not. 

With a **false negative** issues will be reported where there are none.    
    