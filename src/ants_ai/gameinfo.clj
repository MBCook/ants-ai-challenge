(ns ants-ai.gameinfo
    "Functions for reading the gameinfo"
    (:require [ants-ai.defines :as defines]))

(defn view-radius-squared
  "Get the view radius (squared)"
  []
  (:viewradius2 defines/*game-info*))

(defn attack-radius-squared
  "Get the attack radius (squared)"
  []
  (:attackradius2 defines/*game-info*))