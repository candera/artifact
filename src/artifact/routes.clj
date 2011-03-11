(ns artifact.routes
  "Defines the routes for the game."
  (:use compojure.core
	artifact.ui
	artifact.api)
   (:require [compojure.route :as route]))

(defroutes main-routes
  (GET "/" [] (to-html-str index))
  (POST "/join" [] join-page)
  ;; TODO: extract token validation into middleware?
  (GET "/game/:token" [token] (game-page token))
  (GET "/api" [since token] (api since token))
  (route/resources "/")
  (route/not-found "Page not found"))