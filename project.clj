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
  :git-dependencies [["https://github.com/brentonashworth/one.git"]
                     ["https://github.com/levand/domina.git"]
                     ["https://github.com/clojure/clojurescript.git"]]
  :hooks [leiningen.hooks.git-deps]
  :main artifact.core
  :extra-classpath-dirs [".lein-git-deps/clojurescript/src/clj"
                         ".lein-git-deps/clojurescript/src/cljs"
                         ".lein-git-deps/clojurescript/test/cljs"
                         ".lein-git-deps/domina/src/cljs"
                         ".lein-git-deps/one/src/app/clj"
                         ".lein-git-deps/one/src/app/cljs"
                         ".lein-git-deps/one/src/app/cljs-macros"
                         ".lein-git-deps/one/src/lib/clj"
                         ".lein-git-deps/one/src/lib/cljs"
                         ".lein-git-deps/one/test"
                         ".lein-git-deps/one/templates"])

