(ns artifact.error
  "Provides the ability to signal application-level errors without
  having to define classes or other annoying stuff.")

(defprotocol ApplicationError
  (data [this]))

(defn app-throw
  "Throws an error that can be handled via an app-try statments. Any
  type of data can be thrown: it need not derive from Throwable."
  [d]
  (throw (proxy [Throwable artifact.error.ApplicationError] []
           (data [] d))))

(defn- classify-clause
  "Classifies a clause in an app-try statement according to how it
  needs to be emitted into the macro expansion."
  [clause]
  (case (first clause)
        'app-catch :app-catch
        'catch :catch
        :other))

(defmacro app-try
  "Expands into a try-catch block that catch both normal Java
  exceptions and the type of error thrown by app-try.

  (app-try
     (normal-statement)
     (normal-statement)
     (when true (app-throw :whatever))
     (app-catch d
        (println d))  ; prints :whatever
     (catch Exception e
        (println \"Some other exception was thrown\")))

  Note that as currently implemented, you cannot catch Throwable, as
  the macro expands to include a clause that does that. If you need to
  catch Throwable, wrap the app-try in a regular try."
  [& body]
  (let [[statements [[_ name & handlers]] standard-catches] (partition-by classify-clause body)]
    `(try
       ~@statements
       ~@standard-catches
       (catch Throwable t#
         (if (satisfies? ApplicationError t#)
           (let [~name (data t#)] ~@handlers)
           (throw t#))))))


