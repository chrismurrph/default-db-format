(ns default-db-format.general.dev
  (:require
    [fulcro.client.primitives :as prim]
    #?(:cljs [cljs.pprint :as pp])
    #?(:clj
    [clojure.pprint :as pp])))

#?(:cljs (enable-console-print!))

;;
;; About instrumentation and probing that is really useful during development. Lots
;; of intentional crashing (i.e. asserts) here, but also instrumentation things like
;; warnings and pretty printing. Eventually we will elide the crashing out during runtime
;; and make into macros in case the asserts are heavy. For nuts and bolts of that (elide
;; crashing and macros) see a similar library:
;; https://github.com/astoeckley/clojure-assistant
;; When this application becomes 'really professional' then perhaps there will be no calls
;; to this ns, as spec and proper logging framework do the same thing.
;; That's why we call it 'dev'
;;

;;
;; My own invention, is it a list or a vector?
;; Guards against: UnsupportedOperationException nth not supported on this type: PersistentArrayMap
;; , which happens if you try nth on a map:
;; `(nth {1 2} 0)`
;; `(let [[a] {1 2}] a)`
;; , the vector destructuring case being the common bug

(def n-able? (every-pred coll? (complement map?)))

(defn err-empty
  ([x]
   (assert x "Can't check if empty when nil")
   (assert (n-able? x))
   (assert (seq x) "Can't assign empty")
   x)
  ([msg x]
   (assert x "Can't check if empty when nil")
   (assert (n-able? x))
   (assert (seq x) (str "Can't assign empty, msg: " msg))
   x))

(defn warn
  ([want? txt]
   (when-not want?
     (println (str "WARN: " txt #_" -> >" #_want? #_"<"))))
  ([txt]
   (warn true txt)))

(:cljs (defn sleep [_]))

(defmacro assrt
  "Useful to use (rather than official version that this is o/wise a copy of) when don't want intermingling of
  the stack trace produced here with trace output that want to come before"
  ([x]
   (when *assert*
     `(when-not ~x
        (sleep 400)
        (throw (new AssertionError (str "Assert failed: " (pr-str '~x)))))))
  ([x message]
   (when *assert*
     `(when-not ~x
        (sleep 400)
        (throw (new AssertionError (str "Assert failed: " ~message "\n" (pr-str '~x))))))))

(defn chk-v! [v]
  (assert v)
  (assert (n-able? v) v)
  (assert (seq v) v)
  (assert (every? (complement nil?) v) v)
  (assert (every? (some-fn number? keyword? symbol? prim/tempid? string? #_tagged-literal?) v) v))

;;
;; Fixing a terrible discovery that `(get-in {} nil)` will
;; just go on merrily, leading to bugs difficult to track down
;;
(defn get-inn
  ([st v]
   (chk-v! v)
   (clojure.core/get-in st v))
  ([st v default]
   (chk-v! v)
   (clojure.core/get-in st v default)))

(def width 120)

(defn pp-str
  ([n x]
   (binding [pp/*print-right-margin* n]
     (-> x pp/pprint with-out-str)))
  ([x]
   (pp-str width x)))

(defn pp
  ([n x]
   (binding [pp/*print-right-margin* n]
     (-> x pp/pprint)))
  ([x]
   (pp width x)))

(defn probe-off
  ([x]
   x)
  ([x msg]
   (assert (string? msg))
   x))

(defn probe-on
  ([x]
   (-> x
       pp)
   x)
  ([x msg]
   (assert (string? msg))
   (println msg x)
   x))

(defn -flip [f]
  (fn [& xs]
    (apply f (reverse xs))))

(def probe-on-msg (-flip probe-on))
(def probe-off-msg (-flip probe-off))

(def hard-error? true)

(defn err-warn [predicate-res msg]
  (if-not predicate-res
    (if hard-error?
      (assert false msg)
      (do
        (println "WARNING:" msg)
        predicate-res))
    predicate-res))

(defn err-nil-probe
  ([x]
   (assert x "Can't assign nil (or false)")
   x)
  ([x msg]
   (assert x (str "Can't assign nil (or false), msg: " msg))
   x))

(defn err-fn-probe [f msg]
  (fn [x]
    (assert (not (f x)) (str msg ", got: <" x ">"))
    x))

(defn first-crash-if-more [seq]
  (assert (= nil (second seq)) (str "Only supposed to be one. However:\nFIRST:\n" (first seq) "\nSECOND:\n" (second seq)))
  (first seq))

(defn first-crash-if-less [xs]
  (assert (seq xs) "Don't even have one: purposeful crash")
  ;; Get a crash like this, but easier to debug with your own message!
  (nth xs 0))

(defn exactly-1 [xs]
  (let [counted (count xs)]
    (assert (= 1 counted) (str "Expect to be one exactly, got: <" (seq xs) ">")))
  (first xs))

(def only-one exactly-1)

(defn probe-count-on [xs]
  (println "COUNT" (count xs))
  xs)

(defn probe-count-off [xs]
  xs)

(defn probe-first-on [xs]
  (println "FIRST" (pp-str xs))
  xs)

(defn probe-first-off [xs]
  xs)

(defn probe-take-n-on [n xs]
  (println "Try take" n "from" (count xs) "\n" (pp-str (take n xs)))
  xs)

(defn probe-take-n-off [n xs]
  xs)

(defn log [txt]
  (println txt))

(def log-on log)

(defn log-off [txt])

(defn summarize [x]
  (str (cond
         (map? x) (let [counted (count x)]
                    (if (> counted 5)
                      (str counted " map-entries; keys: " (vec (keys x)))
                      (->> x
                           (map (fn [[k v]]
                                  [k (summarize v)]))
                           (into {}))))
         (coll? x) (let [counted (count x)]
                     (if (> counted 5)
                       (str counted " items...")
                       x))
         :else x)))

;;
;; name - of the thing we are asserting on
;; value - of the thing we are asserting on
;;
(defn assert-str [name value]
  (str name " (nil?, fn?, type, value-of): ["
       (nil? value) ", " (fn? value)
       (when (-> value fn? not) (str ", " (type value) ", " value)) "]\n"))



