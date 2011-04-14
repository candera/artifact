(ns artifact.test.game
  (:use [artifact.game] :reload
        [artifact.triplestore]
        [artifact.state]
        [clojure.test]))

(defn- added-player-id
  "Given the tripleseq that comes back from add-player, return the id
of the newly added player"
  [tripleseq]
  (entity (single (query tripleseq [:any "token" :any]))))

(defn- has-action
  "Given a , a player id, and an action, return true if the player
has that action available to them."
  [store id action]
  (some #(= action %) (get-triple-value store id "available-actions")))

;; I'm trying out this syntax for testing game behavior. It's a bit
;; weird to do it all in a let like this, but it makes it easy to
;; intersperse transitions and assertions in the order they would
;; happen. Maybe later I'll write a macro to clean it up.
(deftest add-player-sequence
  (let [store (new-game)
        player1-triples (add-player store "Player One")
        id1 (added-player-id player1-triples)
        _ (is (has-action ))
        player2-triples (add-player store "Player Two")
        store (add-moment store player2-triples)]
        ]))

(deftest first-player-can-become-ready
  (let [store (new-game)
        store (add-moment store player1-triples)]
    (is (= false (get-triple-value store id "ready")))))

(deftest game-does-not-start-with-two-players
  (let [store (new-game)
        player1-triples (add-player store "Player One")
        store (add-moment store player1-triples)
        player2-triples (add-player store "Player Two")
        store (add-moment store player2-triples)]
    (is (empty? (get-triple-value store "player:1" "available-actions")))))

(deftest game-can-start-with-three-ready-players
  (let [store (new-game)
        store (add-moment store (add-player store "Player One"))
        store (add-moment store (add-player store "Player Two"))
        store (add-moment store (add-player store "Player Three"))]
    (is (every? #(= % [["game" "phase" "playing"]])
                (query-values store ["player:*" "available-actions" "*"])))))

(deftest game-cannot-start-with-four-unready-players)
