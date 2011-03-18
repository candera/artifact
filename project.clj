(defproject artifact "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [ring/ring-jetty-adapter "0.3.7"]
		 [ring-json-params "0.1.3"]
		 [ring-core "0.3.7"]
		 [compojure "0.6.1"]]
  :dev-dependencies [[swank-clojure "1.2.0"]]
  :main artifact.core
  ;; This next option has to do with Clojure bug #322. Something
  ;; about transitive AOT results in not enough going into the
  ;; uberjar, which means it won't run.
  :keep-non-project-classes true)
