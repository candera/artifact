(ns artifact.api
  "Defines the JSON API used by clients to retrieve game state."
  (:use [clojure.contrib.json :only [json-str read-json]]
	artifact.tuplestore
	artifact.state
	artifact.game
	artifact.logging))

(defn- hoist-key
  "Takes a tuple of the form [[e a] v] and turns it into [e a v]."
  [tuple]
  (let [[[e a] v] tuple]
    [e a v]))

(defn api-get
  "Handles queries from the game client by returning the list of
  tuples that are visible to that client (based on the token provided
  in the query string)"
  [token]
  (dosync
   {:mime-type "application/json"
    :body (json-str
	   (map hoist-key (sort (get-visible-tuples @*game* token))))}))

(defn api-post
  "Handles posts from the game client, which should specify a tuple to
  assert."
  [token action]
  ;; TODO: Add checking of token
  ;; TODO: Handle errors in the input
  ;; TODO: Handle case where game state has moved on and action is
  ;; no longer available
  (debug "Asserted by " token " : " action)
  (dosync
   (let [player (lookup-player *game* token)]
    (alter *clock* inc)
    (alter *game* update-game *clock* player action)))
  ;; Return an empty string so that something gets rendered back to
  ;; the client.
  ;; TODO: Modify to return the new state
  "")