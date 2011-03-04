(ns artifact.core
  (:use [ring.adapter.jetty :only (run-jetty)]
	[compojure.core]
	[clojure.contrib.prxml :only (prxml *prxml-indent* *html-compatible*)])
  (:require [compojure.route :as route]
	    [compojure.handler :as handler])
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
    [:p "Welcome to artifact! Actual functionality still under development."]
    [:form {:action "/api/join" :method "post"}
     "Name:"
     [:input {:type "text" :name "name"}]
     [:input {:type "submit" :value "Join"}]]]])

(defroutes main-routes
  (GET "/" [] (to-html-str index))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))

(defn -main [& args]
  (run-jetty app {:port 8080}))
