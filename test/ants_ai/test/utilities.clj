(ns ants-ai.test.utilities
  (:use [ants-ai.utilities])
  (:use [clojure.test]))

(deftest test-coalesce
  "Tests that coalesce works as we'd expect"
  (are [result value default] (= result (coalesce value default))
    nil nil nil
    1 nil 1
    [:bob] nil [:bob]
    [:bob] [:bob] 13))
