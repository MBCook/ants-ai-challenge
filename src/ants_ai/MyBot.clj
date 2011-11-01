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
    (first dirs)
    ))

(defn process-ant
  "Take the given ant and figure out a move for them, returned as [ant dir result]"
  [ant]
  (let [valid-directions (filter #(utilities/valid-move? ant %) defines/directions)
        towards-food (move-towards-food ant)
        dir (if (nil? towards-food)
                (pick-random-direction ant)                       ; No food? Go crazy
                (if (some #(= % towards-food) valid-directions)
                    towards-food                                  ; Valid move towards food? Go for it
                    (pick-random-direction ant)))                 ; Can't move towards food? Go crazy
        result (if (nil? dir)
                    nil
                    (utilities/move-ant ant dir))]
    (if (nil? dir)
      [ant nil ant]
      [ant dir result])))

(defn process-ants-for-moves
  "Process each ant in turn, gathering up their moves in the form [loc dir result]"
  []
  (loop [ants (gamestate/my-ants)
         moves []]
    (if (empty? ants)
      moves
      (let [ant (first ants)
            ants-move (process-ant ant)]
        (recur (rest ants) (conj moves ants-move))))))

(defn simple-bot []
  "Core loop for the bot"
  (doseq [[ant dir res] (process-ants-for-moves)]
    (when dir
      (interface/issue-move ant dir))))

(core/start-game simple-bot)