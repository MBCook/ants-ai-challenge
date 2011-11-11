(ns ants-ai.test.diffusion
  (:use [ants-ai.diffusion])
  (:use [clojure.test])
  (:require [ants-ai.defines :as defines]))

(def sample-game-info {:rows 5
                       :cols 5})

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

(deftest test-seed-processing
  "Tests that seed-processing works as we'd expect"
  (are [result keys value] (= result (seed-processing keys value))
    (list) nil nil
    (list [:a [nil nil]]) [:a] nil
    (list [:a [1 nil]] [:b [1 nil]] [:c [1 nil]]) [:a :b :c] 1))

(deftest test-convert-diffusion-row-to-strings-simple
  "Tests that conversion works as we'd expect"
  (binding [defines/*game-info* sample-game-info]
    (are [result row] (= result (convert-diffusion-row-to-strings sample-diffusion-basic row))
      (take 3 sample-diffusion-basic-strings) 0
      (take 3 (drop 3 sample-diffusion-basic-strings)) 1
      (take 3 (drop 6 sample-diffusion-basic-strings)) 2
      (take 3 (drop 9 sample-diffusion-basic-strings)) 3
      (take 3 (drop 12 sample-diffusion-basic-strings)) 4)))

(deftest test-convert-diffusion-to-strings-simple
  "Test that conversion works as we'd expect"
  (binding [defines/*game-info* sample-game-info]
    (is sample-diffusion-basic-strings (convert-diffusion-to-strings sample-diffusion-basic))))

(deftest test-diffuse-across-map-simple
  "Test that diffusion works as we'd expect"
  (binding [defines/*game-info* sample-game-info]
    (is (= sample-diffusion-basic-strings (convert-diffusion-to-strings
                                            (diffuse-across-map (list [2 2])    ; The only square to fill from (center)
                                                                #{}             ; No special squares to avoid
                                                                2))))))         ; The value the special square gets