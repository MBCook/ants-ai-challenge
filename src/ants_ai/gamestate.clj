(ns ants-ai.gamestate
    "Functions for creating and manipulating the gamestate"
    (:require [ants-ai.defines :as defines]
              [ants-ai.interface :as interface]))

(def init-state {:turn 0
                 :water #{}
                 :dead #{}
                 :ants #{}
                 :enemie-ants #{}
                 :hills #{}
                 :enemy-hills #{}
                 :food #{}})

;(defn- update-hills
;  "Return an updated version of the hills array, based on removing hills
;  that would have been razed by an ant on top."
;  [status razing-ant]
;  (setops/select #(let [[r c p] %]
;                        (not= [r c] razing-ant)) (:hills status)))

;(defn- update-enemy-hills
;  "Return an updated version of the enemy hills array, based us having razed a hill."
;  [status razing-ant]
;  (setops/select #(let [[r c p] %]
;                        (not                                                         ; ReFmove only things where...
;                          (and (= [r c] (take 2 razing-ant))                            ; The ant is on a hill
;                               (not= p (nth razing-ant 2))))) (:enemy-hills status)))   ; That doesn't belong to it

;(defn will-be-occupied?
;  "Given a bunch of [loc dir] pairs (where dir may not exist), see if the loc will be filled"
;  [cur-ants loc]
;  (when (or (contains-ant? cur-ants loc))
;            (contains-ant? (water) loc)
;        loc))

(defn update-tile
  "Given the current state and some new information, update the neccessary tile"
  [state {:keys [tile row col player]}]
  (let [loc [row col]
        ant (conj loc player)]
    (condp = tile
      :water (update-in state [:water] conj loc)
      :dead-ant (update-in state [:dead] conj ant)
      :ant (if (zero? player)
                (update-in state [:ants] conj loc)          ; Add/update the ant to the state we're tracking
                (update-in state [:enemy-ants] conj ant))       ; Add/update the ant to the state we're tracking
      :food (update-in state [:food] conj loc)
      :hill (if (zero? player)                                ; We've seen a new hill, remember it
                 (update-in state [:hills] conj loc)
                 (update-in state [:enemy-hills] conj ant)))))

(defn update-state
  "Given the current state and a new message, parse it out and return updated state"
  [state message]
  (cond
    (interface/message? :turn message) (merge init-state {:turn (interface/get-turn message)
                                                          :water (or (:water state) #{})})
    (interface/message? :tile message) (update-tile state (interface/parse-tile message))
    :else state))

(defn game-info
  "Get some value from the setup information of the game"
  [key]
  (defines/*game-info* key))

(defn turn-num
  "Get the turn number"
  []
  (:turn defines/*game-state*))

(defn my-ants
  "Get a set of all ants belonging to us"
  []
  (:ants defines/*game-state*))

(defn enemy-ants
  "Get a set of all enemy ants where an enemy ant is [row col player-num]"
  []
  (:enemie-ants defines/*game-state*))

(defn my-hills
  "Get a set of all ants belonging to us"
  []
  (:hills defines/*game-state*))

(defn enemy-hills
  "Get a set of all enemy ants where an enemy ant is [row col player-num]"
  []
  (:enemy-hills defines/*game-state*))

(defn food
  "Get a set of food locations"
  []
  (:food defines/*game-state*))

(defn water
  "Get a set of water locations"
  []
  (:water defines/*game-state*))
