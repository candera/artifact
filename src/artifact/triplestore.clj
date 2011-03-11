(ns artifact.triplestore)

(def ^{:private true} triplestore (atom []))

;;; Creates a new triple store, which internally is a vector of
;;; moments, each of which is a map that takes an entity and an
;;; attribute to a value. So like this:
;;; [ { [entity att] val      ; Moment 0
;;      [entity att] val } 
;;    { [entity att] val } ]  ; Moment 1
;;;  etc
;;; ]
;;; TODO: Might need to make this a ref just to support multiple consistent reads
(defn create-triplestore [] (atom []))

(defn- triples-to-map [triples]
  (reduce #(assoc %1 (subvec %2 0 2) (nth %2 2))
	  {}
	  triples))

(def ^{:doc "A key that can be passed to get-triple-value to retrieve the current time."}
  time-key ["global" "time"])

(defn latest-time [store]
  (get (last store) time-key -1))

(defn- conj-new-moment
  "Produces a new triplestore that adds the specified triples, but
  also adds a new value for entity 'game' attribute 'time' that's one
  more than the latest one."
  [store triples]
  (let [new-game-time (inc (latest-time store))]
    (conj store
	  (assoc (triples-to-map triples) time-key new-game-time))))

(defn reset-triplestore [store]
  (reset! store []))

(defn add-triples [store & triples]
  (swap! store conj-new-moment triples))

(defn get-triple-value
  "Returns the value of attribute 'att' for entity 'entity' from the
triplestore. Can accept the entity and attribute either as two args or
as a single vector pair."
  ([store [entity att]] (get-triple-value store entity att))
  ([store entity att]
     (some identity (map
		     #(get % [entity att])
		     (rseq @store)))))

(defn get-all-triples [store]
  (reduce merge @store))

(defn- build-filter
  "Given a template (either * or a literal value), return a predicate
  that will return true if either the template is * or the parameter
  matches the template exactly."
  [t]
  (fn [v] (or (= t "*") (= t v))))

(defn- build-spec-filter
  "Given a single triplespec (see query) build a predicate that will
  return true for aany tuple that matches the spec."
  [triplespec]
  (let [[e-spec a-spec v-spec] triplespec]
    (fn [[[e a] v]]
      (and ((build-filter e-spec) e)
	   ((build-filter a-spec) a)
	   ((build-filter v-spec) v)))))

(defn- build-specs-filter
  "Given a sequence of triplespecs (see query), build a predicate that
  will return true for any triple that matches at least one
  triplespec."
  [triplespecs]
  (apply comp (map build-spec-filter triplespecs)))

(defn query
  "Given a store and a triple template, return all triples that match
  the pattern. The pattern can contain either exact matches or
  wildcards (a literal '*' string), which match any item. So, for
  example, (query store [\"*\" \"*\" \"*\"]) return all triples, and
  (query store [\"foo\" \"*\" \"bar\"]) returns all triples that have
  an entity of foo and a value of bar, regardless of attribute."
  [store & triplespecs]
  (filter (build-specs-filter triplespecs) (get-all-triples store)))

(defn value
  "Given a triple, return the value"
  [triple]
  (second triple))

(defn entity
  "Given a triple, return the entity"
  [triple]
  (first (first triple)))

(defn attribute
  "Given a triple, return the attribute"
  [triple]
  (second (first triple)))