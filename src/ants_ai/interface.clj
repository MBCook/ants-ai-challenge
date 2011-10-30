(ns ants-ai.interface
    "Functions for interfacing our code to Google's that runs the game"
    (:require [clojure.string :as string]
              [ants-ai.defines :as defines]))

(defn parse-tile
  "Given a message about a tile, parse it out and figure out what we might need"
  [message]
  (let [[tile row col player :as parts] (string/split (string/lower-case message) #" ")
        player (when player
                     (Integer. player))]
    {:tile (defines/map-tiles tile)
     :row (Integer. row)
     :col (Integer. col)
     :player player}))

(defn message?
  "Check if the message is of the tiven message type"
  [message-type message]
  (re-seq (defines/messages message-type) (string/lower-case message)))


(defn build-game-info
  "Read from "
  ([]
    (build-game-info *in*))
  ([input-stream]
    (let [*in* input-stream]
      (loop [cur (read-line)
             info {}]
        (if (message? :ready cur)
          info
          (let [[k v] (string/split cur #" ")
                neue (assoc info (keyword k) (BigInteger. v))]
            (recur (read-line) neue)))))))

(defn get-turn
  "Given a message about what turn it is, get that number"
  [message]
  (Integer. (or (second (string/split message #" ")) 0)))

(defn issue-move
  "Issue a move command for the given ant, where the ant is [row col] and dir
  is [:north :south :east :west]"
  [[row col :as ant] dir]
  (println "o" row col (defines/direction-symols dir)))