(ns artifact.state
  "Holds the state for the game.")

(def ^{:dynamic true} *game* (ref nil))
(def ^{:dynamic true} *clock* (ref 0))