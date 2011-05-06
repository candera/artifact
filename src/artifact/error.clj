(ns artifact.error
  "Provides the ability to signal application-level errors without
  having to define classes or other annoying stuff.")

(defprotocol ApplicationError
  (data [this]))

(defn error [d]
  (throw (proxy [Throwable artifact.error.ApplicationError] []
           (data [] d))))

;; TODO: You can't expand just a catch: try is handled specially by
;; the compiler, and macroexpansions inside the try block don't work
;; right. So this macro needs to change to produce both the try and
;; the catch.
(defmacro error-catch [name & body]
  `(catch Throwable t#
     (if (satisfies? ApplicationError t#)
       (let [~name (data t#)] ~@body)
       (throw t#))))
