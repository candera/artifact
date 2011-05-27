(ns artifact.tuplestore
  "A simple tuple store with basic query capabilities. Might get replaced by something more capable at some point. Terminology:

* tuple: a sequence with four elements, time, entity, attribute, and
  value. Time can be nil (indicating 'now') or a non-negative integer.
  Entities and attributes must be strings. Values can be strings, nil,
  integers, tuples, booleans, or tupleseqs.

* tupleseq: a sequence of tuples

* tuplespec: see the definition of query"
  (:use artifact.util)
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
           (nil? (time x)))
       (string? (entity x))
       (string? (attribute x))
       (tuple-value? (value x))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn coalesce
  "Returns tupleseq containing the latest value of each tuple"
  [tupleseq]
  (->> tupleseq
       (map (fn [[t e a v]] [[e a] [t v]]))
       (reduce merge {})
       (map (fn [[[e a] [t v]]] [t e a v]))
       (sort)))

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
  (let [[t-spec e-spec a-spec v-spec] tuplespec]
    (fn [[t e a v]]
      (and ((build-filter t-spec) t)
           ((build-filter e-spec) e)
	   ((build-filter a-spec) a)
	   ((build-filter v-spec) v)))))

(defn query
  "Given a store or a tupleseq and a tuplespec, return a tupleseq
of all tuples that match the pattern defined by the tuplespec. The
pattern can contain exact matches, regular expressions, or the
keyword :any, which matches any value. So, for example:

  (query store [:any :any :any :any])

returns all tuples, and

  (query store [:any \"foo\" :any #\"^bar.*\"])

returns all tuples that have an entity of foo and a value of bar,
regardless of attribute, and

  (query store [:any #\"^player:.*\" \"name\" :any])

returns all tuples that have an entity that starts with \"player:\"
and have an attribute of exactly \"name\"."
  [tupleseq tuplespec]
  {:pre ((tupleseq? tupleseq) (= (count tuplespec) 4))}
  (filter (build-spec-filter tuplespec) tupleseq))

(defn query-values
  "Like query, but returns a seq of the values, not the tuples."
  [tupleseq tuplespec]
  (map value (query tupleseq tuplespec)))

(defn get-latest-value
  "Returns the latest value of attribute 'att' for entity 'entity'
  from the tupleseq."
  [tupleseq entity att]
  (value (last (query tupleseq [:any entity att :any]))))

(defn tupleseq?
  "Returns true if x is a tupleseq"
  [x]
  (and (sequential? x)
       (every? tuple? x)))

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
  "Given a tupleseq, return the maximum time, or -1 if the seq
  contains no tuples with integer time."
  [tupleseq]
  (->> tupleseq
       (map time)
       (filter integer?)
       (reduce max -1)))

(defn update-nil-time
  "Given a tupleseq replace all the nil time values with a time one
  greater than the largest existing time, or zero if there are no
  non-nil times."
  [tupleseq]
  (let [new-time (inc (max-time tupleseq))]
    (map (fn [[t e a v]] [(if (nil? t) new-time t) e a v])
         tupleseq)))
