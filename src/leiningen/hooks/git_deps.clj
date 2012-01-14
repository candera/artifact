(require 'robert.hooke)
(require 'leiningen.deps)
(robert.hooke/add-hook #'leiningen.deps/deps
                       (fn [deps project]
                         (println "Hooking deps!")
                         (println "Looks like I ought to clone "
                                  (:git-dependencies project))
                         (deps project)))