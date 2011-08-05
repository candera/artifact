(ns artifact.core
  (:use [ring.adapter.jetty :only (run-jetty)]
        [ring.middleware.params]
        [ring.middleware.multipart-params]
        [artifact.game :only (new-game)]
        [artifact.state :only (*game*)]
        [clojure.contrib.json :only (read-json)]
        compojure.core
        artifact.ui
        artifact.api
        artifact.logging
        artifact.tuplestore)
  (:require [compojure.handler :as handler]
            [compojure.route :as route])
  (:gen-class)
  (:refer-clojure :exclude [time]))

(dosync (ref-set *game* (new-game)))

(defroutes api-routes
  ;; TODO: extract token validation into middleware?
  (GET "/api" [token] (api-get token))
  (POST "/api" [token]
        (fn [req]
          (let [body (slurp (:body req))]
            (debug "Body is:" body)
            (debug "token is:" token)
            (api-post token (read-json body false))))))

(defroutes test-routes
  (GET "/test" [] (test-page)))

(defroutes page-routes
  (GET "/" [] (to-html-str (index)))
  (POST "/join" [name] (join-page name))
  (GET "/game/:token" [token] (game-page token)))

(defroutes default-routes
  (route/resources "/")
  (route/not-found "Page not found"))

(defroutes all-routes
  api-routes
  test-routes
  (handler/site page-routes)
  default-routes)

(def app (wrap-params all-routes))

(defn -main [& args]
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
    (run-jetty app {:port port})))

(let [server (atom nil)
      set-server #(reset! server %)]
  (defn debug-start []
    (future (run-jetty (var app) {:port 8080 :configurator set-server})))
  (defn debug-stop []
    (.stop @server)))

(defn reset-game []
  (dosync
   (ref-set *game* (new-game))))
