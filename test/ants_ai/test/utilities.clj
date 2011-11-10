(ns ants-ai.test.utilities
  (:use [ants-ai.utilities])
  (:use [clojure.test]))

(deftest test-seed-map
  "Tests that seed-map works as we'd expect"
  (are [result keys value] (= result (persistent! (seed-map keys value)))
    {} nil nil
    {:a nil} [:a] nil
    {:a 1 :b 1 :c 1} [:a :b :c] 1
    {:a 1 :b 1 :c 1} #{:a :b :c} 1))
