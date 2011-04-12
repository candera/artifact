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



