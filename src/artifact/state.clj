(ns artifact.state
  "Holds the state for the game."
  (:use artifact.triplestore))

(def ^{:dynamic true} *store* (create-triplestore))