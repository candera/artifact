(ns artifact.triplestore)

;;; A vector of moments, each of which is a map that takes an entity
;;; and an attribute to a value. So like this:
;;; [ { [entity att] val     ; Moment 0
;;      [entity att] val } ]
;;    { [entity att] val } ]  ; Moment 1
;;;  etc
;;; ]
(def ^{:private true} triplestore (atom []))

(defn- triples-to-map [triples]
  (reduce #(assoc %1 (subvec %2 0 2) (nth %2 2))
	  {}
	  triples))

(def ^{:private true} time-key ["global" "time"])

(defn latest-time [triplestore]
  (get (last triplestore) time-key -1))

(defn- conj-new-moment
  "Produces a new triplestore that adds the specified triples, but
  also adds a new value for entity 'game' attribute 'time' that's one
  more than the latest one."
  [triplestore triples]
  (let [new-game-time (inc (latest-time triplestore))]
    (conj triplestore
	  (assoc (triples-to-map triples) time-key new-game-time))))

(defn reset-triplestore []
  (reset! triplestore []))

(defn add-triples [& triples]
  (swap! triplestore conj-new-moment triples))

(defn get-triple-value [entity att]
  (some
   identity
   (map
    #(get % [entity att])
    (rseq @triplestore))))