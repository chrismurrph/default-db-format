# default-db-format
Checks that your Om Next client state is in **default db format**

#### Current release:

###### [default-db-format 0.0.1] @clojars.org

One way of working with Om Next is to have initial state that is not normalized,
and let Om Next do the normalization for you. This library checks that the
normalization - into what is called 'default db format' - did in fact succeed.
Any issues and a heads-up display (HUD) will pop up.

You need to put some code into your root component's render method:

````clojure
(:require [default-db-format.core :as db-format])
  
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
                  (str "BAD: state not fully " msg-boiler))
        ]
    (db-format/display check-result)
    (println message)
    (when (not ok?)
      (pprint check-result)
      ;(pprint state)
      )))
````

`check` also has a one parameter varity that can be called without any configuration.
This is a good way to start out as the heads up display will let you know where its
assumptions have not been met. The most onerous of these being that all
top level keys be **namespaced** i.e. have a 'slash' in them.

The call to `check-default-db` should be in your root component's render method:

````clojure
  (render [this]
    (let [app-props (om/props this)]
      (dom/div nil
               (check-default-db @my-reconciler)
               ...)))
````
  
The intended workflow is that output from the HUD will cause you to either modify your application's
denormalized state or alter the configuration hashmap (`check-config` in the example above) that is 
given to `check`.
  
If you think there is a false positive/negative(*) please see the next section - ***Hacking***  
  
##### Inputs

All `check` does is see that there are **Idents** everywhere there possibly could be, which is everywhere, in a very
flat structure. You may have special value objects in your data. Unless this program is told about these then it
will report a problem and you will get a false negative. At the moment only simple hashmaps as value objects is 
supported. In the example code above `:okay-value-maps` is a set with `[:r :g :b]` in it. Despite the fact that it is a
vector it is used to recognise maps. Thus for example `{:r 255 :g 255 :b 255}` will no longer raise an error.
    
`:excluded-keys` are top level keys you want this program to ignore and `:by-id-kw` is how this program recognises
**Idents**. Your program's component's `ident` methods are all assumed to express their identity  in the same way.
For instance if it is `by-id` then `:line/by-id` and `:graph-point/by-id` will be recognized in first place in an **Ident**.     
  
##### Outputs  
  
##### Hacking
  
The `examples` package contains files with `def`s in them where each `def` is normalized state that has 
been manually copied out from an application. Each `def` is used in one or more devcards, which can
be found in the `cards.cards` namespace. One way to manually get the supposedly normalized state
is to `(pprint @my-reconciler)` and copy and paste the output from the Chrome developer console into 
a `def`. Then construct a new devcard much like one of the existing ones.
   
There is currently really only one way to ***run*** this project:
    
    lein figwheel devcards
    
(*) A false positive is where `check` leads you think the state has attained 'default db format' when it has not. With a false
negative issues will be reported where there are none.    
    