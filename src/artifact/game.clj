(ns artifact.game
  "Implements the logic of the game"
  (:use artifact.tuplestore
        artifact.util
        artifact.error)
  (:refer-clojure :exclude [time]))

(defn- players
  "Given a game, returns a sequence of players in that game."
  [game]
  (or (get-latest-value game "game" "players") []))

(defn- pieces
  "Given a game, returns a sequence of pieces in that game."
  [game]
  (or (get-latest-value game "game" "pieces") []))

(defn- entities
  "Return a seq of entity ids"
  [prefix]
  (map (fn [n] (str prefix ":" n)) (iterate inc 1)))

(defn- next-entity-ids
  "Returns the next n available ids for the specified entity in the specified game"
  [game owning-entity owning-attribute prefix n]
  (let [entity-ids (set (get-latest-value game owning-entity owning-attribute))]
    (take n
          (filter
           #(not (entity-ids %))
           (entities prefix)))))

(defn- next-entity-id
  "Return the next available id for the specified entity in the specified game"
  [game owning-entity owning-attribute prefix]
  (first (next-entity-ids game owning-entity owning-attribute prefix 1)))

(defn- next-player-id
  "Return the next available player id in the specified game."
  [game]
  (next-entity-id game "game" "players" "player"))


(defn- next-professor-id
  "Return the next available professor id in the specified game."
  [game]
  (next-entity-id game "game" "pieces" "professor"))

(defn- next-ra-ids
  "Return the next n ra ids in the specified game."
  [game n]
  (next-entity-ids game "game" "pieces" "ra" n))

(defn lookup-token-by-id
  "Given a player id and a game, return the player's token."
  [game id]
  (get-latest-value game id "token"))

(defn lookup-tokens-by-name
  "Given a player name and a game, return a sequence of tokens for
players with that name."
  [game name]
  (let [player-ids (map entity (query game [#"player:.*" "name" name]))]
    (map #(get-latest-value game % "token") player-ids)))

(defn lookup-player-name
  "Given a player id and a game, return the player's name"
  [game id]
  (get-latest-value game id "name"))

(defn lookup-player
  "Given a token and a game, return the corresponding player id from
  that game."
  [game token]
  (->> (query game [:any "token" token])
       (first)
       (entity)))

(defn- add-player
  "Return the tupleseq needed to include a new player."
  [game name]
  (let [current-players (players game)]
    (if (> (count players) 3)
      (app-throw ::game-full)
      (let [token (str (rand-int 1000000000))
            id (next-player-id game)
            professor-id (next-professor-id game)
            ra-ids (next-ra-ids game 5)
            new-players (conj current-players id)]
        [[nil id "self" true]
         [nil id "name" name]
         [nil id "token" token]
         [nil id "money" 3]
         [nil id "pieces" (conj ra-ids professor-id)]
         [nil id "ready" false]
         [nil (first ra-ids) "location" "research-bar-ready"]
         [nil "game" "players" new-players]]))))

;;; Action functions
;;
;; These take a game, possibly some other arguments, and return the
;; tuples that should be added to the game.

(defn- record-action
  "Emits the tuples that record the player and action for this moment."
  [game player action]
  [[nil "game" "action" action]
   [nil "game" "actor" player]])

(defn- ready?
  "Returns true if the player is ready."
  [game player]
  (get-latest-value game player "ready"))

(defn- player-actions
  "Given a player, return the tuples that indicate what actions that
  player can take."
  [game player]
  (let [players (players game)]
    (concat 
     (if (and (< 2 (count players) 5)
              (every? #(ready? game %) players))
       [[nil "game" "phase" "playing"]])
     (when-not (ready? game player)
       [[nil player "ready" true]]))))

(defn- available-actions
  "Returns the tuples indicating available actions for each player."
  [game]
  (let [players (players game)]
    (map (fn [player]
           [nil player "available-actions" (player-actions game player)])
         players)))

(defn- is-player?
  "Returns true if the specified entity is a player."
  [e]
  (.startsWith e "player:"))

(defn- new-tuples
  "Given the game and an action, return all the new tuples that are a
  consequence of that action."
  [game action]
  (let [[_ e a v] action]
    (cond 
     (= [e a] ["game" "new-player"]) (add-player game v)
     ;; TODO: Also ensure that this is an available action for this
     ;; player. 
     (and (is-player? e) (= a "ready")) [[nil e a v]]
     ;; TODO: add default action here
     true (not-implemented))))

;;; Visibility

(def ^{:private true :doc "Defines the visibility rules for the game"}
  acl-rules
  [["*"        "*"     :public]
   ["player:*" "*"     :private]
   ["player:*" "ready" :public]
   ["player:*" "name"  :public]])

(defn new-game
  "Sets up a game with the data it needs in order to bootstrap."
  []
  [[0 "game" "phase" "setup"]])

(defn- rule-visibility
  "Given a tuple and a rule, return the visibility if the rule
  matches, and nil otherwise."
  [tuple rule]
  (let [[espec aspec vis] rule
	pred (build-spec-filter [espec aspec "*"])]
    (when (pred tuple) vis)))

(defn- is-public?
  "Returns true if the given tuple is public."
  [tuple]
  (->> acl-rules
       (map #(rule-visibility tuple %))
       (filter identity)		; Remove nils
       (last)
       (= :public)))

(defn get-visible-tuples
  "Returns all the tuples up to the current moment that are visible.
  Tuples are visible either if they are public or if they are visible
  in the session identitifed by token."
  [game token]
  (let [player-id (lookup-player game token)
        owned-entities #{player-id}]
    (->> game
         (filter #(or (owned-entities (entity %))
                      (is-public? %))))))

(defn- modify-game
  "Given a game and a sequence of functions that take a game, a
  player, and an action, apply each function in turn, successively
  concatenating the generated tuples onto the game for the next step."
  [game fs]
  (reduce (fn [game f] (concat game (f game))) game fs))

(defn update-game
  "Updates the state of the game given a tuple being asserted by a
given player."
  [game time player action]
  {:pre ((tuple? action))}
  (let [modified-game (modify-game game 
                              [#(record-action % player action)
                               #(new-tuples % action)
                               available-actions])]
   (reify-moment modified-game time)))