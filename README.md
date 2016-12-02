The irresponsible clojure guild presents...

# Spectra

Some tools for working with specs (clj/s)

Status: pre-alpha, be prepared for stuff to break

## Usage

```clojure
(ns my.ns
 (:require [irresponsible.spectra :as ss]
           [#?(:clj clojure.spec :cljs cljs.spec) :as s]))

;; first off, let's demonstrate a sum type

(s/def ::foo string?)
(s/def ::bar number?)
(s/def ::baz (ss/some-spec ::foo ::bar)) ;; => `(s/or ::foo ::foo ::bar ::bar)

(match (ss/conform! ::baz "foo") ;; conform! will throw if conform fails
  [::foo s] ...  ;; of course, these specs are fully qualified, so you
  [::bar s] ...) ;; could just pass them off to another function


(when (ss/assert! :baz "foo") ;; assert! will also throw on fail, returns input, not conformation
  (print "yay!")) ;; assert! can not be disabled like spec/assert can
```

## Copyright and License

MIT LICENSE

Copyright (c) 2016 James Laver

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

