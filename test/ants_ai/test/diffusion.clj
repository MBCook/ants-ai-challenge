(ns ants-ai.test.diffusion
  (:use [ants-ai.diffusion])
  (:use [clojure.test])
  (:require [ants-ai.defines :as defines]))

(deftest test-seed-processing
  "Tests that seed-processing works as we'd expect"
  (are [result keys value] (= result (seed-processing keys value))
    (list) nil nil
    (list [:a [nil nil]]) [:a] nil
    (list [:a [1 nil]] [:b [1 nil]] [:c [1 nil]]) [:a :b :c] 1))

;;;;;;;;;;;;;;;;;;;;;;
;; Very simple game ;;
;;;;;;;;;;;;;;;;;;;;;;

(def sample-game-info-basic {:rows 5
                              :cols 5})
(def sample-game-state-basic {:water #{}})

(def sample-game-food-basic (list [2 2]))

; The below is functionally identical to if the nil valued keys didn't exist (for our purposes)
(def sample-diffusion-basic
  {[0 0] nil,       [0 1] nil,        [0 2] nil,        [0 3] nil,        [0 4] nil,
   [1 0] nil,       [1 1] nil,        [1 2] [1 :south], [1 3] nil,        [1 4] nil,
   [2 0] nil,       [2 1] [1 :east],  [2 2] [2 nil],    [2 3] [1 :west],  [2 4] nil,
   [3 0] nil,       [3 1] nil,        [3 2] [1 :north], [3 3] nil,        [3 4] nil,
   [4 0] nil,       [4 1] nil,        [4 2] nil,        [4 3] nil,        [4 4] nil})

(def sample-diffusion-basic-strings
  (list ". . . . . "
        ". . . . . "
        "          "
        ". . 1 . . "
        ". . S . . "
        "          "
        ". 1 2 1 . "
        ". E . W . "
        "          "
        ". . 1 . . "
        ". . N . . "
        "          "
        ". . . . . "
        ". . . . . "
        "          "))

(deftest test-convert-diffusion-row-to-strings-simple
  "Tests that conversion works as we'd expect"
  (binding [defines/*game-info* sample-game-info-basic
            defines/*game-state* sample-game-state-basic]
    (are [result row] (= result (convert-diffusion-row-to-strings sample-diffusion-basic row))
      (take 3 sample-diffusion-basic-strings) 0
      (take 3 (drop 3 sample-diffusion-basic-strings)) 1
      (take 3 (drop 6 sample-diffusion-basic-strings)) 2
      (take 3 (drop 9 sample-diffusion-basic-strings)) 3
      (take 3 (drop 12 sample-diffusion-basic-strings)) 4)))

(deftest test-convert-diffusion-to-strings-simple
  "Test that conversion works as we'd expect"
  (binding [defines/*game-info* sample-game-info-basic
            defines/*game-state* sample-game-state-basic]
    (is sample-diffusion-basic-strings (convert-diffusion-to-strings sample-diffusion-basic))))

(deftest test-diffuse-across-map-simple
  "Test that diffusion works as we'd expect"
  (binding [defines/*game-info* sample-game-info-basic
            defines/*game-state* sample-game-state-basic]
    (is (= sample-diffusion-basic-strings (convert-diffusion-to-strings
                                            (diffuse-across-map sample-game-food-basic  ; The only square to fill from (center)
                                                                #{}                     ; No special squares to avoid
                                                                2))))))                 ; The value the special square gets

;;;;;;;;;;;;;;;;;;;;;
;; Game with water ;;
;;;;;;;;;;;;;;;;;;;;;

(def sample-game-info-water {:rows 10
                              :cols 10})
(def sample-game-state-water {:water #{[2 6] [2 7] [2 8] [3 6] [4 8] [5 7] [5 8]}}) ; Note: [row column]!

(def sample-game-food-water (list [3 7] [7 6]))                                     ; Note: [row column]!

; The below is functionally identical to if the nil valued keys didn't exist (for our purposes)
(def sample-diffusion-water
  ; NOT FILLED IN YET, WRONG!
 {[0 0] nil, [0 1] nil, [0 2] nil, [0 3] nil, [0 4] nil, [0 5] nil, [0 6] nil, [0 7] nil, [0 8] nil, [0 9] nil,
 [1 0] nil, [1 1] nil, [1 2] nil, [1 3] nil, [1 4] nil, [1 5] nil, [1 6] nil, [1 7] nil, [1 8] nil, [1 9] nil,
 [2 0] nil, [2 1] nil, [2 2] nil, [2 3] nil, [2 4] nil, [2 5] nil, [2 6] nil, [2 7] nil, [2 8] nil, [2 9] nil,
 [3 0] nil, [3 1] nil, [3 2] nil, [3 3] nil, [3 4] nil, [3 5] nil, [3 6] nil, [3 7] nil, [3 8] nil, [3 9] nil,
 [4 0] nil, [4 1] nil, [4 2] nil, [4 3] nil, [4 4] nil, [4 5] nil, [4 6] nil, [4 7] nil, [4 8] nil, [4 9] nil,
 [5 0] nil, [5 1] nil, [5 2] nil, [5 3] nil, [5 4] nil, [5 5] nil, [5 6] nil, [5 7] nil, [5 8] nil, [5 9] nil,
 [6 0] nil, [6 1] nil, [6 2] nil, [6 3] nil, [6 4] nil, [6 5] nil, [6 6] nil, [6 7] nil, [6 8] nil, [6 9] nil,
 [7 0] nil, [7 1] nil, [7 2] nil, [7 3] nil, [7 4] nil, [7 5] nil, [7 6] nil, [7 7] nil, [7 8] nil, [7 9] nil,
 [8 0] nil, [8 1] nil, [8 2] nil, [8 3] nil, [8 4] nil, [8 5] nil, [8 6] nil, [8 7] nil, [8 8] nil, [8 9] nil,
 [9 0] nil, [9 1] nil, [9 2] nil, [9 3] nil, [9 4] nil, [9 5] nil, [9 6] nil, [9 7] nil, [9 8] nil, [9 9] nil})

(def sample-diffusion-basic-water
  (list ". . . . . . 1 . . . "
        ". . . . . . N . . . "
        "                    "
        ". . . . . . . . . . "
        ". . . . . . . . . . "
        "                    "
        ". . . . . . X X X 1 "
        ". . . . . . . . . S "
        "                    "
        "1 . . . . . X 4 3 2 "
        "W . . . . . . . W W "
        "                    "
        ". . . . . 1 2 3 X N "
        ". . . . . E E N . . "
        "                    "
        ". . . . . 1 2 X X . "
        ". . . . . . S . . . "
        "                    "
        ". . . . 1 2 3 2 1 . "
        ". . . . . . S . . . "
        "                    "
        ". . . 1 2 3 4 3 2 1 "
        ". . . E E E . W W W "
        "                    "
        ". . . . 1 2 3 2 1 . "
        ". . . . . . N . . . "
        "                    "
        ". . . . . 1 2 1 . . "
        ". . . . . . N . . . "
        "                    "))