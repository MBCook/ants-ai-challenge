(ns ants-ai.diffusion
    "Functions to deal with diffusion on the maps"
    (:require [ants-ai.defines :as defines]
              [ants-ai.gamestate :as gamestate]
              [ants-ai.gameinfo :as gameinfo]
              [ants-ai.utilities :as utilities]))

(defn- run-diffusion
  "Helper function that actually does the diffusion. squares-to-process is expected to map a map like we return.
    The first and third arguments are expected to be transient."
  [squares-to-process skip-squares map-in-progress start-value min-value delta-fn]
  (if (empty? squares-to-process)             ; Is there anything left to process? If not, we're done
    map-in-progress
    (let [square (first squares-to-process)
          square-loc (key square)
          [square-strength square-from] (val square)]
      (cond
        (get map-in-progress square-loc)                                    ; Is this a square we've already done?
          (recur (dissoc! squares-to-process square-loc)
                skip-squares map-in-progress start-value min-value delta-fn)
        (contains? skip-squares square-loc)                                 ; Is this a square we shouldn't mess with?
          (recur (dissoc! squares-to-process square-loc)
                skip-squares map-in-progress start-value min-value delta-fn)
        (<= square-strength min-value)                                      ; Have we hit the limit?
          (recur (dissoc! squares-to-process square-loc)
                  skip-squares
                  (assoc! map-in-progress square-loc [min-value square-from])
                  start-value min-value delta-fn)
        :else                                                               ; Normal square, mark neighbors for recur
          (let [new-value (delta-fn square-strength)
                ; The below is shorter than applying the opposite dir to each direction individually
                the-directions (dissoc defines/directions (defines/opposite-directions square-from))
                dirs-and-weights (map #([new-value %]) the-directions)
                ; An updated version of squares-to-process with the three new neighbors added
                updated-to-process (loop [to-go dirs-and-weights
                                          the-map (dissoc! squares-to-process square-loc)]
                                    (if (empty? to-go)
                                      the-map
                                      (recur (rest to-go)
                                              (assoc! the-map (utilities/move-ant
                                                                square-loc
                                                                (second (value (first (to-go)))))))))]
            (recur squares-to-process
                    skip-squares
                    (assoc! map-in-progress square)
                    start-value
                    min-value
                    delta-fn))))))

(defn diffuse-across-map
  "Runs the diffusion process. Takes sets. Retuns a map of location -> [strength dir-to-stronger].
   dir-to-stronger can be nil if that square is the maximum. Locations may not be in the map returned."
  ([source-squares bad-squares]
    (recur source-squares bad-squares 32))
  ([source-squares bad-squares start-value]
    (recur source-squares bad-squares 32 1 dec))
  ([source-squares bad-squares start-value min-value delta-fn]
    (run-diffusion (utilities/seed-map  source-squares start-value)       ; Run the diffusion with the known squares,
                    bad-squares                                           ;   the squares that we can't fill in,
                    (transient {})                                        ;   an empty map for the first round of processing,
                    start-value                                           ;   the value the start squares hold,
                    min-value                                             ;   the value we can't go below (our end case),
                    delta-fn)                                             ;   the function used to calculate new strengths
    (recur (assoc! source-map (first source-left) [start-value]) (rest source-left))))