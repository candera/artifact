(ns artifact.tuplestore
  "A simple tuple store with basic query capabilities. Might get replaced by something more capable at some point. Terminology:

* tuple: a sequence with three elements, entity, attribute, and
  value. Entities and attributes must be strings. Values can be
  strings, nil, integers, tuples, or tupleseqs.

* tupleseq: a sequence of tuples

* moment: a tupleseq containing in which no two tuples have the same
  entity and attribute. Always includes a logical tuple with entity
  \"global\" and attribute \"time\" whose value is an integer
  identifying the instant this moment represents. This integer is
  monotonically increasing for successive moments in a tuplestore.

* proposal: the combination of a tuplestore and a tupleseq, which
  together define a proposed new moment.

* tuplestore: a sequence of moments.

* tuplesource: a source of tuples. Currently either a tupleseq or a
  tuplestore.

* tuplespec: see the definition of query"
  (:refer-clojure :exclude [time]))

(defn tuple-atom?
  "Returns true if is a valid atomic value for a tuple. Valid atomic
  values for a tuple are strings, integers, booleans, and nil."
  [x]
  (or (string? x)
      (integer? x)
      (true? x)
      (false? x)
      (nil? x)))

(declare tuple?)
(declare tupleseq?)

(defn- tuple-value?
  "Returns true if x is a valud value for a tuple. Valid values
  include valid tuple atoms (see tuple-atom?), tuples, and
  sequences of those things."
  [x]
  (or (tuple-atom? x)
      (tuple? x)
      (and (sequential? x)
           (every? tuple-atom? x))
      (tupleseq? x)))

(defn time
  "Given a tuple, return its time"
  [tuple]
  (first tuple))

(defn entity
  "Given a tuple, return the entity"
  [tuple]
  (second tuple))

(defn attribute
  "Given a tuple, return the attribute"
  [tuple]
  (nth tuple 2))

(defn value
  "Given a tuple, return the value"
  [tuple]
  (nth tuple 3))

(defn tuple?
  "Returns true if its argument is a valid tuple."
  [x]
  (and (sequential? x)
       (= (count x) 4)
       (or (integer? (time x))
           (= :now x))
       (string? (entity x))
       (string? (attribute x))
       (tuple-value? (value x))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn coalesce
  "Returns tupleseq containing the latest value of each tuple"
  [tupleseq]
  (->> tupleseq
       (map (fn [[t e a v]] [[e a] [t v]]) ,,,)
       (reduce merge {} ,,,)
       (map (fn [[[e a] [t v]]] [t e a v])) ,,,))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
  "Given a single tuplespec (see query) build a predicate that will
return true for any tuple that matches the spec."
  [tuplespec]
  (let [[e-spec a-spec v-spec] tuplespec]
    (fn [[_ e a v]]
      (and ((build-filter e-spec) e)
	   ((build-filter a-spec) a)
	   ((build-filter v-spec) v)))))

(defn query
  "Given a store or a tupleseq and a tuplespec, return a tupleseq
of all tuples that match the pattern defined by the tuplespec. The
pattern can contain exact matches, regular expressions, or the
keyword :any, which matches any value. So, for example:

  (query store [:any :any :any])

returns all tuples, and

  (query store [\"foo\" :any #\"^bar.*\"])

returns all tuples that have an entity of foo and a value of bar,
regardless of attribute, and

  (query store [#\"^player:.*\" \"name\" :any])

returns all tuples that have an entity that starts with \"player:\"
and have an attribute of exactly \"name\"."
  [tupleseq tuplespec]
  (filter (build-spec-filter tuplespec) tupleseq))

(defn query-values
  "Like query, but returns a seq of the values, not the tuples."
  [tuplesource tuplespec]
  (map value (query tuplesource tuplespec)))

(defn get-tuple-value
  "Returns the value of attribute 'att' for entity 'entity' from the
tuplesource. Can accept the entity and attribute either as two args or
as a single vector pair."
  ([tuplesource [entity att]] (get-tuple-value tuplesource entity att))
  ([tuplesource entity att]
     (value (first (query tuplesource [entity att :any])))))

(defn tupleseq?
  "Returns true if x is a tupleseq"
  [x]
  (and (sequential? x)
       (every? tuple? x)))

(defn- not-implemented []
  (throw (Exception. "Not yet implemented")))

(defn single?
  "Returns true for a seq with a single item in it."
  [x]
  (and (seq x) (nil? (seq (rest x)))))

(defn single
  "Given a seq, returns the single item it is made up of. Throws otherwise."
  [x]
  {:pre ((single? x))}
  (first x))

(defn- max-time
  "Given a tupleseq, return the maximum integer time, or -1 if the seq
  contains no tuples with integer time."
  [tupleseq]
  (->> tupleseq
      (map time)
      (filter integer?)
      (reduce max -1)))

(defn reify-moment
  "Given a tupleseq, replace all the time values of :now with an
  integer one greater than the max time value of all other tuples."
  [tupleseq]
  (let [next-time (inc (max-time tupleseq))]
    (map (fn [[t e a v]] [(if (= :now t) next-time t) e a v])
         tupleseq)))
