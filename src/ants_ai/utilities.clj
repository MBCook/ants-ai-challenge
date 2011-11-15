(ns ants-ai.utilities
    "Useful functions to help us deal with the world"
    (:require [clojure.string :as string]
              [ants-ai.defines :as defines]
              [ants-ai.gamestate :as gamestate]
              [ants-ai.gameinfo :as gameinfo]))

(defn safe-abs
  "Correctly handles calling abs on BigInts by coercing back to an int"
  [val]
  (Math/abs (int val)))

(defn debug-log
  "Log something to the console for us to go through later"
  [& message]
  (when defines/logging-enabled
    (binding [*out* (if (nil? defines/*log-file*)
                        *err*
                        defines/*log-file*)]
      (apply println message)
      (flush))))

(defn unit-distance
  "Get the vector distance between two points on a torus. Negative deltas are preserved."
  [location-one location-two]
  (let [[dx dy] (map - location-two location-one)
        [adx ady] (map #(safe-abs %) [dx dy])
        [adx2 ady2] (map #(- (gamestate/game-info %) %2) [:rows :cols] [adx ady])
        fx (if (<= adx adx2)
             dx
             (* adx2 (/ (- dx) adx)))
        fy (if (<= ady ady2)
             dy
             (* ady2 (/ (- dy) ady)))]
    [fx fy]))

(defn distance-no-sqrt
  "Get the euclidean distance between two locations on a torus, squared"
  [location location-two]
  (let [[dx dy] (unit-distance location location-two)]
    (+ (Math/pow dx 2) (Math/pow dy 2))))

(defn distance
  "Get the euclidean distance between two locations on a torus"
  [location location-two]
  (Math/sqrt (distance-no-sqrt location location-two)))

(defn contains-ant?
  "See if the given location is in the given ants array"
  [ants cur]
  (some #(let [[r c p] %]
            (= [r c] cur))
        ants))

(defn passable?
  "Deteremine if the given location can be moved to. If so, loc is returned."
  [loc]
  (when (and (not (contains? (gamestate/water) loc))
             (not (contains? (gamestate/food) loc))                   ; Can't walk onto newly spawned food
             (not (contains? (gamestate/my-hills) loc)))              ; We shouldn't move onto our own hills
    loc))

(defn clamp
  "Lock a value in a range"
  [val lim]
  (cond
    (neg? val)
      (+ val lim)
    (>= val lim)
      (- val lim)
    :else
      val))

(defn move-ant
  "Move the ant in the given direction and return the new location"
  [ant dir]
  (let [dir-vector (defines/direction-offsets dir)
        rows (gameinfo/map-rows)
        cols (gameinfo/map-columns)
        [r c] (map + ant dir-vector)]
    [(clamp r rows) (clamp c cols)]))

(defn valid-move?
  "Check if moving an ant in the given direction is passable. If so,
  return the location that the ant would then be in."
  [ant dir occupied-locations]
  (let [the-loc (move-ant ant dir)]
    (when (and (passable? the-loc)
              (not (contains? occupied-locations the-loc)))
      the-loc)))

(defn direction
  "Determine the directions needed to move to reach a specific location.
  This does not attempt to avoid water. The result will be a collection
  containing up to two directions."
  [location location-two]
  (let [[dr dc] (unit-distance location location-two)
        row (if-not (zero? dr)
                    (/ dr (safe-abs dr))
                    dr)
        col (if-not (zero? dc)
                    (/ dc (safe-abs dc))
                    dc)]
    (set (filter #(not (nil? %))
            [(defines/offset-directions [row 0])
             (defines/offset-directions [0 col])]))))

(defn coalesce
  "Replace nil values with the default."
  [value default]
  (if (nil? value)
    default
    value))

(defn get-line-of-sight-block
  "Runs through a line of sight, returning the first blocked square"
  ([location location-two test-fn]
    (let [[r c] location
          [delt-r delt-c] (unit-distance location location-two) ; Figure out the direction to move in (+/- for row/col)
          manhattan-distance (+ (safe-abs delt-r) (safe-abs delt-c))
          r-step (/ delt-r manhattan-distance)
          c-step (/ delt-c manhattan-distance)]
      (get-line-of-sight-block r c location-two test-fn r-step c-step)))
  ([row col location-two test-fn r-step c-step]
    (let [r (clamp row (gameinfo/map-rows))                   ; Fix the co-ords
          c (clamp col (gameinfo/map-columns))]
      (cond
        (= [r c] location-two)
          nil                                                 ; We hit the end condition
        (apply test-fn [[(int r) (int c)]])
          [r c]                                               ; We found a block, return the location
        :else
          (recur (+ r-step r) (+ c-step c)                    ; Move on to the next square
                 location-two test-fn
                 r-step c-step)))))

(defn is-line-of-site-clear?
  "Checks if a line of site is clear"
  [location location-two test-fn]
  (not (get-line-of-sight-block location location-two test-fn)))