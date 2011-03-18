(ns artifact.routes
  "Defines the routes for the game."
  (:use compojure.core
	artifact.ui
	artifact.api)
   (:require [compojure.route :as route]))

;;; Set things up so that we can use break when we're under swank
(when (resolve 'swank.core.connection/*current-connection*)
  (eval '(do (def swank-connection swank.core.connection/*current-connection*)
	     (defmacro break []
	       `(binding [swank.core.connection/*current-connection* swank-connection]
		  (swank.core/break))))))

(defroutes main-routes
  (GET "/" [] (to-html-str index))
  (POST "/join" [] join-page)
  ;; TODO: extract token validation into middleware?
  (GET "/game/:token" [token] (game-page token))
  (GET "/api" [token] (api-get token))
  (POST "/api" req (do (println "body:" (slurp (:body req))) "")
	;; (fn [req] (println "Body:" (class (:body req)))
	;; 	 (println "slurped body:" (slurp (:body req)))
	;; 	 (api-post token (slurp (:body req))))
	)
  (route/resources "/")
  (route/not-found "Page not found"))
