(ns artifact.triplestore)

;;; A vector of moments, each of which is a vector of triples. So like
;;; this:
;;; [
;;;   [ [ entity att val ]    ; Moment 0
;;      [ entity att val ] ]
;;    [ [ entity att val ] ]  ; Moment 1
;;;  etc
;;; ]

(def ^{:private true} triplestore (atom []))

(defn add-triples [& triples]
  (swap! triplestore conj triples))

(defn get-triple-value [entity att]
  (first
   ;; TODO: Figure out how to retrieve the last triple that matches a
   ;; given entity and attribute
   (fn [moment]
     (filter ))
   (rseq triplestore)))