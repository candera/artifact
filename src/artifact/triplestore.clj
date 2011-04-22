(ns artifact.triplestore
  "A simple triple store with basic query capabilities. Might get replaced by something more capable at some point. Terminology:

* triple: a sequence with three elements, entity, attribute, and
  value. Entities and attributes must be strings. Values can be
  strings, nil, integers, triples, or tripleseqs.

* tripleseq: a sequence of triples

* moment: a tripleseq containing in which no two triples have the same
  entity and attribute. Always includes a logical triple with entity
  \"global\" and attribute \"time\" whose value is an integer
  identifying the instant this moment represents. This integer is
  monotonically increasing for successive moments in a triplestore.

* proposal: the combination of a triplestore and a tripleseq, which
  together define a proposed new moment.

* triplestore: a sequence of moments.

* triplesource: a source of triples. Currently either a tripleseq or a
  triplestore.

* triplespec: see the definition of query")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol TripleSource
  "Implemented by types that can provide a tripleseq."
  (to-tripleseq [this] "Return a tripleseq"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: The problem with doing things this way is that we no longer
;; have a literal syntax for writing triples. I.e. we can't just say
;; ["foo" "bar" 3], but instead have to say (triple "foo" "bar" 3).
;; Same problem for tripleseqs. Is that really a big deal?

(defrecord Triple [entity attribute value])

(defn triple-atom?
  "Returns true if is a valid atomic value for a triple. Valid atomic
  values for a triple are strings, integers, booleans, and nil."
  [x]
  (or (string? x)
      (integer? x)
      (true? x)
      (false? x)
      (nil? x)))

(declare triple?)
(declare tripleseq?)

(defn- triple-value?
  "Returns true if x is a valud value for a triple. Valid values
  include valid triple atoms (see triple-atom?), triples, and
  sequences of those things."
  [x]
  (or (triple-atom? x)
      (triple? x)
      (and (sequential? x)
           (every? triple-atom? x))
      (tripleseq? x)))

(defn triple
  "Returns a new triple with the given entity, attribute, and value."
  [e a v]
  {:pre ((string? e) (string? a) (triple-value? v))}
  (Triple. e a v))

(defn entity
  "Given a triple, return the entity"
  [triple]
  (:entity triple))

(defn attribute
  "Given a triple, return the attribute"
  [triple]
  (:attribute triple))

(defn value
  "Given a triple, return the value"
  [triple]
  (:value triple))

(defn triple?
  "Returns true if its argument is a valid triple."
  [x]
  (and (= (class x) Triple)
       (string? (entity x))
       (string? (attribute x))
       (triple-value? (value x))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord TripleStore [moments])

(defn triplestore
  "Creates a new, empty triplestore."
  []
  (TripleStore. []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Proposal [store tripleseq])

(defn proposal
  "Creates a new proposal"
  [store tripleseq]
  (Proposal. store tripleseq))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: plan to extend this to Triplestore, Proposal, TripleSeq, and
;; maybe Triple.

(defn- build-filter
  "Given a template (a string, a regexp, or the symbol :any),
return a predicate that will return true if either the template
is :any, or the regexp matches, or the parameter matches the template
exactly."
  [t]
  (cond
   (= t :any)
   (constantly true)

   (= (class t) java.util.regex.Pattern)
   #(re-matches t %)

   true
   #(= t %)))

(defn build-spec-filter
  "Given a single triplespec (see query) build a predicate that will
return true for any tuple that matches the spec."
  [triplespec]
  (let [[e-spec a-spec v-spec] triplespec]
    (fn [[e a v]]
      (and ((build-filter e-spec) e)
	   ((build-filter a-spec) a)
	   ((build-filter v-spec) v)))))

(defn query
  "Given a store or a tripleseq and a triplespec, return a tripleseq
of all triples that match the pattern defined by the triplespec. The
pattern can contain exact matches, regular expressions, or the
keyword :any, which matches any value. So, for example:

  (query store [:any :any :any])

returns all triples, and

  (query store [\"foo\" :any #\"^bar.*\"])

returns all triples that have an entity of foo and a value of bar,
regardless of attribute, and

  (query store [#\"^player:.*\" \"name\" :any])

returns all triples that have an entity that starts with \"player:\"
and have an attribute of exactly \"name\"."
  [triplesource triplespec]
  (filter (build-spec-filter triplespec) (to-tripleseq triplesource)))

(defn query-values
  "Like query, but returns a seq of the values, not the triples."
  [triplesource triplespec]
  (map value (query triplesource triplespec)))

(defn get-triple-value
  "Returns the value of attribute 'att' for entity 'entity' from the
triplesource. Can accept the entity and attribute either as two args or
as a single vector pair."
  ([triplesource [entity att]] (get-triple-value triplesource entity att))
  ([triplesource entity att]
     (value (first (query triplesource [entity att :any])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: Implement Iterable? Or whatever interface lets me use filter
;; etc. diretly on a Tripleseq.
(defrecord TripleSeq [triples])

(defn tripleseq
  "Creates a new tripleseq given some triples"
  [triples]
  {:pre ((every? triple? triples))}
  (TripleSeq. triples))

(defn tripleseq?
  "Returns true if x is a tripleseq"
  [x]
  (= (class x) TripleSeq))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Moment [tripleseq time])

(defn moment
  "Creates a new moment given some triples and a time"
  [tripleseq time]
  {:pre ((tripleseq? tripleseq) (integer? time))}
  (Moment. tripleseq time))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- not-implemented []
  (throw (Exception. "Not yet implemented")))

(defmulti combine
  "Given two things, combine them together."
  (fn [a b] [(class a) (class b)]))

;; Given a store and a tripleseq, returns the store with a new moment
;; appended that adds the specified triples, but also adds a new value
;; for entity 'game' attribute 'time' that's one more than the latest
;; one.
(defmethod combine [TripleStore Moment]
  [triplestore moment]
  (not-implemented))

(defmethod combine [Moment TripleSeq]
  [moment tripleseq]
  (not-implemented))

(defmethod combine [TripleSeq Triple]
  [tripleseq triple]
  (not-implemented))

(defmethod combine [TripleSeq TripleSeq]
  [tripleseq tripleseq]
  (not-implemented))

(defmethod combine [TripleStore Moment]
  [store moment]
  (not-implemented))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn single?
  "Returns true for a seq with a single item in it."
  [x]
  (and (seq x) (nil? (seq (rest x)))))

(defn single
  "Given a seq, returns the single item it is made up of. Throws otherwise."
  [x]
  {:pre ((single? x))}
  (first x))

(def ^{:doc "A key that can be passed to get-triple-value to retrieve the current time."}
  time-key ["global" "time"])

(defn latest-time
  "Returns the value of global/time for the most recent moment, or -1
  of the triplestore is empty."
  [store]
  (not-implemented))


