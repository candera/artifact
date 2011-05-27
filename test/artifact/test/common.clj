(ns artifact.test.common
  (:use [clojure.test]))

(defmacro throws [x & body]
  `(is (try ~@body false (catch ~x ~'_ true))))