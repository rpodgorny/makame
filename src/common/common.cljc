(ns common.common)

(defn unfinish-task [x]
  (dissoc x :finished_at :verified_at))

(defn start-task [x t]
  (-> x
      unfinish-task
      (assoc :current_start_at t)
      (update :started_at #(if % % t))))

(defn pause-task [x t]
  (-> x
      ;;(update :duration #(+ % (- t (:started_at x t))))
      (assoc :duration (+ (:duration x 0) (- t (:current_start_at x t))))
      (dissoc :current_start_at)))

(defn finish-task [x t]
  (-> x
      (pause-task t)
      (assoc :finished_at t)))

(defn verify-task [x t]
  (-> x
      (assoc :verified_at t)))
