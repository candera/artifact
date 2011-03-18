(ns artifact.api
  "Defines the JSON API used by clients to retrieve game state."
  (:use [clojure.contrib.json :only [json-str read-json]]
	artifact.triplestore
	artifact.state
	artifact.game
	artifact.logging))

(defn- hoist-key
  "Takes a triple of the form [[e a] v] and turns it into [e a v]."
  [triple]
  (let [[[e a] v] triple]
    [e a v]))

(defn api-get
  "Handles queries from the game client by returning the list of
  triples that are visible to that client (based on the token provided
  in the query string)"
  [token]
  (dosync
   {:mime-type "application/json"
    :body (json-str
	   (map hoist-key (sort (get-visible-triples *store* token))))}))

(defn api-post
  "Handles posts from the game client, which should specify a set of
  triples to assert."
  [token triples]
  ;; TODO: Add checking of token
  ;; TODO: Handle errors in the input
  ;; TODO: Handle case where game state has moved on and action is
  ;; no longer available
  (debug "Asserted by " token " : " triples)
  (dosync (apply add-triples *store* triples))
  "")