(ns artifact.core
  (:use [ring.adapter.jetty :only (run-jetty)]
	[ring.util.response :only (response)]
	[compojure.core]
	[clojure.contrib.prxml :only (prxml *prxml-indent* *html-compatible*)]
	[clojure.contrib.json :only (json-str)]
	[artifact.triplestore])
  (:require [compojure.route :as route]
	    [compojure.handler :as handler])
  (:gen-class))

(def ^{:private true :dynamic true} *store* (create-triplestore))

(defn- to-html-str [& content]
  (binding [*prxml-indent* 2
	    *html-compatible* true
	    *out* (java.io.StringWriter.)]
    (doseq [e content] (prxml e))
    (.toString *out*)))

(def ^{:private true} index
  [:html
   [:head
    [:title "Artifact (Pre-Alpha)"]]
   [:body
    [:p "Welcome to artifact! Actual functionality still under development."]
    [:p "Join a game by entering your name here."]
    [:form {:action "/join" :method "post"}
     "Name:"
     [:input {:type "text" :name "name"}]
     [:input {:type "submit" :value "Join"}]]]])

(defn- request-dump [req]
  [:div {:class "request-dump"}
   [:h4 "Diagnostic info (request dump)"]
   [:p "Request Parameters :"]
   [:table
    [:tr [:th "Name"] [:th "Value"]]
    (map (fn [[k v]] [:tr [:td (str k)] [:td (str v)]]) (:params req))]
   [:p "Raw request map: " (str (:params req))]])

(defn- next-player-id []
  (let [player-ids (set (get-triple-value *store* "game" "players"))]
    (first
     (filter 
      #(not (player-ids %))
      (map #(str "player:" %) (iterate inc 1))))))

(defn- state-url
  "Retrieve the URL that the client can use to get the state of the
game."
  ([token] (str "/api?&token=" token)))

(defn- players
  "Given a store, returns a sequence of players in that store."
  [store]
  (or (get-triple-value store "game" "players") []))

(defn- add-player [store name]
  (let [token (str (rand-int 1000000000))
	id (next-player-id)]
    (add-triples store
     [id "self" true]
     [id "name" name]
     [id "token" token]
     ["game" "players" (conj (players store) id)])
    id))

(defn- lookup-player
  "Given a token, return a player id."
  [token]
  (->> (query *store* ["*" "token" token])
       (first)
       (entity)))

(defn- lookup-token
  "Given a player id, return the player's token"
  [id]
  (get-triple-value *store* id "token"))

(defn- lookup-player-name
  "Given a player id, return the player's name"
  [id]
  (get-triple-value *store* id "name"))

(defn- game-page [token]
  (let [player-id (lookup-player token)
	player-name (lookup-player-name player-id)]
   (response
    (to-html-str
     [:doctype! "html"]
     [:html
      [:head
       [:title "Artifact (Pre-Alpha)"]
       [:link {:rel "stylesheet" :type "text/css" :href "/styles/game.css"}]

       ;; JQuery
       ;; Empty string in script tag is to get the closing tag to
       ;; show up, since the validator complains otherwise.
       [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/1.5.1/jquery.min.js"} ""]

       [:script
	;; URL for retrieving game state
	[:raw! (str "var gameStateUrl='" (state-url token) "';")]]
       [:script {:src "/script/game.js"} ""]]
      [:body
       [:p "You have joined, "
	(or player-name "<No name>")]
       [:button {:onclick "updateGameState()"} "Update game state"]
       [:textarea {:id "gameState" :readonly "readonly"}
	"game state will go here"]]]))))

(defn- join-page [req]
  (let [player-name (:name (:params req))
	player-id (add-player *store* player-name)
	token (lookup-token player-id)]
    {:status 303
     :headers {"Location" (str "/game/" token)}}))

(defn- hoist-key
  "Takes a triple of the form [[e a] v] and turns it into [e a v]."
  [triple]
  (let [[[e a] v] triple]
    [e a v]))

(defn- api [since token]
  {:mime-type "application/json"
   :body (json-str
	  (map hoist-key (get-all-triples *store*)))})

(defroutes main-routes
  (GET "/" [] (to-html-str index))
  (POST "/join" [] join-page)
  (GET "/game/:token" [token] (game-page token))
  ;; TODO: extract token validation into middleware?
  (GET "/api" [since token] (api since token))
  (route/resources "/")
  (route/not-found "Page not found"))

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
