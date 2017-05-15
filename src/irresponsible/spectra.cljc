(ns irresponsible.spectra
  (:require [#?(:clj clojure.core :cljs cljs.core) :as cc]
            [#?(:clj clojure.spec.alpha :cljs cljs.spec.alpha) :as s])
  #?(:cljs
     (:require-macros [irresponsible.spectra :refer [if-cljs spec-ns some-spec only-keys ns-keys ns-keys* only-ns-keys only-ns-keys*]]))
  (:refer-clojure :exclude [instance?]))

#?
(:clj
 (defmacro if-cljs
   [then else]
   (if (resolve 'cljs.core/str) then else))
 :clj
 (defmacro if-cljs
   [then _]
   then))
 ;; (if (:ns &env) then else)) ;; no longer works, apparently :/

(def spec-ns (if-cljs "cljs.spec.alpha" "clojure.spec.alpha"))
(def spec-fun #(symbol spec-ns %))
(def spec-def (spec-fun "def"))
(def spec-and (spec-fun "and"))
(def spec-or  (spec-fun "or"))

(defmacro some-spec
  "Given a list of spec names, constructs an or using the specs as both names and values
   such that when a spec succeeds, the name is the spec that succeeded
   ex: (some-spec ::my-spec ::your-spec)
        ;; => `(spec/or ::my-spec ::my-spec ::your-spec ::your-spec)
   args: [s & specs]
   returns: an or form"
  [& specs]
  (when-not (seq specs)
    (throw (ex-info "some-spec: expected at least one spec" {:got specs})))
  `(~spec-or ~@(interleave specs specs)))

(s/fdef some-spec
  :args (s/+ keyword?)
  :ret (s/cat :or `#{s/or} :specs (s/+ keyword?)))

(defn assert!
  "Returns the value if it matches the spec, else throws an error with detailed info
   ex: (assert! ::int 123) ; 123
       (assert! float? 1.23) ; 1.23
   args: [spec val]
   returns: val
   throws: if validation fails"
  [spec val]
  (if (s/valid? spec val)
    val
    (throw (ex-info  "Assertion failed"
                     {:got val :spec spec :spec/form (s/form spec)
                      :explain (s/explain-data spec val) :describe (s/describe spec)}))))

(defn conform!
  "Conforms the value and throws an exception if ::clojure.spec/invalid is returned
   ex: (conform! integer? 123)  ;; => 123
       (conform! integer? 1.23) ;; throws
   args: [spec val]
   returns: conformed value
   throws: if val does not conform to spec"
  [spec val]
  (let [r (s/conform spec val)]
    (if (= ::s/invalid r)
      (throw (ex-info  "Conformation failed"
                       {:got val :spec spec :spec/form (s/form spec)
                        :explain (s/explain-data spec val) :describe (s/describe spec)}))
      r)))

(s/def ::kw-vec        (s/coll-of keyword? :kind vector?))
(s/def ::req-un ::kw-vec)
(s/def ::req    ::kw-vec)
(s/def ::opt-un ::kw-vec)
(s/def ::opt    ::kw-vec)
;; (s/def ::ns-keys-opts (s/keys* :opt-un [::req-un ::req ::opt-un ::opt]))

(defn- dens [k]
  (keyword (name k)))

(defn valid-keys [{:keys [req-un req opt-un opt]}]
  (-> (sorted-set)
      (into req)
      (into opt)
      (into (map dens req-un))
      (into (map dens opt-un))))

(defn maybe-ns [ns kw]
  (if (namespace kw)
    kw
    (keyword ns (name kw))))

(defmacro ns-defs [ns & defs]
  (cons 'do
        (map (fn [[k v]]
               `(~spec-def ~(maybe-ns ns k) ~v))
             (partition 2 defs))))

(defn only [& ks]
  (let [s (set ks)]
    #(every? s (keys %))))

(defn- strip-empty [opts]
  (into (empty opts) (filter (fn [[k v]] (or (not (vector? v)) (seq v)))) opts))

(defn- keys-keys [opts]
  (-> (if (map? opts)
        opts
        (apply sorted-map opts))
      (select-keys [:req-un :req :opt-un :opt :gen])
      strip-empty))

(defn ns-kv [ns [k v]]
  (->> (if (= :gen k)
         v
         (mapv (partial maybe-ns ns) v))
       (vector k)))

(defn keys-impl [spec-fn opts-fn opts]
  (->> opts keys-keys
       (mapcat opts-fn)
       (cons (spec-fun spec-fn))))

(defn ns-keys-impl [ns spec-fn opts]
  (keys-impl spec-fn (partial ns-kv ns) opts))

(defn only-impl [spec-fn opts-fn opts]
  `(~spec-and
    ~(keys-impl spec-fn opts-fn opts)
    (only ~@(valid-keys opts))))

(defmacro only-keys [& opts]
  (only-impl "keys" identity opts))

(defmacro ns-keys
  "Like {clojure,cljs}.spec/keys, except takes a namespace string which is
   used as a prefix to all the unprefixed keys. If the namespace symbol has a namespace
   it is ignored for convenience when using syntax-quote
   args: [ns & opts]
   returns: spec def"
  [ns & opts]
  (ns-keys-impl ns "keys" opts))

;; (s/fdef ns-keys
;;   :args (s/cat :ns symbol? :opts ::ns-keys-opts))

(defmacro ns-keys*
  "Like ns-keys, but returns a regex spec
   args: [ns & opts]
   returns: spec def"
  [ns & opts]
  (ns-keys-impl ns "keys*" opts))

;; (s/fdef ns-keys*
;;   :args (s/cat :ns symbol? :opts ::ns-keys-opts))

(defmacro only-ns-keys  [ns & opts]
  (only-impl "keys" (partial ns-kv ns) opts))

(defmacro only-ns-keys* [ns & opts]
  (only-impl "keys*" (partial ns-kv ns) opts))

;;; ## Helpers
;;;
;;; Convenience functions for writing specs that are not directly spec-related
;;;

(defn instance?
  "Returns a predicate that checks if the class is an instance of the named class
   args: [class]
   returns: function"
  [class]
  #(cc/instance? class %))

