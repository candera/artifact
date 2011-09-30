
(defproject artifact "0.1.0-SNAPSHOT"
  :description "The server side of the game Artifact, an archaeology-themed board game. To run it, fire up the server and connect to it with a browser."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [ring/ring-jetty-adapter "0.3.11"]
                 [ring-json-params "0.1.3"]
                 [ring/ring-core "0.3.11"]
                 [compojure "0.6.5"]
                 [enlive "1.0.0"]]
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]]
  :main artifact.core
  ;; This next option has to do with Clojure bug #322. Something
  ;; about transitive AOT results in not enough going into the
  ;; uberjar, which means it won't run.
  :keep-non-project-classes true)
