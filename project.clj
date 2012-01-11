(defproject artifact "0.1.0-SNAPSHOT"
  :description "The server side of the game Artifact, an archaeology-themed board game. To run it, fire up the server and connect to it with a browser."
  :dependencies [;; Unfortunately, 1.3.0 doesn't work. Something,
                 ;; somewhere, is still using clojure.contrib, and
                 ;; it's binding something that isn't declared
                 ;; dynamic. I think it's something in swank-clojure,
                 ;; but I'm not sure.
                 [org.clojure/clojure "1.3.0"]
                 [ring/ring-jetty-adapter "1.0.1"]
                 [ring/ring-core "1.0.1"]
                 [org.clojure/data.json "0.1.1"]
                 [compojure "1.0.0"]
                 [enlive "1.0.0"]]
  :main artifact.core
  ;; This next option has to do with Clojure bug #322. Something
  ;; about transitive AOT results in not enough going into the
  ;; uberjar, which means it won't run.
  :keep-non-project-classes true)
