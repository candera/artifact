(ns artifact.test.common
  (:use [clojure.test]
        [artifact.error]))

(defmacro throws
  "Emits a call to the is macro that will pass if the specified x is
  'thrown' by the body expressions. x can be either a
  java.lang.Throwable, in which case we look for a regular exception
  of that type to be raised, or it can be anything else, in which case
  we look for an app-throw that threw that value."
  [x & body]
  (if (and (symbol? x) (isa? (resolve x) java.lang.Throwable))
    `(is (try ~@body false (catch ~x ~'_ true)))
    `(is (app-try
          ~@body
          false
          (~'app-catch e# (= e# ~x))))))