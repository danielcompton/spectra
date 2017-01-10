The irresponsible clojure guild presents...

# Spectra

Some tools for working with specs (clj/s)

Status: pre-alpha, be prepared for stuff to break

## Usage

```clojure
(ns my.ns
 (:require [irresponsible.spectra :as ss]
           [#?(:clj clojure.spec :cljs cljs.spec) :as s]))

;;; first off, let's demonstrate a sum type (this OR this)

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

;; Now something a little bit more exotic

(s/def :fake.json.ns/foo int?)
(s/def :fake.json.ns/bar string?)
;;; ns-keys is like clojure.spec/keys, but takes a namespace to apply
;;; to keywords that do not have a namespace already
;;; There is a corresponding keys* which returns a regex spec like s/keys*
(s/def ::json (ss/ns-keys fake.json.ns :opt-un [:foo :bar]))

(def fake-json {:foo 123 :bar ""})
(assert! ::json fake-json) ;; hooray!
```

## Copyright and License

MIT LICENSE

Copyright (c) 2016 James Laver

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

