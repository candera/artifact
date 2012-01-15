(require 'robert.hooke)
(require 'leiningen.deps)
(require 'leiningen.git-deps)
(robert.hooke/add-hook #'leiningen.deps/deps
                       (fn [deps project]
                         (leiningen.git-deps/git-deps project)
                         (deps project)))