(ns ants-ai.diffusion
    "Functions to deal with diffusion on the maps"
    (:require [ants-ai.defines :as defines]
              [ants-ai.gamestate :as gamestate]
              [ants-ai.gameinfo :as gameinfo]
              [ants-ai.utilities :as utilities]))

(defn seed-processing
  "Creates and returns a list with the associated values and no directions"
  [keys value]
  (map #(vector % [value nil]) keys))

(defn- run-diffusion
  "Helper function that actually does the diffusion. squares-to-process is expected to be a list of vectors in
    the form [loc [strength dir]]. The third argument is expected to be transient."
  [squares-to-process skip-squares map-in-progress start-value min-value delta-fn]
  (if (empty? squares-to-process)             ; Is there anything left to process? If not, we're done
    map-in-progress
    (let [[square-loc square-data] (first squares-to-process)
          [square-strength square-from] square-data
          still-to-process (rest squares-to-process)]
      (cond
        (get map-in-progress square-loc)                                    ; Is this a square we've already done?
          (recur still-to-process skip-squares map-in-progress start-value min-value delta-fn)
        (contains? skip-squares square-loc)                                 ; Is this a square we shouldn't mess with?
          (recur still-to-process skip-squares map-in-progress start-value min-value delta-fn)
        (<= square-strength min-value)                                      ; Have we hit the limit?
          (recur still-to-process
                  skip-squares
                  (assoc! map-in-progress square-loc [min-value square-from])
                  start-value min-value delta-fn)
        :else                                                               ; Normal square, mark neighbors for recur
          (let [new-value (delta-fn square-strength)
                ; The below is shorter than applying the opposite dir to each direction individually
                the-directions (disj defines/directions (defines/opposite-directions square-from))
                dirs-and-weights (map #([new-value %]) the-directions)
                ; A new list holding the new squares that need to go on the to-process list
                new-to-process (map #(vector (utilities/move-ant square-loc (defines/opposite-directions %))
                                              [new-value %])
                                    the-directions)]
            (recur (concat squares-to-process new-to-process)
                    skip-squares
                    (assoc! map-in-progress square-loc square-data)
                    start-value
                    min-value
                    delta-fn))))))

(defn diffuse-across-map
  "Runs the diffusion process. Takes sets. Retuns a map of location -> [strength dir-to-stronger].
   dir-to-stronger can be nil if that square is the maximum. Locations may not be in the map returned."
  ([source-squares bad-squares]
    (diffuse-across-map source-squares bad-squares 32))
  ([source-squares bad-squares start-value]
    (diffuse-across-map source-squares bad-squares start-value 1 dec))
  ([source-squares bad-squares start-value min-value delta-fn]
    (run-diffusion (seed-processing source-squares start-value)           ; Run the diffusion with the known squares,
                    bad-squares                                           ;   the squares that we can't fill in,
                    (transient {})                                        ;   an empty map for the first round of processing,
                    start-value                                           ;   the value the start squares hold,
                    min-value                                             ;   the value we can't go below (our end case),
                    delta-fn)))                                           ;   the function used to calculate new strengths

(defn convert-diffusion-row-to-strings
  "Convert one row to a list of strings (strengths, directions, blank)."
  [diffusions row-number]
  (list
    (apply str (for [c (range (gameinfo/map-columns))]                            ; 1 3 7 2 . .
                    (if (contains? (gamestate/water) [row-number c])
                      "X "
                      (str (utilities/coalesce (first (diffusions [row-number c])) ".") " "))))
    (apply str (for [c (range (gameinfo/map-columns))]                            ; E S S N . .
                    (str (utilities/coalesce (defines/direction-symols (second (diffusions [row-number c]))) ".") " ")))
    (apply str (for [c (range (gameinfo/map-columns))] "  "))))                   ; (just spaces to make reading easier

(defn convert-diffusion-to-strings
  "Given a map of diffusion values, converts it into a list of lists of strings"
  [diffusions]
  (apply concat (for [r (range (gameinfo/map-rows))] (convert-diffusion-row-to-strings diffusions r))))


