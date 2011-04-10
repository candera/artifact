(ns artifact.test.game
  (:use artifact.game
        artifact.triplestore
        artifact.state
        clojure.test))

(deftest game-starts-with-three-players
  (binding [*store* (new-game)]))

