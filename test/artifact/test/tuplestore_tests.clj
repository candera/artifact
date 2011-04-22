(ns artifact.test.tuplestore-tests
  (:refer-clojure :exclude [time])
  (:use [artifact.tuplestore]
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

(def ^{:private true} flintstones-tupleseq
  [[1 "fred" "loves" "wilma"]
   [1 "barney" "loves" "betty"]])

(deftest query-works
  (is (= [[1 "fred" "loves" "wilma"]]
           (query flintstones-tupleseq [#"f.*" :any :any])))
  (is (= [[1 "fred" "loves" "wilma"]]
           (query flintstones-tupleseq ["fred" :any :any])))
  (is (set= [[1 "fred" "loves" "wilma"] [1 "barney" "loves" "betty"]]
            (query flintstones-tupleseq [:any "loves" :any]))))

(deftest query-values-works
  (is (= ["wilma"] (query-values flintstones-tupleseq ["fred" "loves" :any])))
  (is (empty? (query-values flintstones-tupleseq ["barney" "hates" :any]))))

(deftest query-can-work-against-tupleseq
  (is (= [[1 "fred" "loves" "wilma"]]
           (query flintstones-tupleseq [#"f.*" :any :any])))
  (is (= [[1 "fred" "loves" "wilma"]]
           (query flintstones-tupleseq ["fred" :any :any])))
  (is (set= [[1 "fred" "loves" "wilma"] [1 "barney" "loves" "betty"]]
           (query flintstones-tupleseq [:any "loves" :any]))))

(deftest get-tuple-value-works
  (is (= "wilma"
         (get-tuple-value flintstones-tupleseq "fred" "loves")
         (get-tuple-value flintstones-tupleseq "fred" "loves"))))

(deftest coalesce-works
  (is (set= (coalesce [[1 "a" "b" "c"] [2 "a" "b" "C"]])
            [[2 "a" "b" "C"]]))
  (is (set= (coalesce [[1 "a" "b" "c"] [1 "x" "y" "z"] [2 "a" "b" "C"] [2 "x" "y" "Z"] [3 "x" "y" "z"]])
            [[2 "a" "b" "C"] [3 "x" "y" "z"]])))


