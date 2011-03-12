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
(pprint  (map #(is-visible? #{"global"} %) (get-all-triples store)))