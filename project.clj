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
                     [lein-marginalia "0.7.0-SNAPSHOT"]
                     [robert/hooke "1.1.2"]]
  :git-dependencies [["todo"]]
  :hooks [leiningen.hooks.git-deps]
  :main artifact.core
  :extra-classpath-dirs ["lib/clojurescript/src/clj"
                         "lib/clojurescript/src/cljs"
                         "lib/clojurescript/test/cljs"
                         "lib/domina/src/cljs"
                         "lib/one/src/app/clj"
                         "lib/one/src/app/cljs"
                         "lib/one/src/app/cljs-macros"
                         "lib/one/src/lib/clj"
                         "lib/one/src/lib/cljs"
                         "lib/one/test"
                         "lib/one/templates"])

