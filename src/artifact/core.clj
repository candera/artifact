(ns artifact.core
  (:use [ring.adapter.jetty :only (run-jetty)]
	[artifact.routes :only (main-routes)])
  (:require [compojure.handler :as handler])
  (:gen-class))

(def app
  (handler/site main-routes))

(defn -main [& args]
  (run-jetty app {:port 8080}))

(let [server (atom nil)
      set-server #(reset! server %)]
  (defn debug-start []
    (future (run-jetty (var app) {:port 8080 :configurator set-server})))
  (defn debug-stop []
    (.stop @server)))
