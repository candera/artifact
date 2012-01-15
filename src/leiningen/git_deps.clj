(ns leinigen.git-deps)

(defn git-deps
  "A leiningen task that will pull dependencies in via git.

  TODO: Write documentation that doesn't suck. If you are using this
  and see this message, feel free to email me and make me improve this
  documentation RIGHT NOW."
  [project]
  (println "TODO: Clone git projects: "
           (apply str (interpose " " (:git-dependencies project)))))