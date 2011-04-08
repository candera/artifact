(ns artifact.util
  "Helper functions. This namespace should not refer to any other
artifact namespaces so that circular dependencies can be avoided.")

;;; Set things up so that we can use break when we're under swank
(if (resolve 'swank.core.connection/*current-connection*)
  (eval
   '(do
      (def swank-connection swank.core.connection/*current-connection*)
      (defmacro break []
	`(binding [swank.core.connection/*current-connection* swank-connection]
           (swank.core/break)))))
  ;; We need a definition of break for when we're not under slime,
  ;; lest there be a compile-time exception
  (defmacro break []))

