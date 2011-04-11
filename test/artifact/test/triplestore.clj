(ns artifact.test.triplestore
  (:use [artifact.triplestore]
        [clojure.test]))

(defn- triple?
  "Returns true if its argument is a triple."
  [v]
  (and (seq? v)
       (= 3 (count v))))

(defn- tripleseq?
  "Returns true if its argument is a tripleseq."
  [v]
  (and (seq? v)
       (every? triple? v)))

(deftest get-all-triples-returns-tripleseq
  (let [store (create-triplestore)
        store (add-moment store [[1 2 3] [4 5 6]])
        store (add-moment store [[7 8 9] [1 2 3]])]
    (is (tripleseq? (get-all-triples store)))))