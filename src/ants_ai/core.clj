(ns ants-ai.core
    "The core of the program"
    (:require [ants-ai.interface :as interface]
              [ants-ai.defines :as defines]
              [ants-ai.gamestate :as gamestate]))

(defn start-game
  "Play the game with the given bot."
  ([bot]
    (start-game *in* bot))
  ([input-stream bot]
    (binding [*in* input-stream]
      (when (interface/message? :turn (read-line))
        (binding [defines/*game-info* (interface/build-game-info)]
          (println "go") ; we're "setup" so let's start
          (loop [cur (read-line)
                 state {}]
            (if (interface/message? :end cur)
              nil   ; This is where collect-stats was
              (do
                (when (interface/message? :go cur)
                  (binding [defines/*game-state* state]
                    (bot)
                    (println "go")))
                (recur (read-line) (gamestate/update-state state cur))))))))))