(ns artifact.triplestore
  "A simple triple store with basic query capabilities. Might get replaced by something more capable at some point. Terminology:

* triple: a sequence with three elements, entity, attribute, and
  value. Entities and attributes must be strings. Values can be
  strings, nil, integers, triples, or tripleseqs.

* moment: a map of [entity attribute] pairs to values, each of which
  implies a triple. Also includes the entry [\"global\" \"time\"]
  whose value is a monotonically increasing integer identifying the
  instant this moment represents.

* triplestore: a sequence of moments

* tripleseq: a sequence of triples

* triplespec: see the definition of query")

(defn create-triplestore
  "Creates a new, empty triplestore."
  []
  [])

(defn entity
  "Given a triple, return the entity"
  [triple]
  (first triple))

(defn attribute
  "Given a triple, return the attribute"
  [triple]
  (second triple))

(defn value
  "Given a triple, return the value"
  [triple]
  (nth triple 2))
;; triple? and tripleseq? make mutual use of each other, so one of
;; them has to be declared first. I chose tripleseq? arbitrarily
(declare tripleseq?)

(defn triple?
  "Returns true if its argument is a triple."
  [x]
  (and (sequential? x)
       (= 3 (count x))
       (string? (entity x))
       (string? (attribute x))
       (let [v (value x)]
         (or (nil? v)
             (string? v)
             (triple? v)
             (tripleseq? v)
             (integer? v)))))

(defn tripleseq?
  "Returns true if its argument is a tripleseq."
  [x]
  (and (seq? x)
       (every? triple? x)))

(defn- triples-to-map
  "Turns a tripleseq into a map of the form
 {[e a] v, [e a] v, ...}"
  [tripleseq]
  {:pre (tripleseq? tripleseq)}
  (reduce #(assoc %1 (subvec %2 0 2) (nth %2 2))
	  {}
	  tripleseq))

(def ^{:doc "A key that can be passed to get-triple-value to retrieve the current time."}
  time-key ["global" "time"])

(defn latest-time
  "Returns the value of global/time for the most recent moment, or -1
  of the triplestore is empty."
  [store]
  (get (last store) time-key -1))

(defn- conj-new-moment
  "Given a store and a tripleseq, returns the store with a new moment
appended that adds the specified triples, but also adds a new value
for entity 'game' attribute 'time' that's one more than the latest
one."
  [store tripleseq]
  (let [new-game-time (inc (latest-time store))]
    (conj store
	  (assoc (triples-to-map tripleseq) time-key new-game-time))))

(defn add-moment
  "Given a store and a tripleseq, creates a new moment and returns an
updated store that includes the specified triples. As a convenience, a
triple may be nil, in which case it is ignored."
  [store tripleseq] 
  {:pre (every? #(or (nil? %) (triple? %)) tripleseq)}
  (conj-new-moment store (filter identity tripleseq)))

(defn get-triple-value
  "Returns the value of attribute 'att' for entity 'entity' from the
triplestore. Can accept the entity and attribute either as two args or
as a single vector pair."
  ([store [entity att]] (get-triple-value store entity att))
  ([store entity att]
     (some identity (map
		     #(get % [entity att])
		     (rseq store)))))

(defn get-all-triples
  "Returns a tripleseq containing the most current value of each
  unique entity-attribute pair."
  [store]
  (map (fn [[[e a] v]] [e a v]) (reduce merge store)))

(defn- build-filter
  "Given a template (either ending with a * or a literal value),
return a predicate that will return true if either the template is *
or the parameter matches the template exactly."
  [t]
  (cond
   (= t "*")
   (constantly true)

   (.endsWith t "*")
   #(.startsWith % (.substring t 0 (dec (count t))))

   true
   #(= t %)))

(defn build-spec-filter
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
  "Given a store and a seq of triple template, return all triples that
  match the pattern. The pattern can contain either exact matches or
  wildcards (a prefix string followed by a literal '*'), which matches
  any item starting with the prefix. So, for example:

  (query store [\"*\" \"*\" \"*\"])

  returns all triples, and

  (query store [\"foo\" \"*\" \"bar\"])

  returns all triples that have an entity of foo and a value of bar,
  regardless of attribute, and

  (query store [\"player:*\" \"name\" \"*\"])

  returns all triples that have an entity that starts with \"player:\"
  and have an attribute of \"name\"."
  [store & triplespecs]
  (filter (build-specs-filter triplespecs) (get-all-triples store)))

(defn query-values
  "Like query, but returns only the values, not the triples."
  [store & triplespecs]
  (map value (query store triplespecs)))

