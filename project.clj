(defproject artifact "0.1.0-SNAPSHOT"
  :description "The server side of the game Artifact, an archaeology-themed board game. To run it, fire up the server and connect to it with a browser."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-jetty-adapter "1.0.1"]
                 [ring/ring-core "1.0.1"]
                 [ring "1.0.1"]         ; TODO: Do I need this and the
                                        ; other Ring libraries?
                 [org.clojure/data.json "0.1.1"]
                 [compojure "1.0.0"]
                 [enlive "1.0.0"]]
  :dev-dependencies [[jline "0.9.94"]
                     [marginalia "0.7.0-SNAPSHOT"]
                     [lein-marginalia "0.7.0-SNAPSHOT"]]
  :main artifact.core
  ;; This next option has to do with Clojure bug #322. Something
  ;; about transitive AOT results in not enough going into the
  ;; uberjar, which means it won't run.
  :source-path "src/app/clj")
