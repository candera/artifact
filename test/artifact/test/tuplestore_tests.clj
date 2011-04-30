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
  [[1 "fred" "loves" nil]
   [1 "barney" "loves" "betty"]
   [nil "fred" "loves" "wilma"]])

(deftest query-works
  (is (= [[1 "fred" "loves" nil] [nil "fred" "loves" "wilma"]]
           (query flintstones-tupleseq [:any #"f.*" :any :any])))
  (is (= [[1 "fred" "loves" nil] [nil "fred" "loves" "wilma"]]
           (query flintstones-tupleseq [:any "fred" :any :any])))
  (is (= [[1 "fred" "loves" nil] [1 "barney" "loves" "betty"]]
           (query flintstones-tupleseq [1 :any "loves" :any]))))

(deftest query-values-works
  (is (= [nil "wilma"] (query-values flintstones-tupleseq [:any "fred" "loves" :any])))
  (is (empty? (query-values flintstones-tupleseq [:any "barney" "hates" :any]))))

(deftest get-latest-value-works
  (is (= "wilma"
         (get-latest-value flintstones-tupleseq "fred" "loves"))))

(deftest coalesce-works
  (is (set= (coalesce [[1 "a" "b" "c"] [2 "a" "b" "C"]])
            [[2 "a" "b" "C"]]))
  (is (set= (coalesce [[1 "a" "b" "c"] [1 "x" "y" "z"] [2 "a" "b" "C"] [2 "x" "y" "Z"] [3 "x" "y" "z"]])
            [[2 "a" "b" "C"] [3 "x" "y" "z"]])))

(deftest reify-moment-works
  (is (= [] (reify-moment [] 0)))
  (is (= (reify-moment [[1 "a" "b" "c"]] 1)
         [[1 "a" "b" "c"]]))
  (is (= (reify-moment
          [[1 "a" "b" "c"] [nil "d" "e" "f"] [nil "h" "i" "j"]]
          2)
         [[1 "a" "b" "c"] [2 "d" "e" "f"] [2 "h" "i" "j"]]))
  (is (= (reify-moment
          [[nil "a" "b" "c"] [nil "d" "e" "f"] [nil "h" "i" "j"]]
          0)
           [[0 "a" "b" "c"] [0 "d" "e" "f"] [0 "h" "i" "j"]])))

