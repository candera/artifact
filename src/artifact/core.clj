(ns artifact.core
  (:use [ring.adapter.jetty :only (run-jetty)]
	[ring.util.response :only (response)]
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
    [:p "Join a game by entering your name here."]
    [:form {:action "/game" :method "post"}
     "Name:"
     [:input {:type "text" :name "name"}]
     [:input {:type "submit" :value "Join"}]]]])

(defn- request-dump [req]
  [:div {:class "request-dump"}
   [:p "Params :"]
   [:table
    [:tr [:th "Name"] [:th "Value"]]
    (map (fn [[k v]] [:tr [:td (str k)] [:td (str v)]]) (:params req))]
   [:p "Raw: " (str (:params req))]])

(defn- game-page [req]
  (response
   (to-html-str
    [:html
     [:head
      [:title "Artifact (Pre-Alpha)"]
      [:body
       [:p "You have joined, "
	(get (:params req) :name "<No name>")]
       (request-dump req)]]])))

(defroutes main-routes
  (GET "/" [] (to-html-str index))
  (POST "/game" [] game-page)
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))

(defn -main [& args]
  (run-jetty app {:port 8080}))
