(ns artifact.test.error-tests
  (:use [artifact.error]
        [artifact.test.common]
        [clojure.test])
  (:import [java.io FileNotFoundException]))

(deftest app-try-catches-app-throw
  (is (= :test-value
         (app-try
          (+ 2 3)                       ; a harmless no-op, just so
                                        ; we're doing something else
                                        ; in this function.
          (app-throw :test-value)
          false
          (app-catch e
                     e)
          (catch FileNotFoundException x x)))))

(deftest app-try-catches-exception
    (is (= :exception
         (app-try
          (+ 2 3)
          (throw (FileNotFoundException. "Boom!"))
          (app-throw :test-value)
          false
          (app-catch e
                     e)
          (catch FileNotFoundException x :exception)))))

(deftest app-try-rethrows-uncaught
  (throws Exception
          (app-try
           (+ 2 3)
           (throw (Exception. "Boom!"))
           (app-throw :test-value)
           false
           (app-catch e
                      e)
           (catch FileNotFoundException x x))))

(defn throwing-fn [data]
  (app-throw data))

(deftest app-try-catches-nested-throw
  (is (= :test-value
         (app-try
          (throwing-fn :test-value)
          (app-catch e e)))))