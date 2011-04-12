(ns artifact.test.triplestore
  (:use [artifact.triplestore]
        [clojure.test]))

(deftest get-all-triples-returns-tripleseq
  (let [store (create-triplestore)
        store (add-moment store [["a" "b" "c"] ["d" "e" "f"]])
        store (add-moment store [["h" "i" "j"] ["a" "b" "c"]])]
    (is (tripleseq? (get-all-triples store)))))

(deftest query-works
  (let [store (create-triplestore)
        store (add-moment store [["fred" "loves" "wilma"]
                                 ["barney" "loves" "betty"]])]
    (is (= [["fred" "loves" "wilma"]] (query store [#"f.*" :any :any])))
    (is (= [["fred" "loves" "wilma"]] (query store ["fred" :any :any])))
    (is (= (set [["fred" "loves" "wilma"] ["barney" "loves" "betty"]])
             (set (query store [:any "loves" :any]))))))