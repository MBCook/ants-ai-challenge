(ns ants-ai.MyBot
    "The core of our bot"
    (:require [ants-ai.interface :as interface]
              [ants-ai.defines :as defines]
              [ants-ai.utilities :as utilities]
              [ants-ai.gamestate :as gamestate]
              [ants-ai.core :as core]))

(defn simple-bot []
  (doseq [ant (gamestate/my-ants)]
    (let [dir (first (filter #(utilities/valid-move? ant %) defines/directions))]
      (when dir
        (interface/issue-move ant dir)))))

(core/start-game simple-bot)