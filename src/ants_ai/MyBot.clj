(ns ants-ai.MyBot
    "The core of our bot"
    (:require [ants-ai.interface :as interface]
              [ants-ai.defines :as defines]
              [ants-ai.utilities :as utilities]
              [ants-ai.gamestate :as gamestate]
              [ants-ai.core :as core]))

(defn process-ant
  "Take the given ant and figure out a move for them, returned as [ant dir result]"
  [ant]
  (let [valid-directions (filter #(utilities/valid-move? ant %) defines/directions)
        dir (if (empty? valid-directions)
                  nil
                  (rand-nth valid-directions))
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