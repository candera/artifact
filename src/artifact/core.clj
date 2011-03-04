(ns artifact.core
  (:use [ring.adapter.jetty :only (run-jetty)]
	[clojure.contrib.prxml :only (prxml *prxml-indent* *html-compatible*)])
  (:gen-class))

(defn- to-html-str [content]
  (binding [*prxml-indent* 2
	    *html-compatible* true
	    *out* (java.io.StringWriter.)]
    (prxml content)
    (.toString *out*)))

(def ^{:private true} index
  [:html
   [:head
    [:title "Artifact (Pre-Alpha)"]]
   [:body
    "Welcome to artifact! Actual functionality still under development."]])

(defn- app [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (to-html-str index)})

(defn -main [& args]
  (run-jetty app {:port 8080}))
