(ns ants-ai.test.diffusion
  (:use [ants-ai.diffusion])
  (:use [clojure.test]))

(deftest test-seed-processing
  "Tests that seed-processing works as we'd expect"
  (are [result keys value] (= result (seed-processing keys value))
    (list) nil nil
    (list [:a [nil nil]]) [:a] nil
    (list [:a [1 nil]] [:b [1 nil]] [:c [1 nil]]) [:a :b :c] 1))
