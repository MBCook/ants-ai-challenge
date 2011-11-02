(ns ants-ai.defines
    "Symbols that will be useful throughout the program")

; Symbols that will be useful throughout the program

(declare ^{:dynamic true} *game-info*)
(declare ^{:dynamic true} *game-state*)
(declare ^{:dynamic true} *log-file*)

(def directions [:north :east :west :south])

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