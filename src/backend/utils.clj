(ns backend.utils
  (:require [clj-time.core :as t]
            [clj-time.format]))

(defn get-current-t []
  (int (/ (System/currentTimeMillis) 1000)))

(defn parse-date-t [x]
  (when x
    (t/in-seconds (t/interval (t/epoch) (clj-time.format/parse x)))))

(comment
  (parse-date-t "2019-01-01"))

(defn calc-busy-time-one [t till task]
  (let [deadline-t (parse-date-t (:deadline task))]
    (* (:estimate task)
       (if (< deadline-t till)
         1
         (/ (- till t) (- deadline-t t))))))

(defn calc-busy-time [t till tasks]
  (reduce + (map #(calc-busy-time-one t till %) tasks)))

(comment
  (let [t (parse-date-t "2019-01-01")
        till (+ t (* 1 24 3600))
        tasks [{:estimate (* 8 24 3600)
                :deadline "2019-01-20"}
               {:estimate (* 24 3600)
                :deadline "2019-01-20"}]]
    (calc-busy-time t till tasks)))

(defn user-calc-load [user tasks t till]
  (let [busy-time (calc-busy-time t till tasks)
        time-to-till (- till t)
        perf (:perf user 1)
        avail-time (* time-to-till perf)]
    {:total time-to-till
     :busy busy-time
     :avail avail-time
     :free (- avail-time busy-time)
     :load (/ busy-time avail-time)}))

(defn calc-busy-time-finished-one [t since task]
  (let [finished-t (:finished_at task)
        start-t (- finished-t (:duration task))]
    ;; (if (< finished-t since)
    ;;   0
    ;;   (* (:duration task)
    ;;      (if (> start-t since)
    ;;        1
    ;;        (/ (- finished-t since) (- finished-t start-t))))
    (* (:duration task)
       (cond (> start-t since) 1
             (< finished-t since) 0
             true  (/ (- finished-t since) (- finished-t start-t))))))

(defn calc-busy-time-finished [t since tasks]
  (reduce + (map #(calc-busy-time-finished-one t since %) tasks)))

(comment
  (let [t (parse-date-t "2019-02-02")
        since (- t (* 2 24 3600))
        tasks [{:duration (* 8 3600)
                :finished_at (parse-date-t "2019-02-01")}
               {:duration (* 4 3600)
                :finished_at (parse-date-t "2019-02-01")}]]
    ;;(calc-busy-time-finished-one t since (first tasks))))
    (calc-busy-time-finished t since tasks)))

(defn user-calc-load-finished [user tasks t since]
  (let [busy-time (calc-busy-time-finished t since tasks)
        time-since-since (- t since)
        perf (:perf user 1)
        avail-time (* time-since-since perf)]
    {:total time-since-since
     :busy busy-time
     :avail avail-time
     :free (- avail-time busy-time)
     :load (/ busy-time avail-time)}))
