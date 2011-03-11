(ns artifact.game
  "Implements the logic of the game"
  (:use artifact.triplestore))

(defn- players
  "Given a store, returns a sequence of players in that store."
  [store]
  (or (get-triple-value store "game" "players") []))

(defn- next-player-id
  "Return the next available player id in the specified store."
  [store]
  (let [player-ids (set (get-triple-value store "game" "players"))]
    (first
     (filter 
      #(not (player-ids %))
      (map #(str "player:" %) (iterate inc 1))))))

(defn lookup-token
  "Given a player id and a store, return the player's token."
  [store id]
  (get-triple-value store id "token"))

(defn lookup-player-name
  "Given a player id and a store, return the player's name"
  [store id]
  (get-triple-value store id "name"))

(defn lookup-player
  "Given a token and a store, return the corresponding player id from
  that store."
  [store token]
  (->> (query store ["*" "token" token])
       (first)
       (entity)))

(defn add-player
  "Update the store to include a new player. Return the identity of
  the new player."
  [store name]
  (let [token (str (rand-int 1000000000))
	id (next-player-id store)]
    (add-triples store
     [id "self" true]
     [id "name" name]
     [id "token" token]
     ["game" "players" (conj (players store) id)])
    id))
