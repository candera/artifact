(ns artifact.core
  (:use [ring.adapter.jetty :only (run-jetty)]
	[ring.middleware.params]
	[ring.middleware.multipart-params]
	[artifact.game :only (initialize-game)]
	[artifact.state :only (*store*)]
	[clojure.contrib.json :only (read-json)]
	compojure.core
	artifact.ui
	artifact.api
	artifact.logging
	artifact.triplestore)
  (:require [compojure.handler :as handler]
	    [compojure.route :as route])
  (:gen-class))

(dosync (initialize-game *store*))

;; (def app
;;   (handler/site main-routes))

;;; Set things up so that we can use break when we're under swank
(when (resolve 'swank.core.connection/*current-connection*)
  (eval
   '(do
      (def swank-connection swank.core.connection/*current-connection*)
      (defmacro break []
	`(binding [swank.core.connection/*current-connection* swank-connection]
		  (swank.core/break))))))

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
  (GET "/" [] (to-html-str index))
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
  (run-jetty app {:port 8080}))

(let [server (atom nil)
      set-server #(reset! server %)]
  (defn debug-start []
    (future (run-jetty (var app) {:port 8080 :configurator set-server})))
  (defn debug-stop []
    (.stop @server)))

(defn reset-game []
  (dosync
   (reset-triplestore *store*)
   (initialize-game *store*)))
