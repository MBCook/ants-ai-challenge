(ns ants-ai.defines
    "Symbols that will be useful throughout the program")

; Symbols that will be useful throughout the program

(declare ^{:dynamic true} *game-info*)
(declare ^{:dynamic true} *game-state*)
(declare ^{:dynamic true} *log-file*)

; Per turns stuff
;(def ^{:dynamic true} *food-diffusion-map* (atom {}))
;(def ^{:dynamic true} *food-reservations* (atom {}))    ; loc -> set of ants
(def ^{:dynamic true} *current-defense* (atom nil))       ; Defense shape in use
(def ^{:dynamic true} *positions-to-fill* (atom #{}))     ; Defense spots we need to fill, set of locations
(def ^{:dynamic true} *positions-unfilled* (atom {}))     ; Defense spots that we still haven't filled, set of locations

; Permanent stuff
(def ^{:dynamic true} *ant-last-moves* (atom {}))
(def ^{:dynamic true} *seeded-rng* (atom nil))
(def ^{:dynamic true} *defense-positions* (atom {}))    ; loc -> ant

(def logging-enabled false)
(def visualizer-enabled false)

(def directions #{:north :east :west :south})
(def opposite-directions {:north :south, :east :west, :west :east, :south :north})

(def direction-symols {:north "N"
                        :south "S"
                        :east "E"
                        :west "W"})

(def direction-offsets {:north [-1 0]
                         :west [0 -1]
                         :south [1 0]
                         :east [0 1]})

(def offset-directions {[-1 0] :north
                         [0 -1] :west
                         [1 0] :south
                         [0 1] :east})

(def four-point-defense (list [-1 -1] [-1 1] [1 1] [1 -1]))
(def twelve-point-defense (list [-1 -1] [-2 -1] [-1 -2] [-1 1] [-2 1] [-1 2]
                                [1 1] [2 1] [1 2] [1 -1] [2 -1] [1 -2]))
(def sixteen-point-defense (list [-1 -1] [-2 -1] [-1 -2] [-2 -2] [-1 1] [-2 1] [-1 2] [-2 2]
                                  [1 1] [2 1] [1 2] [2 2] [1 -1] [2 -1] [1 -2] [2 -2]))

(def messages {:ready #"ready"
               :turn #"turn [0-9]+"
               :end #"end"
               :go #"go"
               :tile #"\w \d+ \d+"})

(def map-tiles {"f" :food
                "w" :water
                "a" :ant
                "d" :dead-ant
                "h" :hill})
