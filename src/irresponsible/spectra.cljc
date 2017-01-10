(ns irresponsible.spectra
  (:require [#?(:clj clojure.core :cljs cljs.core) :as cc]
            [#?(:clj clojure.spec :cljs cljs.spec) :as s])
  (:refer-clojure :exclude [instance?]))

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
  `(s/or ~@(interleave specs specs)))

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
(s/def ::ns-keys-opts (s/keys* :opt-un [::req-un ::req ::opt-un ::opt]))

(defmacro ns-keys
  "Like {clojure,cljs}.spec/keys, except takes a namespace symbol whose name is
   used as a prefix to all the unprefixed keys. If the namespace symbol has a namespace
   it is ignored for convenience when using syntax-quote
   args: [ns & opts]
   returns: spec def"
  [ns & opts]
  (let [v (select-keys (conform! ::ns-keys-opts opts) [:req-un :req :opt-un :opt :gen])]
    `(s/keys ~@(mapcat (fn [[k v]]
                         [k (if (= :gen k) v (mapv #(if (namespace %) % (keyword (name ns) (name %))) v))])
                       (sort (seq v))))))

(s/fdef ns-keys
  :args (s/cat :ns symbol? :opts ::ns-keys-opts))

(defmacro ns-keys*
  "Like ns-keys, but returns a regex spec
   args: [ns & opts]
   returns: spec def"
  [ns & opts]
  (let [v (select-keys (conform! ::ns-keys-opts opts) [:req-un :req :opt-un :opt :gen])]
    `(s/keys* ~@(mapcat (fn [[k v]]
                         [k (if (= :gen k) v (mapv #(if (namespace %) % (keyword (name ns) (name %))) v))])
                       (sort (seq v))))))

(s/fdef ns-keys*
  :args (s/cat :ns symbol? :opts ::ns-keys-opts))

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


