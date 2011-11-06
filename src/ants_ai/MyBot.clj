(ns ants-ai.MyBot
    "The core of our bot"
    (:require [clojure.set :as set]
              [ants-ai.interface :as interface]
              [ants-ai.defines :as defines]
              [ants-ai.utilities :as utilities]
              [ants-ai.gamestate :as gamestate]
              [ants-ai.gameinfo :as gameinfo]
              [ants-ai.core :as core]))

; Move functions take only an ant.
; They return a collection of directions that it would like to move this turn (possibly nil/empty)

(defn move-in-random-direction
  "Pick a random valid direction"
  [ant]
  defines/directions)

(defn move-towards-food
  "Move towards the closest piece of food"
  [ant]
  (when (not-empty (gamestate/food))
    (let [food-distances (map #(vector % (utilities/distance-no-sqrt ant %)) (gamestate/food))
          food (sort-by #(second %) food-distances)
          best-spot (first (first food))]
      (utilities/direction ant best-spot))))

(defn move-away-from-enemy
  "Move away from the closest enemy"
  [ant]
  (when (not-empty (gamestate/enemy-ants))
    (let [ant-distances (map #(vector % (utilities/distance-no-sqrt ant %)) (gamestate/enemy-ants))
          ants (sort-by #(second %) (filter #(<= (second %) (max 9 (gameinfo/attack-radius-squared))) ant-distances))]
      (when (not-empty ants)
        (let [worst-ant (first (first ants))]
          (set/difference (set defines/directions) (set (utilities/direction ant worst-ant))))))))

(defn move-to-capture-hill
  "Move towards an enemy hill the ant can see"
  [ant]
  (when (not-empty (gamestate/enemy-hills))
    (let [hill-distances (map #(vector % (utilities/distance-no-sqrt ant %)) (gamestate/enemy-hills))
          hills (sort-by #(second %) (filter #(<= (second %) (gameinfo/view-radius-squared)) hill-distances))]
      (when (not-empty hills)
        (let [best-spot (first (first hills))]
          (utilities/direction ant best-spot))))))

(defn find-move-through-functions
  "Run each function in turn for the ant, return the first non-nil direction we find that's valid"
  [ant valid-directions]
  (when (not-empty valid-directions))
    (loop [functions-to-run {move-to-capture-hill :capture        ; First capture any hills we can see
                            move-away-from-enemy :run-away        ; Get away from nearby enemy ants
                            move-towards-food :food               ; Then go for the closest food
                            move-in-random-direction :random}]    ; Nothing better? Go in a random direction
      (if (not-empty functions-to-run)
        (let [the-function-stuff (first functions-to-run)
              the-function (key the-function-stuff)
              the-function-purpose (val the-function-stuff)
              result (apply the-function ant [])]
          (if (empty? result)
            (recur (rest functions-to-run))                         ; No decision, try the next function
            (let [moves-to-choose-from (set/intersection (set result) (set valid-directions))
                  dir (when (not-empty moves-to-choose-from)
                        (rand-nth (vec moves-to-choose-from)))]
              (if dir                                               ; Was one of the moves valid?
                (do
                  (utilities/debug-log "Ant at " ant " doing " the-function-purpose ", going " dir)
                  dir)
                (recur (rest functions-to-run)))))))))

(defn process-ant
  "Take the given ant and figure out a move for them, returned as [ant dir result]"
  [ant occupied-locations]
  (let [valid-directions (filter #(utilities/valid-move? ant %) defines/directions)
        dir (find-move-through-functions ant valid-directions)
        result (when dir
                (utilities/move-ant ant dir))]
    (cond
      (nil? dir)                                                  ; No valid moves? Stand still
        [ant nil ant]
      (utilities/contains-ant? occupied-locations result)         ; Make sure we won't run into one of our other ants
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
  (utilities/debug-log "Enemy ants: " (gamestate/enemy-ants))
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