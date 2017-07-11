[![Clojars Project](https://img.shields.io/clojars/v/irresponsible/spectra.svg)](https://clojars.org/irresponsible/spectra)
[![Build Status](https://travis-ci.org/irresponsible/spectra.svg?branch=master)](https://travis-ci.org/irresponsible/spectra)

The irresponsible clojure guild presents...

# Spectra

The missing toolkit for clojure.spec/cljs.spec

Status: new, but we're using it without problems

## Rationale

Using spec to get the guarantees you want of your code often requires
much boilerplate. This library removes that boilerplate for a few
common cases I've tripped over.

## Usage

If you're using clojure 1.8, you will need [clojure-future-spec](https://github.com/tonsky/clojure-future-spec)

```clojure
(ns my.ns
 (:require [irresponsible.spectra :as ss]
           [#?(:clj clojure.spec.alpha :cljs cljs.spec.alpha) :as s]))

(s/def ::foo string?)
(s/def ::bar number?)
(s/def ::baz (ss/some-spec ::foo ::bar)) ;; => `(s/or ::foo ::foo ::bar ::bar)

;;; we'll use this with conform!, which is like conform but throws with helpful
;;; info in the metadata when it fails. We use core.match

(let [[k v] (ss/conform! ::baz "foo")]
  (case k ;; because you just get back the original spec name, you could recurse!
    ::foo (prn "got a foo" v)
	::bar (prn "got a bar" v)))

;;; assert! will also throw on fail, returns input, not conformation

(when (ss/assert! :baz "foo")
  (print "yay!")) ;; assert! can not be disabled like spec/assert can

;; Now I'll show you how spectra makes using spec with json less painful!
;; Firstly, we can use a fake namespace to represent the json
;; ns-defs will prepend the given namespace to keywords that don't have them

(ss/ns-defs "fake.json.ns"
  :foo integer?
  :bar string?) ; => (do (s/def :fake.json.ns/foo integer?) (s/def :fake.json.ns/bar string?))

;;; ns-keys is like clojure.spec/keys, but takes a namespace to apply
;;; to keywords that do not have a namespace already
;;; There is a corresponding keys* which returns a regex spec like s/keys*
(s/def ::json (ss/ns-keys "fake.json.ns" :opt-un [:foo :bar]))

;; now we can use it!
(def fake-json {:foo 123 :bar ""})
(ss/assert! ::json fake-json) ;; hooray!

;; ::json2 is like ::json, except it will not permit unrecognised keys
;; there is also `only-ns-keys*` if you require a regex spec be returned
(s/def ::json2 (ss/only-ns-keys "fake.json.ns" :opt-un [:foo :bar]))

;; we can also have this 'only' functionality without specifying specs for the keys
(s/def ::my-map (ss/only :a :b :c)) ;; accepts {} but not {:a 1 :b 2 :c 3 :d 4}
```

## Copyright and License

MIT LICENSE

Copyright (c) 2016 James Laver

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

