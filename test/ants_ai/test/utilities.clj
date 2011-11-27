(ns ants-ai.test.utilities
  (:use [ants-ai.utilities])
  (:use [clojure.test])
  (:require [ants-ai.defines :as defines]))

(deftest test-coalesce
  "Tests that coalesce works as we'd expect"
  (are [result value default] (= result (coalesce value default))
    nil nil nil
    1 nil 1
    [:bob] nil [:bob]
    [:bob] [:bob] 13))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Line of sight tests ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-get-line-of-sight-block
  "Tests the line of sight function"
  (binding [defines/*game-info* {:rows 150 :cols 150}]
    (get-line-of-sight-block [114 148] [91 60] (fn [_] false))
    (is true)))

(deftest test-get-line-of-sight-block-2
  "Tests the line of sight function"
  (binding [defines/*game-info* {:rows 150 :cols 150}]
    (get-line-of-sight-block [114 148 1] [91 60] (fn [_] false))
    (is true)))

(deftest test-get-line-of-sight-block-3
  "Tests the line of sight function"
  (binding [defines/*game-info* {:rows 150 :cols 150}]
    (get-line-of-sight-block [114 148] [91 60 1] (fn [_] false))
    (is true)))

;(deftest timing-get-line-of-sight-block
;  "Tests the line of sight function"
;  (binding [defines/*game-info* {:rows 150 :cols 150}]
;    (let [water (set (for [x (range 500)] [(rand-int 150) (rand-int 150)]))
;          pairs (set (for [x (range 10000)] [[(rand-int 150) (rand-int 150)] [(rand-int 150) (rand-int 150)]]))
;          good-pairs (filter #(not= (first %) (second %)) pairs)]
;      (time
;        (loop [pairs-left good-pairs]
;          (if (empty? pairs-left)
;            nil
;            (let [pair (first pairs-left)]
;              (get-line-of-sight-block (first pair) (second pair) #(contains? water %))
;              (recur (rest pairs-left)))))))
;    (is true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Defense positions tests ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def rows 100)
(def cols 100)

(def sample-game-info {:rows rows
                        :cols cols})

(def sample-game-state {:water (loop [water-spots #{}   ; Water covers the 50th row
                                      index 0]
                                  (if (= 100 index)
                                    water-spots
                                    (recur (conj water-spots [50 index]) (inc index))))
                        :hills #{[49 20] [52 50] [75 75]}})

(deftest test-defense-positions
  "See that the defense positions we're given make sense based on what we've given"
  (binding [defines/*game-info* sample-game-info
            defines/*game-state* sample-game-state]
    (let [everything (calculate-defense-strategies)]
      (is {[52 50] {:sixteen-point-defense (list [51 49] [51 48] [51 51] [51 52] [53 51] [54 51] [53 52] [54 52] [53 49] [54 49] [53 48] [54 48]),
                    :twelve-point-defense (list [51 49] [51 48] [51 51] [51 52] [53 51] [54 51] [53 52] [53 49] [54 49] [53 48]),
                    :four-point-defense (list [51 49] [51 51] [53 51] [53 49])},
           [49 20] {:sixteen-point-defense (list [48 19] [47 19] [48 18] [47 18] [48 21] [47 21] [48 22] [47 22]),
                    :twelve-point-defense (list [48 19] [47 19] [48 18] [48 21] [47 21] [48 22]),
                    :four-point-defense (list [48 19] [48 21])},
           [75 75] {:sixteen-point-defense (list [74 74] [73 74] [74 73] [73 73] [74 76] [73 76] [74 77] [73 77] [76 76] [77 76] [76 77] [77 77] [76 74] [77 74] [76 73] [77 73]),
                    :twelve-point-defense (list [74 74] [73 74] [74 73] [74 76] [73 76] [74 77] [76 76] [77 76] [76 77] [76 74] [77 74] [76 73]),
                    :four-point-defense (list [74 74] [74 76] [76 76] [76 74])}}))))