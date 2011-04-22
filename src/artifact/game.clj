(ns artifact.game
  "Implements the logic of the game"
  (:use artifact.triplestore
        [artifact.util :only (break)]))

(defn- players
  "Given a store, returns a sequence of players in that store."
  [store]
  (or (get-triple-value store "game" "players") []))

(defn- pieces
  "Given a store, returns a sequence of pieces in that store."
  [store]
  (or (get-triple-value store "game" "pieces") []))

(defn- entities
  "Return a seq of entity ids"
  [prefix]
  (map (fn [n] (str prefix ":" n)) (iterate inc 1)))

(defn- next-entity-ids
  "Returns the next n available ids for the specified entity in the specified store"
  [store owning-entity owning-attribute prefix n]
  (let [entity-ids (set (get-triple-value store owning-entity owning-attribute))]
    (take n
          (filter
           #(not (entity-ids %))
           (entities prefix)))))

(defn- next-entity-id
  "Return the next available id for the specified entity in the specified store"
  [store owning-entity owning-attribute prefix]
  (first (next-entity-ids store owning-entity owning-attribute prefix 1)))

(defn- next-player-id
  "Return the next available player id in the specified store."
  [store]
  (next-entity-id store "game" "players" "player"))


(defn- next-professor-id
  "Return the next available professor id in the specified store."
  [store]
  (next-entity-id store "game" "pieces" "professor"))

(defn- next-ra-ids
  "Return the next n ra ids in the specified store."
  [store n]
  (next-entity-ids store "game" "pieces" "ra" n))

(defn lookup-token-by-id
  "Given a player id and a store, return the player's token."
  [store id]
  (get-triple-value store id "token"))

(defn lookup-tokens-by-name
  "Given a player name and a store, return a sequence of tokens for
players with that name."
  [store name]
  (let [player-ids (map entity (query store [#"player:.*" "name" name]))]
    (map #(get-triple-value store % "token") player-ids)))

(defn lookup-player-name
  "Given a player id and a store, return the player's name"
  [store id]
  (get-triple-value store id "name"))

(defn lookup-player
  "Given a token and a store, return the corresponding player id from
  that store."
  [store token]
  (->> (query store [:any "token" token])
       (first)
       (entity)))

(defn- start-playing-action
  "Given a player id, return the available-actions triple that starts
the game."
  [player-id]
  [player-id "available-actions" [["game" "phase" "playing"]]])

(defn add-player
  "Return the tripleseq needed to include a new player."
  [store name]
  (let [token (str (rand-int 1000000000))
        id (next-player-id store)
        professor-id (next-professor-id store)
        ra-ids (next-ra-ids store 5)
        players (conj (players store) id)]
    [[id "self" true]
     [id "name" name]
     [id "token" token]
     [id "money" 3]
     [id "pieces" (conj ra-ids professor-id)]
     [id "ready" false]
     [(first ra-ids) "location" "research-bar-ready"]
     ["game" "players" players]]))

(defn available-actions
  ""
  [triplesource id]
  (let [players (players triplesource)]
    (filter identity
            [[id "ready" true]
             (when (> (count players) 2)
               ["game" "phase" "playing"])])))

;;; Visibility

(def ^{:private true :doc "Defines the visibility rules for the game"}
  acl-rules
  [["*"        "*"     :public]
   ["player:*" "*"     :private]
   ["player:*" "ready" :public]
   ["player:*" "name"  :public]])

(defn new-game
  "Sets up a game with the data it needs in order to bootstrap."
  []
  (add-moment (create-triplestore) [["game" "phase" "setup"]]))

(defn- rule-visibility
  "Given a triple and a rule, return the visibility if the rule
  matches, and nil otherwise."
  [triple rule]
  (let [[espec aspec vis] rule
	pred (build-spec-filter [espec aspec "*"])]
    (when (pred triple) vis)))

(defn- is-public?
  "Returns true if the given triple is public."
  [triple]
  (->> acl-rules
       (map #(rule-visibility triple %))
       (filter identity)		; Remove nils
       (last)
       (= :public)))

(defn get-visible-triples
  "Returns all the triples up to the current moment that are visible.
  Triples are visible either if they are public or if they are visible
  in the session identitifed by token."
  [store token]
  (let [player-id (lookup-player store token)
        owned-entities #{player-id}]
    (->> store
         (get-all-triples)
         (filter #(or (owned-entities (entity %))
                      (is-public? %))))))

(defn- consequents
  "Given some game state, return a list of functions that can generate
new state (in the form of triples)."
  [store tripleseq]
  (let [[e a] (query store )])))

(defn- new-triples
  "Given the store and some additional triples, return all the new triples
that are a consequence of that state."
  [store tripleseq]
  (let [fns (consequents store tripleseq)]
    (loop [f fns
           acc [tripleseq]]
      (let [res (apply (first f) store acc)]
        (if (next fns)
         (recur (next fns) (conj acc res)))))))

(defn update-game
  "Updates the state of the game given a triple being asserted by a
given player."
  [store player action]
  (apply add-moment store
         (new-triples store [["game" "action" action]
                             ["game" "actor" player]])))