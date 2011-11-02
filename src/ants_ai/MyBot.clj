(ns ants-ai.MyBot
    "The core of our bot"
    (:require [ants-ai.interface :as interface]
              [ants-ai.defines :as defines]
              [ants-ai.utilities :as utilities]
              [ants-ai.gamestate :as gamestate]
              [ants-ai.core :as core]))

(defn pick-random-direction
  "Pick a random valid direction"
  [ant]
  (let [valid-directions (filter #(utilities/valid-move? ant %) defines/directions)]
    (if (empty? valid-directions)
        nil
        (rand-nth valid-directions))))

(defn move-towards-food
  "Move towards the closest piece of food"
  [ant]
  (let [food-distances (map #(vector % (utilities/distance ant %)) (gamestate/food))
        food (sort-by #(second %) food-distances)
        best-spot (first (first food))
        dirs (utilities/direction ant best-spot)]
    (first dirs)))

(defn process-ant
  "Take the given ant and figure out a move for them, returned as [ant dir result]"
  [ant occupied-locations]
  (let [valid-directions (filter #(utilities/valid-move? ant %) defines/directions)
        towards-food (move-towards-food ant)                      ; Directions that will move us to the closest food
        dir (cond
              (nil? towards-food)                                 ; Can't go towards food? Go crazy
                (pick-random-direction ant)
              (some #(= % towards-food) valid-directions)         ; Go towards food
                towards-food
              :else                                               ; No idea what to do, go crazy
                (pick-random-direction ant))
        result (when dir
                (utilities/move-ant ant dir))]
    (cond
      (nil? dir)                                                  ; No valid moves? Stand still
        [ant nil ant]
      (utilities/contains-ant? occupied-locations result)
        (do
          (utilities/debug-log "Ant at " ant " avoiding collision at " result)
          [ant nil ant])
      :else                                                       ; We're good
        [ant dir result])))

(defn process-ants-for-moves
  "Process each ant in turn, gathering up their moves in the form [loc dir result]"
  []
  (utilities/debug-log "")
  (utilities/debug-log "New turn")
  (loop [ants (gamestate/my-ants)         ; Ants we're processing
         moves []]                        ; Moves we'll be making
    (if (empty? ants)                     ; Out of ants? We're done
      moves
      (let [ant (first ants)                                              ; Ant we're working with
            occupied-locations (into (rest ants) (map #(last %) moves))   ; Locations to consider to be occupied
            ants-move (process-ant ant occupied-locations)                ; Figure out a move
            result (last ants-move)]
        (utilities/debug-log ant " moving " (second ants-move) " to " result ", occupied " occupied-locations)
        (recur (rest ants)                    ; Ants left to process
                (conj moves ants-move))))))   ; Moves updated with our new move

(defn simple-bot []
  "Core loop for the bot"
  (doseq [[ant dir res] (process-ants-for-moves)]
    (when dir
      (interface/issue-move ant dir))))

(binding [defines/*log-file* (java.io.FileWriter.
                                "/Users/michael/Programming/Ants AI Challenge/tools/game_logs/my-log.txt"
                                false)]
  (core/start-game simple-bot)
  (.close defines/*log-file*))