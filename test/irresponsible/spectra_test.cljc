(ns irresponsible.spectra-test
  (:require [irresponsible.spectra :as ss]
            [#?(:clj clojure.spec :cljs cljs.spec) :as s]
            #?(:clj  [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(s/def ::int integer?)
(s/def ::float float?)

(s/def ::int-or-float (ss/some-spec ::int ::float))

(t/deftest all-tests
  (t/testing "some-spec"
    (t/is (= `(s/or ::int ::int ::float ::float)
             (macroexpand-1 `(ss/some-spec ::int ::float))))
    (t/is (= [::int 123] (s/conform ::int-or-float 123)))
    (t/is (= [::float 1.23] (s/conform ::int-or-float 1.23)))
    (t/is (= (do ::s/invalid) (s/conform ::int-or-float ""))))
  (t/testing "assert!"
    (t/is (= 123 (ss/assert! ::int-or-float 123)))
    (t/is (= ::sentinel
             (try
               (ss/assert! ::int 1.23)
               (catch Exception e
                 ::sentinel)))))
  (t/testing "conform!"
    (t/is (= [::int 123] (ss/conform! ::int-or-float 123)))
    (t/is (= ::sentinel
             (try
               (ss/conform! ::int 1.23)
               (catch Exception e
                 ::sentinel)))))
  (t/testing "ns-keys"
    (t/is (= `(s/keys :gen :foo
                      :opt [:foo/bar :foo/baz]
                      :opt-un [:foo/bar :foo/baz]
                      :req [:foo/bar :foo/baz]
                      :req-un [:foo/bar :foo/baz])
             (macroexpand-1 `(ss/ns-keys foo
                                         :req [:bar :bar/baz]
                                         :req-un [:bar :bar/baz]
                                         :opt [:bar :bar/baz]
                                         :opt-un [:bar :bar/baz]
                                         :garbage :removed
                                         :gen :foo)))))
  (t/testing "ns-keys"
    (t/is (= `(s/keys* :gen :foo
                      :opt [:foo/bar :foo/baz]
                      :opt-un [:foo/bar :foo/baz]
                      :req [:foo/bar :foo/baz]
                      :req-un [:foo/bar :foo/baz])
             (macroexpand-1 `(ss/ns-keys* foo
                                         :req [:bar :bar/baz]
                                         :req-un [:bar :bar/baz]
                                         :opt [:bar :bar/baz]
                                         :opt-un [:bar :bar/baz]
                                         :garbage :removed
                                         :gen :foo))))))
