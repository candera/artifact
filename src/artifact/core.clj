(ns artifact.core
  (:use [ring.adapter.jetty :only (run-jetty)]
	[ring.middleware.json-params]
	[artifact.routes :only (main-routes)]
	[artifact.game :only (initialize-game)]
	[artifact.state :only (*store*)])
  (:require [compojure.handler :as handler])
  (:gen-class))

(initialize-game *store*)

;; (def app
;;   (handler/site main-routes))

(def app
  (-> main-routes
      wrap-json-params
      wrap-params))

(defn -main [& args]
  (run-jetty app {:port 8080}))

(let [server (atom nil)
      set-server #(reset! server %)]
  (defn debug-start []
    (future (run-jetty (var app) {:port 8080 :configurator set-server})))
  (defn debug-stop []
    (.stop @server)))
