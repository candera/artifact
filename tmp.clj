(ns artifact.experiment (:use artifact.core ))

(@#'artifact.core/to-html-str @#'artifact.core/index)

(use 'artifact.triplestore)

(in-ns 'artifact.triplestore)

(conj-new-moment [] [["entity1" "att1" "val1-1"]])

(reset-triplestore)

(add-triples ["entity1" "att1" "val1-1"]
	     ["entity1" "att2" "val2-1"])

(add-triples ["entity1" "att1" "val1-2"])

(get-triple-value "entity1" "att1")
(get-triple-value ["entity1" "att1"])
(get-triple-value "entity1" "att2")
(get-triple-value "entity2" "att1")
(get-triple-value "entity1" "att3")
(get-triple-value ["global" "time"])

(use 'clojure.pprint)

(pprint @#'artifact.triplestore/triplestore)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(in-ns 'artifact.game)
(def store (create-triplestore))
(initialize-game store)

(defn priority [& triples]
  (first (filter (fn [[[_ a] _]] (= a "priority")) triples)))

(->> (query store ["acl:*" "*" "*"])
     (group-by entity)
     (map second)
     (map priority)
     (pprint))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^{:private true :doc "Defines the visibility rules for the game"}
  acl-rules
  [["*"        "*"     :public]
   ["player:*" "*"     :private]
   ["player:*" "ready" :public]
   ["player:*" "name"  :public]])

(def store (create-triplestore))

(add-triples store
	     ["foo" "bar" 1]
	     ["player:1" "ready" true]
	     ["player:1" "token" "abcd"]
	     ["player:1" "name" "Craig"]
	     ["player:2" "ready" false])

(use 'artifact.triplestore)

(defn visibility [triple rule]
  (let [[espec aspec vis] rule
	pred (build-spec-filter [espec aspec "*"])]
    (when (pred triple) vis)))

(defn visibilities [triple]
  (map #(visibility triple %) acl-rules))

(->> (get-all-triples store)
     ;; Something that turns each triple into [:public :private nil :public]
     (map visibilities)
     (map  #(last (filter identity %)))
     (pprint))

(pprint (get-all-triples store))
(pprint  (map #(is-visible? #{"global"} %) (get-all-triples store)));; foo

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(use 'artifact.triplestore)
(use 'artifact.game)

(def store (new-game))
(def store (add-moment store (add-player store "Craig")))
(def store (add-moment store (add-player store "Alice")))
(def store (add-moment store (add-player store "Ellen")))

(def store (create-triplestore))
(def store (add-moment store [["fred" "loves" "wilma"]
                                 ["barney" "loves" "betty"]]))

(use 'clojure.pprint)

(pprint store)

(pprint (get-all-triples store))

(query store [:any "loves" :any] ["fred" :any :any])

(pprint (reduce merge store))

(pprint  (map (fn [[[e a] v]] [e a v]) (reduce merge store)))

(query store ["player:*" "available-actions" "*"])

(query-values store ["player:*" "available-actions" "*"])

(query-values store ["I" "hate" :any])

(query store ["player:1" "token" :any])
(query (get-all-triples store) ["player:1" "token" :any])

(to-tripleseq store)
(to-tripleseq (get-all-triples store))
(tripleseq? (get-all-triples store))

(map triple? (get-all-triples store))

(pprint (zipmap (get-all-triples store) (map triple? (get-all-triples store))))

(tripleseq? [["a" "b" "c"]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(let [foo bar]
  (expr1 arg1)
  (expr2 arg2))

(use 'clojure.pprint)

(in-ns 'artifact.test.game)
(def  store (new-game))
(def  player1-triples (add-player store "One"))
(def  id1 (added-player-id player1-triples))
player1-triples
(def  store (add-moment store player1-triples))
(is (can-become-ready? store id1))
(get-all-triples store)
(["player:2" "token" "173456988"]
["player:2" "available-actions" (["player:2" "ready" true])]
["game" "players" ["player:1" "player:2"]]
["player:1" "token" "224453781"]
["player:1" "available-actions" (["player:1" "ready" true])]
["player:2" "money" 3] ["player:1" "money" 3] ["game" "phase" "setup"]
["player:2" "ready" false] ["global" "time" 2] ["player:1" "ready"
false]
["player:2" "pieces" ("professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5")]
["player:1" "pieces" ("professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5")]
["ra:1" "location" "research-bar-ready"] ["player:2" "name" "Two"]
["player:2" "self" true] ["player:1" "name" "One"] ["player:1" "self"
true])
(is (not (can-start-game? player1-triples id1)))
(def  player2-triples (add-player store "Two"))
(def  id2 (added-player-id player2-triples))
(def  store (add-moment store player2-triples))
(is (can-become-ready? store id2))
(is (not (can-start-game? store id2)))
(def  player3-triples (add-player store "Three"))
(def  id3 (added-player-id player3-triples))
(query player3-triples [:any "token" :any])
(map triple? player3-triples)
(filter (complement triple?) player3-triples)
(def  store (add-moment store player3-triples))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def clock (atom 0))
(defn now [] (swap! clock inc))

(def game (new-game))
(def game (update-game game (now) nil [nil "game" "new-player" "One"]))
(def id1 (last (get-latest-value game "game" "players")))
(def game (update-game game (now) id1 [nil id1 "ready" true]))
(def game (update-game game (now) nil [nil "game" "new-player" "Two"]))
(def id2 (last (get-latest-value game "game" "players")))
(def game (update-game game (now) nil [nil "game" "new-player" "Three"]))
(def id3 (last (get-latest-value game "game" "players")))
(def game (update-game game (now) id2 [nil id2 "ready" true]))
(def game (update-game game (now) id3 [nil id3 "ready" true]))

(use 'clojure.pprint)
(use 'artifact.tuplestore)
(pprint (coalesce game))
(pprint game)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(pprint (coalesce '([0 "game" "phase" "setup"] [1 "game" "action"
         [nil "game" "new-player" "One"]] [1 "game" "actor" nil]
         [1 "player:1" "self" true] [1 "player:1" "name" "One"]
         [1 "player:1" "token" "478783384"] [1 "player:1" "money" 3]
         [1 "player:1" "pieces" ("professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5")]
         [1 "player:1" "ready" false]
         [1 "ra:1" "location" "research-bar-ready"]
         [1 "game" "players" ["player:1"]]
         [1 "player:1" "available-actions" ([nil "player:1" "ready"
         true])] [2 "game" "action" [nil "player:1" "ready" true]]
         [2 "game" "actor" "player:1"] [2 "player:1" "ready" true]
         [2 "player:1" "available-actions" ()] [3 "game" "action"
         [nil "game" "new-player" "Two"]] [3 "game" "actor" nil]
         [3 "player:2" "self" true] [3 "player:2" "name" "Two"]
         [3 "player:2" "token" "489025038"] [3 "player:2" "money" 3]
         [3 "player:2" "pieces" ("professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5")]
         [3 "player:2" "ready" false]
         [3 "ra:1" "location" "research-bar-ready"]
         [3 "game" "players" ["player:1" "player:2"]]
         [3 "player:1" "available-actions" ()]
         [3 "player:2" "available-actions" ([nil "player:2" "ready"
         true])] [4 "game" "action" [nil "game" "new-player" "Three"]]
         [4 "game" "actor" nil] [4 "player:3" "self" true]
         [4 "player:3" "name" "Three"]
         [4 "player:3" "token" "514142623"] [4 "player:3" "money" 3]
         [4 "player:3" "pieces" ("professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5")]
         [4 "player:3" "ready" false]
         [4 "ra:1" "location" "research-bar-ready"]
         [4 "game" "players" ["player:1" "player:2" "player:3"]]
         [4 "player:1" "available-actions" ()]
         [4 "player:2" "available-actions" ([nil "player:2" "ready"
         true])]
         [4 "player:3" "available-actions" ([nil "player:3" "ready"
         true])] [5 "game" "action" [nil "player:2" "ready" true]]
         [5 "game" "actor" "player:2"] [5 "player:2" "ready" true]
         [5 "player:1" "available-actions" ()]
         [5 "player:2" "available-actions" ()]
         [5 "player:3" "available-actions" ([nil "player:3" "ready"
         true])] [6 "game" "action" [nil "player:3" "ready" true]]
         [6 "game" "actor" "player:3"] [6 "player:3" "ready" true]
         [6 "player:1" "available-actions" ([nil "game" "phase" "playing"])]
         [6 "player:2" "available-actions" ([nil "game" "phase" "playing"])]
         [6 "player:3" "available-actions" ([nil "game" "phase" "playing"])]
         [7 "game" "action" [nil "game" "new-player" "Four"]]
         [7 "game" "actor" nil] [7 "player:4" "self" true]
         [7 "player:4" "name" "Four"]
         [7 "player:4" "token" "252873007"] [7 "player:4" "money" 3]
         [7 "player:4" "pieces" ("professor:1" "ra:1" "ra:2" "ra:3" "ra:4" "ra:5")]
         [7 "player:4" "ready" false]
         [7 "ra:1" "location" "research-bar-ready"]
         [7 "game" "players"
         ["player:1" "player:2" "player:3" "player:4"]]
         [7 "player:1" "available-actions" ()]
         [7 "player:2" "available-actions" ()]
         [7 "player:3" "available-actions" ()]
         [7 "player:4" "available-actions" ([nil "player:4" "ready"
         true])] [8 "game" "action" [nil "player:4" "ready" true]]
         [8 "game" "actor" "player:4"] [8 "player:4" "ready" true]
         [8 "player:1" "available-actions" ([nil "game" "phase" "playing"])]
         [8 "player:2" "available-actions" ([nil "game" "phase" "playing"])]
         [8 "player:3" "available-actions" ([nil "game" "phase" "playing"])]
         [8 "player:4" "available-actions" ([nil "game" "phase" "playing"])])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(in-ns 'logic-tutorial.tut1)

(fact parent 'Craig 'Ellen)
(fact parent 'Craig 'Susan)
(fact parent 'Alice 'Ellen)
(fact parent 'Alice 'Susan)
(fact parent 'Edwin 'Craig)
(fact parent 'Maddy 'Craig)
(fact parent 'Edwin 'Kristin)
(fact parent 'Maddy 'Kristin)
(fact female 'Kristin)

(run* [q] (female q))

(defn sibling [x y]
  (exist [p]
         (parent p x)
         (parent p y)))

(defn sister [x y]
  (all
   (sibling x y)
   (female x)))

(run* [q] (sister q 'Craig))

(defn aunt [adult kid]
  (exist [p]
         (sibling p adult)
         (parent p kid)
         (female adult)))


(run* [q] (sibling 'Craig q))

(run* [q] (aunt 'Kristin q))
(run* [q] (aunt q 'Ellen))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(use 'artifact.error)



(try
  (error "oh noes!")
  (error-catch e
               (str "Barfed with: " e)))

(try (error "oh noes!")
     (catch java.lang.Throwable t__2372__auto__
       (if (clojure.core/satisfies? artifact.error/ApplicationError t__2372__auto__)
         (let* [e (artifact.error/data t__2372__auto__)]
               (str "Barfed with: " e))
         "Rethrown")))


(error "foo")

(try
  (error "oh noes!")
  (catch Exception e (str "Barfed with exception: " e))
  user/foo)

(try (error "oh noes!")
     (clojure.core/when
      (clojure.core/seq user/stdcatches)
      (catch Exception e
        (str "Barfed with exception: " e)))
     user/foo)

(try (error "oh noes!")
     (catch Exception e (str "Barfed with exception: " e))
     (catch java.lang.Throwable t__1932__auto__
       (if (clojure.core/satisfies? user/ApplicationError t__1932__auto__)
         (clojure.core/let [nil (user/data t__1932__auto__)])
         (throw t__1932__auto__))))

(try
  (error "oh noes!")
  (catch java.lang.Throwable t__1932__auto__
    (if (clojure.core/satisfies? user/ApplicationError t__1932__auto__)
      (clojure.core/let [nil (user/data t__1932__auto__)])
      (throw t__1932__auto__))))


(app-try
 (error "oh noes!")
 (app-catch e
            (str "Barfed with: " e))
 (catch Exception e
   (str "Barfed with exception: " e)))


(defn- classify-clause [clause]
  (case (first clause)
        'app-catch 1
        'catch 2
        3))

(defmacro app-try [& body]
  (let [[statements [_ name & handlers] standard-catches] (partition-by classify-clause body)]
    `(try
       ~@statements
       ~@standard-catches
       (catch Throwable t#
          (if (satisfies? ApplicationError t#)
            (let [~name (data t#)] ~@handlers)
            (throw t#))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(use 'artifact.error)

(app-try
 (println "Got here!")
 (throw (Exception. "oh noes!"))
 (println "Never got here!")
 (app-catch e
            (str "Barfed with: " e))
 (catch Exception x
   (str "Regular exception: " x)))

(try
  (println "Got here!")
  (app-throw "oh noes!")
  (println "Never got here!")
  (catch java.lang.Throwable t__1841__auto__
    (if (clojure.core/satisfies? artifact.error/ApplicationError t__1841__auto__)
      "Satisfied"
      "Not satisfied")))

(app-try
 (println "Got here!")
 (app-throw "oh noes!")
 (println "Never got here!")
 (app-catch e
            (str "Barfed with: " e))
 (catch Exception e
   (str "Barfed with exception: " e)))

(try
  (println "Got here!")
  (app-throw "oh noes!")
  (println "Never got here!")
  (catch Throwable e (str "Barfed with exception: " e)))

(try
  (println "Got here!")
  (app-throw "oh noes!")
  (println "Never got here!")
  (catch Exception e
    (str "Barfed with exception: " e))
  (catch java.lang.Throwable t__2186__auto__
    (if (clojure.core/satisfies? artifact.error/ApplicationError t__2186__auto__)
      (clojure.core/let [e (artifact.error/data t__2186__auto__)]
                        (str "Barfed with: " e))
      (throw t__2186__auto__))))

(try
  (println "Got here!")
  (app-throw "oh noes!")
  (println "Never got here!")
  (catch Exception e (str "Barfed with exception: " e))
  (catch java.lang.Throwable t__2154__auto__
    (if (clojure.core/satisfies? artifact.error/ApplicationError t__2154__auto__)
      (clojure.core/let [nil (artifact.error/data t__2154__auto__)])
      (throw t__2154__auto__))))

(+ 2 3)                       ; a harmless no-op, just so
                                        ; we're doing something else
                                        ; in this function.

(defn- classify-clause
  "Classifies a clause in an app-try statement according to how it
  needs to be emitted into the macro expansion."
  [clause]
  (if (seq? clause)
    (case (first clause)
          'app-catch :app-catch
          'catch :catch
          :other)
    :other))

(classify-clause false)

(map classify-clause
              '[(+ 2 3)
                (app-throw :test-value)
                false
                (app-catch e
                           e)
                (catch FileNotFoundException x x)])