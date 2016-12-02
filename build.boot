(set-env!
  :project 'irresponsible/spectra
  :version "0.1.0"
  :resource-paths #{"src"}
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha14"]
                  [org.clojure/clojurescript "1.9.293" :scope "test"]
                  [adzerk/boot-test        "1.1.2"     :scope "test"]
                  [adzerk/boot-cljs        "1.7.228-1" :scope "test"]])

(require '[adzerk.boot-test :as t])

(task-options!
 pom {:project (get-env :project)
      :version (get-env :version)}
 target {:dir #{"target"}})

(deftask testing []
  (set-env! :source-paths  #(conj % "test")
            :resource-paths #(conj % "test"))
  identity)

(deftask test []
  (testing)
  (t/test))
