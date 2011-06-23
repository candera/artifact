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

(def demo-game
  [[0 "game" "phase" "setup"]
   [1 "game" "action" [nil "game" "new-player" "Craig"]]
   [1 "game" "actor" nil]
   [1 "player:1" "self" true]
   [1 "player:1" "name" "Craig"]
   [1 "player:1" "token" "398164494"]
   [1 "player:1" "money" 3]
   [1 "player:1" "pieces"
    ["professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5"]]
   [1 "player:1" "ready" false]
   [1 "player:1" "icon" "/images/professor-blue.png"]
   [1 "ra:1" "location" "research-bar-ready"]
   [1 "game" "players" ["player:1"]]
   [1 "player:1" "available-actions" [[nil "player:1" "ready" true]]]
   [2 "game" "action" [nil "player:1" "ready" true]]
   [2 "game" "actor" "player:1"]
   [2 "player:1" "ready" true]
   [2 "player:1" "available-actions" []]
   [3 "game" "action" [nil "game" "new-player" "Alice"]]
   [3 "game" "actor" nil]
   [3 "player:2" "self" true]
   [3 "player:2" "name" "Alice"]
   [3 "player:2" "token" "891533391"]
   [3 "player:2" "money" 3]
   [3 "player:2" "pieces"
    ["professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5"]]
   [3 "player:2" "ready" false]
   [3 "player:2" "icon" "/images/professor-red.png"]
   [3 "ra:1" "location" "research-bar-ready"]
   [3 "game" "players" ["player:1" "player:2"]]
   [3 "player:1" "available-actions" []]
   [3 "player:2" "available-actions" [[nil "player:2" "ready" true]]]
   [4 "game" "action" [nil "player:2" "ready" true]]
   [4 "game" "actor" "player:2"]
   [4 "player:2" "ready" true]
   [4 "player:1" "available-actions" []]
   [4 "player:2" "available-actions" []]
   [5 "game" "action" [nil "game" "new-player" "Ellen"]]
   [5 "game" "actor" nil]
   [5 "player:3" "self" true]
   [5 "player:3" "name" "Ellen"]
   [5 "player:3" "token" "533196904"]
   [5 "player:3" "money" 3]
   [5 "player:3" "pieces"
    ["professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5"]]
   [5 "player:3" "ready" false]
   [5 "player:3" "icon" "/images/professor-green.png"]
   [5 "ra:1" "location" "research-bar-ready"]
   [5 "game" "players" ["player:1" "player:2" "player:3"]]
   [5 "player:1" "available-actions" []]
   [5 "player:2" "available-actions" []]
   [5 "player:3" "available-actions" [[nil "player:3" "ready" true]]]
   [6 "game" "action" [nil "player:3" "ready" true]]
   [6 "game" "actor" "player:3"]
   [6 "player:3" "ready" true]
   [6 "player:1" "available-actions" [[nil "game" "phase" "playing"]]]
   [6 "player:2" "available-actions" [[nil "game" "phase" "playing"]]]
   [6 "player:3" "available-actions" [[nil "game" "phase" "playing"]]]
   [7 "game" "action" [nil "game" "new-player" "Susa"]]
   [7 "game" "actor" nil]
   [7 "player:4" "self" true]
   [7 "player:4" "name" "Susa"]
   [7 "player:4" "token" "149784895"]
   [7 "player:4" "money" 3]
   [7 "player:4" "pieces"
    ["professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5"]]
   [7 "player:4" "ready" false]
   [7 "player:4" "icon" "/images/professor-yellow.png"]
   [7 "ra:1" "location" "research-bar-ready"]
   [7 "game" "players" ["player:1" "player:2" "player:3" "player:4"]]
   [7 "player:1" "available-actions" []]
   [7 "player:2" "available-actions" []]
   [7 "player:3" "available-actions" []]
   [7 "player:4" "available-actions" [[nil "player:4" "ready" true]]]
   [8 "game" "action" [nil "player:4" "ready" true]]
   [8 "game" "actor" "player:4"]
   [8 "player:4" "ready" true]
   [8 "player:1" "available-actions" [[nil "game" "phase" "playing"]]]
   [8 "player:2" "available-actions" [[nil "game" "phase" "playing"]]]
   [8 "player:3" "available-actions" [[nil "game" "phase" "playing"]]]
   [8 "player:4" "available-actions" [[nil "game" "phase" "playing"]]]
   [9 "game" "action" [nil "game" "phase" "playing"]]
   [9 "game" "actor" "player:4"]
   [9 "game" "phase" "playing"]
   [9 "player:1" "available-actions" [[nil "game" "phase" "playing"]]]
   [9 "player:2" "available-actions" [[nil "game" "phase" "playing"]]]
   [9 "player:3" "available-actions" [[nil "game" "phase" "playing"]]]
   [9 "player:4" "available-actions" [[nil "game" "phase" "playing"]]]])

(dosync (ref-set *game* demo-game))

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
  (POST "/test" [token] "test"))

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
