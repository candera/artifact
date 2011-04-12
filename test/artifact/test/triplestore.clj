(ns artifact.test.triplestore
  (:use [artifact.triplestore]
        [clojure.test]))

(deftest get-all-triples-returns-tripleseq
  (let [store (create-triplestore)
        store (add-moment store [["a" "b" "c"] ["d" "e" "f"]])
        store (add-moment store [["h" "i" "j"] ["a" "b" "c"]])]
    (is (tripleseq? (get-all-triples store)))))

(def ^{:private true} flintstones
  (add-moment (create-triplestore) [["fred" "loves" "wilma"]
                                    ["barney" "loves" "betty"]]))

(deftest query-works
  (is (= [["fred" "loves" "wilma"]] (query flintstones [#"f.*" :any :any])))
  (is (= [["fred" "loves" "wilma"]] (query flintstones ["fred" :any :any])))
  (is (= (set [["fred" "loves" "wilma"] ["barney" "loves" "betty"]])
         (set (query flintstones [:any "loves" :any])))))

(deftest query-values-works
  (is (= ["wilma"] (query-values flintstones ["fred" "loves" :any])))
  (is (empty? (query-values flintstones ["barney" "hates" :any]))))