(ns irresponsible.spectra-test
  (:require [irresponsible.spectra :as ss :include-macros true]
            [#?(:clj clojure.spec.alpha :cljs cljs.spec.alpha) :as s]
            #?(:clj  [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(s/def ::int integer?)
(s/def ::float float?)

(s/def ::int-or-float (ss/some-spec ::int ::float))

(t/deftest all-tests
  (t/testing "some-spec"
    (t/is (= `(s/or ::int ::int ::float ::float)
             (macroexpand-1 '(irresponsible.spectra/some-spec ::int ::float))))
    (t/is (= [::int 123] (s/conform ::int-or-float 123)))
    (t/is (= [::float 1.23] (s/conform ::int-or-float 1.23)))
    (t/is (= (do ::s/invalid) (s/conform ::int-or-float ""))))
  (t/testing "assert!"
    (t/is (= 123 (ss/assert! ::int-or-float 123)))
    (t/is (= ::sentinel
             (try
               (ss/assert! ::int 1.23)
               (catch #?(:clj Exception :cljs :default) e
                 ::sentinel)))))
  (t/testing "conform!"
    (t/is (= [::int 123] (ss/conform! ::int-or-float 123)))
    (t/is (= ::sentinel
             (try
               (ss/conform! ::int 1.23)
               (catch #?(:clj Exception :cljs :default) e
                   ::sentinel)))))
  (t/testing "ns-defs"
    (t/is (= `(do (s/def ::foo ::bar)
                  (s/def :foo/bar ::baz))
             (macroexpand-1 '(irresponsible.spectra/ns-defs "foo"
                               ::foo ::bar
                               :bar ::baz)))))
  (t/testing "only"
    (doseq [pass [{:a 123} {:a 123 :b 456} {:a 123 :b 456 :c 789}]]
      (t/testing pass
        (t/is ((ss/only :a :b :c) pass))))
    (doseq [fail [{:a 123 :b 456 :c 789 :d 101112}]]
      (t/testing fail
        (t/is (not ((ss/only [:a :b :c]) fail))))))
  (t/testing "ns-keys"
    (t/is (= `(s/keys :req-un [:foo/bar :bar/baz]
                      :req [:foo/bar :bar/baz]
                      :opt-un [:foo/bar :bar/baz]
                      :opt [:foo/bar :bar/baz]
                      :gen :foo)                       
             (macroexpand-1 '(irresponsible.spectra/ns-keys "foo"
                               :req [:bar :bar/baz]
                               :req-un [:bar :bar/baz]
                               :opt [:bar :bar/baz]
                               :opt-un [:bar :bar/baz]
                               :garbage :removed
                               :gen :foo)))))
  (t/testing "ns-keys*"
    (t/is (= `(s/keys* :req-un [:foo/bar :bar/baz]
                       :req [:foo/bar :bar/baz]
                       :opt-un [:foo/bar :bar/baz]
                       :opt [:foo/bar :bar/baz]
                       :gen :foo)                       
             (macroexpand-1 '(irresponsible.spectra/ns-keys* "foo"
                               :req [:bar :bar/baz]
                               :req-un [:bar :bar/baz]
                               :opt [:bar :bar/baz]
                               :opt-un [:bar :bar/baz]
                               :garbage :removed
                               :gen :foo)))))
  (t/testing "only-ns-keys"
    (t/is (= `(s/and (s/keys :req-un [:foo/req-foo :foo/req-bar]
                             :req [:foo/foo :foo/bar]
                             :opt-un [:foo/opt-foo :foo/opt-bar]
                             :opt [:opt/foo :opt/bar]
                             :gen :foo)
                     (ss/only :bar :foo :opt-bar :opt-foo :req-bar :req-foo :opt/bar :opt/foo))
             (macroexpand-1 '(irresponsible.spectra/only-ns-keys "foo"
                               :req-un [:req-foo :req-bar]
                               :req [:foo :bar]
                               :opt-un [:opt-foo :opt-bar]
                               :opt [:opt/foo :opt/bar]
                               :garbage :removed
                               :gen :foo)))))
  (t/testing "only-ns-keys*"
    (t/is (= `(s/and (s/keys* :req-un [:foo/req-foo :foo/req-bar]
                              :req [:foo/foo :foo/bar]
                              :opt-un [:foo/opt-foo :foo/opt-bar]
                              :opt [:opt/foo :opt/bar]
                              :gen :foo)
                     (ss/only :bar :foo :opt-bar :opt-foo :req-bar :req-foo :opt/bar :opt/foo))
             (macroexpand-1 '(irresponsible.spectra/only-ns-keys* "foo"
                               :req-un [:req-foo :req-bar]
                               :req [:foo :bar]
                               :opt-un [:opt-foo :opt-bar]
                               :opt [:opt/foo :opt/bar]
                               :garbage :removed
                               :gen :foo))))))

