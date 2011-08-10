(ns artifact.ui
  "Contains the HTML-y bits of the game."
  (:use [clojure.contrib.prxml :only (prxml *prxml-indent* *html-compatible*)]
        [clojure.contrib.json :only (json-str)]
        [ring.util.response :only (response)]
        compojure.core
        artifact.tuplestore
        artifact.game
        artifact.state
        artifact.error
        artifact.logging)
  (:refer-clojure :exclude [time]))

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
  ([token] (str "/api?token=" token)))

(defn to-html-str [& content]
  (binding [*prxml-indent* 2
            *html-compatible* true
            *out* (java.io.StringWriter.)]
    (doseq [e content] (prxml e))
    (.toString *out*)))

;;; Endpoints

(defn index [& messages]
  [:html
   [:head
    [:title "Artifact (Pre-Alpha)"]]
   [:body {:onload "document.join.name.focus()"}
    [:p "Welcome to artifact! Actual functionality still under development."]
    (map (fn [m] [:p {:class "flash"} m]) messages)
    [:p "Join a game by entering your name here."]
    [:form {:action "/join" :method "post" :name "join"}
     "Name:"
     [:input {:type "text" :name "name"}]
     [:input {:type "submit" :value "Join"}]]]])

(defn game-page [token]
  (dosync
   (let [player-id (lookup-player @*game* token)
         player-name (lookup-player-name @*game* player-id)]
     (response
      (to-html-str
       [:doctype! "html"]
       [:html
        [:head
         [:title "Artifact (Pre-Alpha)"]
         [:link {:rel "stylesheet" :type "text/css" :href "/styles/game.css"}]
         [:link {:rel "stylesheet" :type "text/css" :href "/styles/sunny/jquery-ui-1.8.13.custom.css"}]

         ;; JQuery and related plugins
         ;; Empty string in script tag is to get the closing tag to
         ;; show up, since the validator complains otherwise.
         [:script {:src "/script/jquery.min-1.5.1.js"} ""]
         [:script {:src "/script/jquery.timers-1.2.js"} ""]
         [:script {:src "/script/jquery-ui-1.8.13.custom.min.js"} ""]

         [:script
          ;; URL for retrieving game state
          [:raw! (str "var gameStateUrl='" (state-url token) "';")]]
         [:script {:src "/script/game.js"} ""]]
        [:body
         [:div {:id "setup-ui"}
          [:button {:id "start-game"
                    :onclick "javascript:startGame()"
                    :disabled "disabled"}
           "Start game!"]
          [:table {:id "joined-players"}
           [:tr [:th "Player"] [:th "State"]] ""]]
         [:div {:id "playing-ui"}
          [:div {:id "playing-tabs"}
           [:ul
            [:li [:a {:href "#ma-board"} "Major Action Board"]]
            [:li [:a {:href "#academy-board"} "Academy Board"]]]
           [:div {:id "ma-board"} ""]
           [:div {:id "academy-board"} ""]]]
         [:textarea {:id "gameState" :readonly "readonly" :rows 20}
          "diagnostic information is displayed here"]]])))))

(def ^{:private true} error-messages
  {:artifact.game/cannot-add-more-players "The game is already full."})

(defn join-page [player-name]
  (app-try
   (dosync
    (alter *game* update-game nil [nil "game" "new-player" player-name])
    (let [token (last (query-values @*game* [:any #"player:.*" "token" :any]))]
      {:status 303
       :headers {"Location" (str "/game/" token)}}))
   (app-catch e
              (debug "Error when" player-name "tried to join game:" e)
              (index (get error-messages e "Unrecognized error")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def test-style
  "div {
    margin: 3px;
    overflow: auto;
}
.section {
    background: none repeat scroll 0 0 rgba(255, 255, 0, 0.1);
    width: 800px;
}
.option {
    border: 2px solid black;
    display: inline;
    float: left;
    padding-bottom: 2px;
    width: 123px;
}
.selection {
    display: block;
}
.selection-box {
    border: 2px solid black;
    border-radius: 6px 6px 6px 6px;
    display: inline-block;
    padding: 1px;
    vertical-align: middle;
    width: 13px;
}
.selection-area {
    background: none repeat scroll 0 0 blue;
    border-radius: 4px 4px 4px 4px;
    height: 15px;
    margin: auto;
}
.selection-text {
    vertical-align: middle;
}
h2 {
    background: none repeat scroll 0 0 yellow;
    color: #666666;
    margin: 0;
    text-align: center;
    width: 100%;
    font-size: small;
}
h1 {
}
")

(def test-options
  ["Politic"
   "Leverage Departmental politics to become the Dean's favorite"
   ["1" "2" "3"]

   "Submit Grant"
   "Submit a research grant to receive more funding"
   ["$5" "$3" "Roll ($1-6)"]

   "Dig"
   "Trade in Resource Points to pull cards from the Dig Site"
   ["Artifact Card" "Artifact Card"]

   "Teach"
   ""
   ["Collect 1 RA"]

   "Scheme"
   ""
   ["Scheme" "Scheme"]

   "Explore"
   ""
   ["Explore" "Explore"]])

(defn- selection [selection-text]
   [:div {:class "selection"}
    [:div {:class "selection-box"}
     [:div {:class "selection-area"} ""]]
    [:span {:class "selection-text"} selection-text]])

(defn- option [option]
  (let [[title description selections] option]
   [:div {:class "option"}
    [:h2 title]
    (map selection selections)]))

(defn test-page []
  (to-html-str
   [:html
    [:head
     [:title "Test page"]
     [:style test-style]]
    [:body
     [:div {:class "section"}
      [:h1 "Major Actions"]
      (map option (partition 3 test-options))]]]))

