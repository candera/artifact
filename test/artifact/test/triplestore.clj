(ns artifact.test.triplestore
  (:use [artifact.triplestore]
        [clojure.test]))

(defmacro throws [x & body]
  `(is (try ~@body false (catch ~x ~'_ true))))

(deftest single-works
  (is (= 1 (single [1])))
  (throws java.lang.AssertionError (single [1 2])))

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

(deftest query-can-work-against-tripleseq
  (let [tripleseq (get-all-triples flintstones)]
   (is (= [["fred" "loves" "wilma"]] (query tripleseq [#"f.*" :any :any])))
   (is (= [["fred" "loves" "wilma"]] (query tripleseq ["fred" :any :any])))
   (is (= (set [["fred" "loves" "wilma"] ["barney" "loves" "betty"]])
          (set (query tripleseq [:any "loves" :any]))))))

;; (deftest get-all-triples-works-with-tripleseq
;;   (let [tripleseq (get-all-triples flintstones)]
;;     (is (= ))))
