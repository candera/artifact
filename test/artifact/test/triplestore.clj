(ns artifact.test.triplestore
  (:use [artifact.triplestore]
        [clojure.test]))

(deftest get-all-triples-returns-tripleseq
  (let [store (create-triplestore)
        store (add-moment store [["a" "b" "c"] ["d" "e" "f"]])
        store (add-moment store [["h" "i" "j"] ["a" "b" "c"]])]
    (is (tripleseq? (get-all-triples store)))))