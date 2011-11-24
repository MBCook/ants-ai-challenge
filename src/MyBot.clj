(ns MyBot
    "The core of our bot"
    (:require [clojure.set :as set]
              [ants-ai.interface :as interface]
              [ants-ai.defines :as defines]
              [ants-ai.diffusion :as diffusion]
              [ants-ai.utilities :as utilities]
              [ants-ai.gamestate :as gamestate]
              [ants-ai.gameinfo :as gameinfo]
              [ants-ai.core :as core]))

; Move functions take only an ant.
; They return a set of directions that it would like to move this turn (possibly nil/empty)

(defn move-in-random-direction
  "Pick a random valid direction"
  [ant valid-directions last-move]
  (if (contains? valid-directions last-move)
    #{last-move}                                  ; Prefer the direction the ant was already going in
    valid-directions))

;(defn move-towards-food-diffusion
;  "Move towards the closest piece of food the ant can see"
;  [ant _ _]
;  (let [[_ dir] (get @defines/*food-diffusion-map* ant)]
;    (when dir
;      #{dir})))

;(defn move-towards-food-res
;  "Move towards the closest piece of food the ant can see, with reservations"
;  [ant _ _]
;  (when (not-empty (gamestate/food))
;    (let [food-distances (map #(vector % (utilities/distance-no-sqrt ant %)) (gamestate/food))
;          food (sort-by #(second %) (filter #(<= (second %) (gameinfo/view-radius-squared)) food-distances))
;          best-spot (first (some #(let [reservations (@defines/*food-reservations* (first %))]  ; Gets first food square
;                                    (when (< reservations 2)                                    ; with less than 2 ants going
;                                      %))                                                       ; after it
;                                  food))]
;      (when best-spot
;        (swap! defines/*food-reservations* #(assoc % best-spot (inc (% best-spot))))        ; Make a reservation
;        (interface/visualizer-color :food)
;        (interface/visualize-arrow ant best-spot)
;        (utilities/direction ant best-spot)))))                                             ; Send the move on

(defn move-towards-food-classic
  "Move towards the closest piece of food the ant can see"
  [ant _ _]
  (when (not-empty (gamestate/food))
    (let [food-distances (map #(vector % (utilities/distance-no-sqrt ant %)) (gamestate/food))
          food (sort-by #(second %) (filter #(<= (second %) (gameinfo/view-radius-squared)) food-distances))
          water-test-fn #(contains? (gamestate/water) %)
          visible-food (filter #(utilities/is-line-of-site-clear? ant (first %) water-test-fn) food)
          best-spot (first (first visible-food))]
      (when best-spot
;        (interface/visualizer-color :food)
;        (interface/visualize-arrow ant best-spot)
        (utilities/direction ant best-spot)))))                                             ; Send the move on

(defn move-away-from-enemy
  "Move away from the closest enemy"
  [ant _ _]
  ; This rule doesn't apply if we are in visible range of one of our hills, or there are no enemies we know of
  (when (or (empty (gamestate/enemy-ants))
            (and (not-empty (gamestate/my-hills))
                 (some #(<= (second %) (gameinfo/view-radius-squared))
                    (map #(utilities/distance-no-sqrt ant %) (gamestate/my-hills)))))
    nil)
  ; The rule also doesn't apply if there are no enemy ants
  (let [ant-distances (map #(vector % (utilities/distance-no-sqrt ant %)) (gamestate/enemy-ants))
        ants (sort-by #(second %) (filter #(<= (second %) (max 16 (gameinfo/attack-radius-squared))) ant-distances))]
    (when (not-empty ants)
      (let [worst-ant (first (first ants))]
        (interface/visualizer-color :ant)
        (interface/visualize-arrow worst-ant ant)
        (set/difference defines/directions (utilities/direction ant worst-ant))))))

(defn move-to-capture-hill
  "Move towards an enemy hill the ant can see"
  [ant _ _]
  (when (not-empty (gamestate/enemy-hills))
    (let [hill-distances (map #(vector % (utilities/distance-no-sqrt ant %)) (gamestate/enemy-hills))
          hills (sort-by #(second %) (filter #(<= (second %) (gameinfo/view-radius-squared)) hill-distances))
          water-test-fn #(contains? (gamestate/water) %)
          visible-hills (filter #(utilities/is-line-of-site-clear? ant (first %) water-test-fn) hills)
          visible-hills hills
          best-spot (first (first visible-hills))]
      (when best-spot
        (interface/visualizer-color :hill)
        (interface/visualize-arrow ant best-spot)
        (utilities/direction ant best-spot)))))

(defn find-move-through-functions
  "Run each function in turn for the ant, return the first non-nil direction we find that's valid"
  [ant valid-directions ants-last-move]
  (when (not-empty valid-directions))
    (loop [functions-to-run {move-to-capture-hill :capture        ; First capture any hills we can see
                            move-away-from-enemy :run-away        ; Get away from nearby enemy ants
                            move-towards-food-classic :food       ; Then go for the closest food
                            move-in-random-direction :random}]    ; Nothing better? Go in a random direction
      (if (not-empty functions-to-run)
        (let [the-function-stuff (first functions-to-run)
              the-function (key the-function-stuff)
              the-function-purpose (val the-function-stuff)
              result (apply the-function [ant valid-directions ants-last-move])]
          (if (empty? result)
            (recur (rest functions-to-run))                         ; No decision, try the next function
            (let [moves-to-choose-from (set/intersection result valid-directions)
                  dir (when (not-empty moves-to-choose-from)
                        (utilities/seeded-rand-nth (vec moves-to-choose-from)))]
              (if dir                                               ; Was one of the moves valid?
                (do
                  (utilities/debug-log "Ant at " ant " doing " the-function-purpose ", going " dir)
                  (interface/visualize-info ant (str "Reason: " the-function-purpose))
                  (interface/visualize-info ant (str "Valid moves: " valid-directions))
                  (interface/visualize-info ant (str "Direction: " dir))
                  (interface/visualize-info ant (str "Last move: " ants-last-move))
                  dir)
                (recur (rest functions-to-run)))))))))

(defn process-ant
  "Take the given ant and figure out a move for them, returned as [ant dir result]"
  [ant occupied-locations]
  (let [valid-moves (filter #(utilities/valid-move? ant % occupied-locations) defines/directions)  ; Ways ant could move
        ants-last-move (@defines/*ant-last-moves* ant)                          ; The way the ant last moved
        ants-way-back (defines/opposite-directions ants-last-move)
        valid-directions (if (= (list ants-way-back) valid-moves)               ; Be sure that if we only have one valid
                            (set valid-moves)                                   ; move that it's always available
                            (set (filter #(not= % ants-way-back) valid-moves)))
        dir (find-move-through-functions ant valid-directions ants-last-move)   ; The above is so our ant won't move backwards
        result (when dir
                (utilities/move-ant ant dir))]
    (cond
      (nil? dir)                                                  ; No valid moves? Stand still
        [ant nil ant]
      :else                                                       ; We're good
        [ant dir result])))

(defn reset-per-turn-atoms
  "Resets the atoms that only make sense during a turn"
  []
;  (reset! defines/*food-map* (diffusion/diffuse-across-map (gamestate/food)
;                                                            (gamestate/water)
;                                                            9))
  (reset! defines/*food-reservations* (loop [reservations {}
                                             food-to-go (gamestate/food)]
                                        (if (empty? food-to-go)
                                          reservations
                                          (recur (assoc reservations (first food-to-go) 0) (rest food-to-go))))))

(defn process-ants-for-moves
  "Process each ant in turn, gathering up their moves in the form [loc dir result]"
  []
  (utilities/debug-log "")
  (utilities/debug-log "New turn")
;  (reset-per-turn-atoms)
  (loop [ants (gamestate/my-ants)         ; Ants we're processing
         moves []]                        ; Moves we'll be making (a list and not a set because order matters)
    (if (empty? ants)                     ; Out of ants? We're done
      moves
      (let [ant (first ants)                                              ; Ant we're working with
            occupied-locations (into (rest ants) (map #(last %) moves))   ; Locations to consider to be occupied
            ants-move (process-ant ant occupied-locations)                ; Figure out a move
            result (last ants-move)]
        (utilities/debug-log ant " moving " (second ants-move) " to " result)
        (recur (rest ants)                    ; Ants left to process
                (conj moves ants-move))))))   ; Moves updated with our new move

(defn simple-bot []
  "Core loop for the bot"
  (interface/setup-visualizer)
  (reset! defines/*seeded-rng* (new java.util.Random (gameinfo/rand-seed)))
  (doseq [[ant dir res] (process-ants-for-moves)]
    (when dir
      (interface/issue-move ant dir)                      ; Issue the move to the server
      (swap! defines/*ant-last-moves* assoc ant nil)      ; Forget the last move at the ant's old position
      (swap! defines/*ant-last-moves* assoc res dir))))   ; Remember which way the ant went to their new position

(if defines/logging-enabled
  (binding [defines/*log-file* (java.io.FileWriter.
                                  "/Users/michael/Programming/Ants AI Challenge/tools/game_logs/my-log.txt"
                                  false)]
    (core/start-game simple-bot)
    (.close defines/*log-file*))
  (core/start-game simple-bot))
