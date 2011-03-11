(ns artifact.api
  "Defines the JSON API used by clients to retrieve game state."
  (:use [clojure.contrib.json :only [json-str]]
	artifact.triplestore
	artifact.state))

(defn- hoist-key
  "Takes a triple of the form [[e a] v] and turns it into [e a v]."
  [triple]
  (let [[[e a] v] triple]
    [e a v]))

(defn api [since token]
  {:mime-type "application/json"
   :body (json-str
	  (map hoist-key (get-all-triples *store*)))})
