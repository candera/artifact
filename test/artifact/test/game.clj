(ns artifact.test.game
  (:use [artifact.game] :reload
        [artifact.triplestore]
        [artifact.state]
        [clojure.test]))

(deftest first-player-can-become-ready
  (let [store (new-game)
        player1-triples (add-player store "Player One")
        id (entity (single (query player1-triples [:any "token" :any])))
        store (add-moment store player1-triples)]
    (is (= (query-values store [id "available-actions"])))))

(deftest game-does-not-start-with-two-players
  (let [store (new-game)
        store (add-moment store (add-player store "Player One"))
        store (add-moment store (add-player store "Player Two"))]
    (is (empty? (get-triple-value store "player:1" "available-actions")))))

(deftest game-can-start-with-three-ready-players
  (let [store (new-game)
        store (add-moment store (add-player store "Player One"))
        store (add-moment store (add-player store "Player Two"))
        store (add-moment store (add-player store "Player Three"))]
    (is (every? #(= % [["game" "phase" "playing"]])
                (query-values store ["player:*" "available-actions" "*"])))))

(deftest game-cannot-start-with-four-unready-players)
