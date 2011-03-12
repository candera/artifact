(ns artifact.ui
  "Contains the HTML-y bits of the game."
  (:use [clojure.contrib.prxml :only (prxml *prxml-indent* *html-compatible*)]
	[clojure.contrib.json :only (json-str)]
	[ring.util.response :only (response)]
	compojure.core
	artifact.game
	artifact.state))

;;; Helpers

(defn- request-dump [req]
  [:div {:class "request-dump"}
   [:h4 "Diagnostic info (request dump)"]
   [:p "Request Parameters :"]
   [:table
    [:tr [:th "Name"] [:th "Value"]]
    (map (fn [[k v]] [:tr [:td (str k)] [:td (str v)]]) (:params req))]
   [:p "Raw request map: " (str (:params req))]])

(defn- state-url
  "Retrieve the URL that the client can use to get the state of the
game."
  ([token] (str "/api?&token=" token)))

(defn to-html-str [& content]
  (binding [*prxml-indent* 2
	    *html-compatible* true
	    *out* (java.io.StringWriter.)]
    (doseq [e content] (prxml e))
    (.toString *out*)))

;;; Endpoints

(def index
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

(defn game-page [token]
  (let [player-id (lookup-player *store* token)
	player-name (lookup-player-name *store* player-id)]
   (response
    (to-html-str
     [:doctype! "html"]
     [:html
      [:head
       [:title "Artifact (Pre-Alpha)"]
       [:link {:rel "stylesheet" :type "text/css" :href "/styles/game.css"}]

       ;; JQuery and related plugins
       ;; Empty string in script tag is to get the closing tag to
       ;; show up, since the validator complains otherwise.
       [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/1.5.1/jquery.min.js"} ""]
       [:script {:src "/script/jquery.timers-1.2.js"} ""]

       [:script
	;; URL for retrieving game state
	[:raw! (str "var gameStateUrl='" (state-url token) "';")]]
       [:script {:src "/script/game.js"} ""]]
      [:body
       [:p "The following players have joined:"
	(or player-name "<No name>")]
       [:div {:id "joining-ui"}
	"UI for joining up will be shown here."
	[:table {:id "joined-players"}]]
       [:textarea {:id "gameState" :readonly "readonly" :rows 20}
	"game state will go here"]]]))))

(defn join-page [req]
  (let [player-name (:name (:params req))
	player-id (add-player *store* player-name)
	token (lookup-token *store* player-id)]
    {:status 303
     :headers {"Location" (str "/game/" token)}}))



