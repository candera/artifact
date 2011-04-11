(ns artifact.test.game
  (:use [artifact.game] :reload
        [artifact.triplestore]
        [artifact.state]
        [clojure.test]))

(deftest game-does-not-start-with-two-players
  (let [store (new-game)
        store (add-moment store (add-player store "Player One"))
        store (add-moment store (add-player store "Player Two"))]
    (is (empty? (get-triple-value store "player:1" "available-actions")))))

(deftest game-can-start-with-three-players
  (let [store (new-game)
        store (add-moment store (add-player store "Player One"))
        store (add-moment store (add-player store "Player Two"))
        store (add-moment store (add-player store "Player Three"))]
    (is (every? #(= % [["game" "phase" "playing"]])
                (query-values store ["player:*" "available-actions" "*"])))))

