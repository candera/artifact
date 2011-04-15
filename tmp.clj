(ns artifact.experiment (:use artifact.core ))

(@#'artifact.core/to-html-str @#'artifact.core/index)

(use 'artifact.triplestore)

(in-ns 'artifact.triplestore)

(conj-new-moment [] [["entity1" "att1" "val1-1"]])

(reset-triplestore)

(add-triples ["entity1" "att1" "val1-1"]
	     ["entity1" "att2" "val2-1"])

(add-triples ["entity1" "att1" "val1-2"])

(get-triple-value "entity1" "att1")
(get-triple-value ["entity1" "att1"])
(get-triple-value "entity1" "att2")
(get-triple-value "entity2" "att1")
(get-triple-value "entity1" "att3")
(get-triple-value ["global" "time"])

(use 'clojure.pprint)

(pprint @#'artifact.triplestore/triplestore)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(in-ns 'artifact.game)
(def store (create-triplestore))
(initialize-game store)

(defn priority [& triples]
  (first (filter (fn [[[_ a] _]] (= a "priority")) triples)))

(->> (query store ["acl:*" "*" "*"])
     (group-by entity)
     (map second)
     (map priority)
     (pprint))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^{:private true :doc "Defines the visibility rules for the game"}
  acl-rules
  [["*"        "*"     :public]
   ["player:*" "*"     :private]
   ["player:*" "ready" :public]
   ["player:*" "name"  :public]])

(def store (create-triplestore))

(add-triples store
	     ["foo" "bar" 1]
	     ["player:1" "ready" true]
	     ["player:1" "token" "abcd"]
	     ["player:1" "name" "Craig"]
	     ["player:2" "ready" false])

(use 'artifact.triplestore)

(defn visibility [triple rule]
  (let [[espec aspec vis] rule
	pred (build-spec-filter [espec aspec "*"])]
    (when (pred triple) vis)))

(defn visibilities [triple]
  (map #(visibility triple %) acl-rules))

(->> (get-all-triples store)
     ;; Something that turns each triple into [:public :private nil :public]
     (map visibilities)
     (map  #(last (filter identity %)))
     (pprint))

(pprint (get-all-triples store))
(pprint  (map #(is-visible? #{"global"} %) (get-all-triples store)));; foo

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(use 'artifact.triplestore)
(use 'artifact.game)

(def store (new-game))
(def store (add-moment store (add-player store "Craig")))
(def store (add-moment store (add-player store "Alice")))
(def store (add-moment store (add-player store "Ellen")))

(def store (create-triplestore))
(def store (add-moment store [["fred" "loves" "wilma"]
                                 ["barney" "loves" "betty"]]))

(use 'clojure.pprint)

(pprint store)

(pprint (get-all-triples store))

(query store [:any "loves" :any] ["fred" :any :any])

(pprint (reduce merge store))

(pprint  (map (fn [[[e a] v]] [e a v]) (reduce merge store)))

(query store ["player:*" "available-actions" "*"])

(query-values store ["player:*" "available-actions" "*"])

(query-values store ["I" "hate" :any])

(query store ["player:1" "token" :any])
(query (get-all-triples store) ["player:1" "token" :any])

(to-tripleseq store)
(to-tripleseq (get-all-triples store))
(tripleseq? (get-all-triples store))

(map triple? (get-all-triples store))

(pprint (zipmap (get-all-triples store) (map triple? (get-all-triples store))))

(tripleseq? [["a" "b" "c"]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(let [foo bar]
  (expr1 arg1)
  (expr2 arg2))

(use 'clojure.pprint)

(in-ns 'artifact.test.game)
(def  store (new-game))
(def  player1-triples (add-player store "One"))
(def  id1 (added-player-id player1-triples))
player1-triples
(def  store (add-moment store player1-triples))
(is (can-become-ready? store id1))
(get-all-triples store)
(["player:2" "token" "173456988"]
["player:2" "available-actions" (["player:2" "ready" true])]
["game" "players" ["player:1" "player:2"]]
["player:1" "token" "224453781"]
["player:1" "available-actions" (["player:1" "ready" true])]
["player:2" "money" 3] ["player:1" "money" 3] ["game" "phase" "setup"]
["player:2" "ready" false] ["global" "time" 2] ["player:1" "ready"
false]
["player:2" "pieces" ("professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5")]
["player:1" "pieces" ("professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5")]
["ra:1" "location" "research-bar-ready"] ["player:2" "name" "Two"]
["player:2" "self" true] ["player:1" "name" "One"] ["player:1" "self"
true])
(is (not (can-start-game? player1-triples id1)))
(def  player2-triples (add-player store "Two"))
(def  id2 (added-player-id player2-triples))
(def  store (add-moment store player2-triples))
(is (can-become-ready? store id2))
(is (not (can-start-game? store id2)))
(def  player3-triples (add-player store "Three"))
(def  id3 (added-player-id player3-triples))
(query player3-triples [:any "token" :any])
(map triple? player3-triples)
(filter (complement triple?) player3-triples)
(def  store (add-moment store player3-triples))
