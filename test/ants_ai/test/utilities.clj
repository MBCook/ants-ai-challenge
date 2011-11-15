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

(deftest test-get-line-of-sight-block
  "Tests the line of sight function"
  (binding [defines/*game-info* {:rows 150 :cols 150}]
    (get-line-of-sight-block [114 148] [91 60] (fn [_] false))
    (is true)))

(deftest timing-get-line-of-sight-block
  "Tests the line of sight function"
  (binding [defines/*game-info* {:rows 150 :cols 150}]
    (let [water (set (for [x (range 500)] [(rand-int 150) (rand-int 150)]))
          pairs (set (for [x (range 10000)] [[(rand-int 150) (rand-int 150)] [(rand-int 150) (rand-int 150)]]))
          good-pairs (filter #(not= (first %) (second %)) pairs)]
      (time
        (loop [pairs-left good-pairs]
          (if (empty? pairs-left)
            nil
            (let [pair (first pairs-left)]
              (get-line-of-sight-block (first pair) (second pair) #(contains? water %))
              (recur (rest pairs-left)))))))
    (is true)))


