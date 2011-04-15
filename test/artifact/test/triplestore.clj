(ns artifact.test.triplestore
  (:use [artifact.triplestore]
        [clojure.test]))

(defmacro throws [x & body]
  `(is (try ~@body false (catch ~x ~'_ true))))

(defn- set=
  "Returns true if the members of coll1 and coll2 are the same by
converting them both to sets and using = to compare."
  [coll1 coll2]
  (= (set coll1) (set coll2)))

(deftest single-works
  (is (= 1 (single [1])))
  (throws java.lang.AssertionError (single [1 2])))

(deftest get-all-triples-returns-tripleseq
  (let [store (create-triplestore)
        store (add-moment store [["a" "b" "c"] ["d" "e" "f"]])
        store (add-moment store [["h" "i" "j"] ["a" "b" "c"]])]
    (is (tripleseq? (get-all-triples store)))))

(def ^{:private true} flintstones-tripleseq
  [["fred" "loves" "wilma"]
   ["barney" "loves" "betty"]])

(def ^{:private true} flintstones-store
  (add-moment (create-triplestore) flintstones-tripleseq))

(deftest query-works
  (is (= [["fred" "loves" "wilma"]]
           (query flintstones-store [#"f.*" :any :any])))
  (is (= [["fred" "loves" "wilma"]]
           (query flintstones-store ["fred" :any :any])))
  (is (= (set [["fred" "loves" "wilma"] ["barney" "loves" "betty"]])
         (set (query flintstones-store [:any "loves" :any])))))

(deftest query-values-works
  (is (= ["wilma"] (query-values flintstones-store ["fred" "loves" :any])))
  (is (empty? (query-values flintstones-store ["barney" "hates" :any]))))

(deftest query-can-work-against-tripleseq
  (is (= [["fred" "loves" "wilma"]]
           (query flintstones-tripleseq [#"f.*" :any :any])))
  (is (= [["fred" "loves" "wilma"]]
           (query flintstones-tripleseq ["fred" :any :any])))
  (is (set= [["fred" "loves" "wilma"] ["barney" "loves" "betty"]]
           (query flintstones-tripleseq [:any "loves" :any]))))

(deftest get-all-triples-works-with-tripleseq
  (is (set= flintstones-tripleseq (get-all-triples flintstones-tripleseq))))

(deftest get-triple-value-works
  (is (= "wilma"
         (get-triple-value flintstones-store "fred" "loves")
         (get-triple-value flintstones-tripleseq "fred" "loves"))))
