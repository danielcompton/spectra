(ns irresponsible.spectra
  (:require [#?(:clj clojure.spec :cljs cljs.spec) :as s])
  (:refer-clojure :as cc :exclude [instance?]))

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
      (throw (ex-info  "Conformation failed failed"
                       {:got val :spec spec :spec/form (s/form spec)
                        :explain (s/explain-data spec val) :describe (s/describe spec)}))
      r)))

;; (defn mapify
;;   ""
;;   [spec]
;;   (let [d (s/describe spec)] 
;;     (match d
;;       (['cat & items] :seq)
;;       (let [is (partition 2 items)]
;;         (map #(match %
;;                 ([_ (v :guard keyword?)] :seq)
;;                 {:kind :req-un :name v}

;;                 ([k (['? (name :guard keyword?)] :seq)] :seq)



;;                      ([k (['* (name :guard keyword?)] :seq)] :seq)



;;                      ([k (['+ (name :guard keyword?)] :seq)] :seq)
                     
      ;; :else (throw (ex-info "mapify: expected a 'cat' spec" {:got spec :describe d})))))


;;; these are shorthand for definitions

(defn instance? [class]
  #(cc/instance? class %))


