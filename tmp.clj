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