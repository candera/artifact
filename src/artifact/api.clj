(ns artifact.api
  "Defines the JSON API used by clients to retrieve game state."
  (:use [clojure.data.json :only [json-str read-json]]
        artifact.tuplestore
        artifact.state
        artifact.game
        artifact.logging
        artifact.error)
  (:refer-clojure :exclude [time]))

(defn api-get
  "Handles queries from the game client by returning the list of
  tuples that are visible to that client (based on the token provided
  in the query string)"
  [token]
  (dosync
   {:mime-type "application/json"
    :body (json-str
           (get-visible-tuples (coalesce @*game*) token))}))

;; Contains the list of error messages to send back to the client,
;; based on what's thrown by the app.
(def ^{:private true} error-map
  {:artifact.game/cannot-add-more-players
   "You cannot join the game because there would be too many players"})

(defn api-post
  "Handles posts from the game client, which should specify a tuple to
  assert."
  [token action]
  ;; TODO: Add checking of token
  ;; TODO: Handle errors in the input
  ;; TODO: Handle case where game state has moved on and action is
  ;; no longer available
  (debug "Asserted by " token " : " action)
  (app-try
   (dosync
    (let [player (lookup-player @*game* token)]
      (alter *game* update-game player action))
    ;; Return an empty string so that something gets rendered back to
    ;; the client.
    ;; TODO: Modify to return the new state
    "")
   (app-catch e
              (do (debug "Error when updating game state: " e))
              {:status 400
               :body (get error-map e "Unanticipated error")})))