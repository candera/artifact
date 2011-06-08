(ns artifact.test.game-tests
  (:use [artifact.game] :reload
        [artifact.tuplestore]
        [artifact.state]
        [artifact.test.common]
        [clojure.test])
  (:refer-clojure :exclude [time]))

(defn- added-player-id
  "Given the tupleseq that comes back from add-player, return the id
of the newly added player"
  [tupleseq]
  (entity (single (query tupleseq [:any :any "token" :any]))))

(defn- player-by-name
  "Return the id for the player with the given name."
  [game name]
  (last (map entity (query game [:any #"^player:.*" "name" name]))))

(defn- has-action?
  "Given a tupleseq, a player id, and an [e a v] action triple, return
  true if othe player has that action available to them."
  [tupleseq id action]
  (some (fn [[_ e a v]] (= [e a v] action))
        (get-latest-value tupleseq id "available-actions")))

(defmacro defscenario
  "Generates a test of the type shown elsewhere in this file by
emitting a let that has bindings interspersed with assertions."
  [name & body]
  `(deftest ~name
     (let [~@body])))

(defn- can-become-ready?
  "Returns true if the specified player has the [id \"ready\" true]
action available. "
  [tupleseq id]
  (has-action? tupleseq id [id "ready" true]))

(defn- can-start-game?
  "Returns true if the specified player has the [\"game\" \"phase\" \"playing\" action available."
  [tupleseq ids]
  (every? #(has-action? tupleseq % ["game" "phase" "playing"]) ids))

(defn- add-player [game name]
  (let [game (update-game game nil [nil "game" "new-player" name])]
    [game (player-by-name game name)]))

(defn- become-ready [game id]
  (update-game game id [nil id "ready" true]))

(defscenario add-player-sequence
  game (new-game)
  [game id1] (add-player game "One")
  _ (is (can-become-ready? game id1))
  ;; Game can't start with only one player, and him not even ready
  _ (is (not (can-start-game? game [id1])))
  game (become-ready game id1)
  ;; Then, once player one is ready, becoming ready is no longer an
  ;; available action, and the game still can't start.
  _ (is (not (can-become-ready? game id1)))
  _ (is (not (can-start-game? game [id1])))
  [game id2] (add-player game "Two")
  _ (is (can-become-ready? game id2))
  _ (is (not (can-start-game? game [id1 id2])))
  [game id3] (add-player game "Three")
  _ (is (can-become-ready? game id3))
  _ (is (not (can-start-game? game [id1 id2 id3])))
  ;; Second players becomes ready, but game can't start
  game (become-ready game id2)
  _ (is (not (can-start-game? game [id1 id2 id3])))
  ;; All players ready, game can start
  game (become-ready game id3)
  _ (is (can-start-game? game [id1 id2 id3]))
  [game id4] (add-player game "Four")
  _ (is (not (can-start-game? game [id1 id2 id3 id4])))
  ;; Fourth player becomes ready, game can start
  game (become-ready game id4)
  _ (is (can-start-game? game [id1 id2 id3 id4]))
  ;; Fifth player cannot be added
  _ (throws :artifact.game/cannot-add-more-players (add-player game "Five")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest lookup-player-works
  (let [game [[0 "player:1" "token" "p1-token"]
              [1 "player:2" "token" "p2-token"]]]
    (is (nil? (lookup-player game "invalid-token")))
    (is (= "player:1" (lookup-player game "p1-token")))
    (is (= "player:2" (lookup-player game "p2-token")))))

(deftest get-visible-tuples-works
  (is (= []
         (get-visible-tuples
          [[0 "player:1" "token" "p1-token"]]
          "p2-token"))))

