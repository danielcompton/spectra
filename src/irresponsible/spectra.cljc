(ns irresponsible.spectra
  (:require [#?(:clj clojure.core :cljs cljs.core) :as cc]
            [#?(:clj clojure.spec :cljs cljs.spec) :as s])
  #?(:cljs
     (:require-macros [irresponsible.spectra :refer [if-cljs spec-ns some-spec ns-keys ns-keys*]]))
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

(def spec-ns (if-cljs "cljs.spec" "clojure.spec"))

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
  (let [name (symbol spec-ns "or")]
    `(~name ~@(interleave specs specs))))

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

(defn keys-impl [ns spec-mac opts]
  (letfn [(inner [s]
            (if (namespace s)
              s
              (keyword (name ns) (name s))))
          (outer [[k v]]
            (let [v2 (if (= :gen k) v (mapv inner v))]
              [k v2]))]
    (let [v (select-keys opts [:req-un :req :opt-un :opt :gen])
          name (symbol spec-ns spec-mac)]
      `(~name ~@(mapcat outer (sort (seq v)))))))

(defmacro ns-keys
  "Like {clojure,cljs}.spec/keys, except takes a namespace symbol whose name is
   used as a prefix to all the unprefixed keys. If the namespace symbol has a namespace
   it is ignored for convenience when using syntax-quote
   args: [ns & opts]
   returns: spec def"
  [ns & {:keys [req-un req opt-un opt gen]}]
  (->> (cond-> {}
         gen    (assoc :gen gen)
         req-un (assoc :req-un req-un)
         req    (assoc :req req)
         opt-un (assoc :opt-un opt-un)
         opt    (assoc :opt opt))
       (keys-impl ns "keys")))

;; (s/fdef ns-keys
;;   :args (s/cat :ns symbol? :opts ::ns-keys-opts))

(defmacro ns-keys*
  "Like ns-keys, but returns a regex spec
   args: [ns & opts]
   returns: spec def"
  [ns & {:keys [req-un req opt-un opt gen]}]
  (->> (cond-> {}
         gen    (assoc :gen gen)
         req-un (assoc :req-un req-un)
         req    (assoc :req req)
         opt-un (assoc :opt-un opt-un)
         opt    (assoc :opt opt))
       (keys-impl ns "keys*")))

;; (s/fdef ns-keys*
;;   :args (s/cat :ns symbol? :opts ::ns-keys-opts))

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

