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

(defn- has-action?
  "Given a triplesource, a player id, and an action, return true if
the player has that action available to them."
  [triplesource id action]
  (some #(= action %) (get-triple-value triplesource id "available-actions")))

(defn- scenario-clause
  "Given a pair, if the first item is :assert, emits a literal underscore and te second item. Otherwise, emits the pair as-is."
  [[a b]]
  (if (= a :assert) ['_ b] [a b]))

(defn- scenario-clauses
  "Walks across items two at a time, calling gen-scenario-clause on
each pair, creating a vector of bindings suitable for use in
defscenario."
  [items]
  (reduce into [] (map scenario-clause (partition 2 items))))

(defmacro defscenario
  "Generates a test of the type shown elsewhere in this file by
emitting a let that has bindings interspersed with assertions."
  [name & body]
  `(deftest ~name
     (let ~(scenario-clauses body))))

(defn- can-become-ready?
  "Returns true if the specified player has the [id \"ready\" true]
action available. "
  [triplesource id]
  (has-action? triplesource id [id "ready" true]))

(defn- can-start-game?
  "Returns true if the specified player has the [\"game\" \"phase\" \"playing\" action available."
  [triplesource id]
  (has-action? triplesource id ["game" "phase" "playing"]))

(defscenario add-player-sequence
  store (new-game)
  store (update-game nil store ["game" "action" ["game" "new-player" "One"]])
  id1 (player-by-name "One")
  :assert (is (can-become-ready? store id1))
  :assert (is (not (can-start-game? player1-triples id1)))
  store (update-game nil store ["game" "action" ["game" "new-player" "Two"]])
  id2 (player-by-name "Two")
  :assert (is (can-become-ready? store id2))
  :assert (is (not (can-start-game? store id2)))
  store (update-game nil store ["game" "action" ["game" "new-player" "Three"]])
  id3 (player-by-name "Three")
  :assert (is (can-become-ready? store id3))
  :assert (is (not (can-start-game? store id1)))
  :assert (is (not (can-start-game? store id2)))
  :assert (is (not (can-start-game? store id3)))
  ;; TODO: have one player become ready, assert game can't start
  ;; TODO: have two players become ready, assert game can't start
  ;; TODO: have third player become ready, assert game can start
  )

;; (deftest game-does-not-start-with-two-players
;;   (let [store (new-game)
;;         player1-triples (add-player store "Player One")
;;         store (add-moment store player1-triples)
;;         player2-triples (add-player store "Player Two")
;;         store (add-moment store player2-triples)]
;;     (is (empty? (get-triple-value store "player:1" "available-actions")))))

;; (deftest game-can-start-with-three-ready-players
;;   (let [store (new-game)
;;         store (add-moment store (add-player store "Player One"))
;;         store (add-moment store (add-player store "Player Two"))
;;         store (add-moment store (add-player store "Player Three"))]
;;     (is (every? #(= % [["game" "phase" "playing"]])
;;                 (query-values store ["player:*" "available-actions" "*"])))))

;; (deftest game-cannot-start-with-four-unready-players)
