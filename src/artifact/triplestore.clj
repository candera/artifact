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

(defn v
  "Given a [[entity attribute] value] tuple returns a triple as a
  [entity attribute value] vector."
  [triple]
  (let [[[entity att] val] triple]
    [entity att val]))