(ns frontend.utils
  (:require [cljs-time.core :as t]
            [cljs-time.format]
            [cljs-time.coerce]
            [clojure.string :as str]))

(defn datetime->str [dt]
  (when dt
    (cljs-time.format/unparse (cljs-time.format/formatter "yyyy-MM-dd") dt)))

(defn t->datetime [t]
  (when t
    (cljs-time.coerce/to-local-date-time (cljs-time.coerce/from-long (* t 1000)))))

(defn get-current-t []
  (js/Math.round (/ (.getTime (js/Date.)) 1000)))

;;(defn parse-date [x]
;;  (cljs-time.format/parse x))

(defn parse-date-t [x]
  (when x
    (t/in-seconds (t/interval (t/epoch) (cljs-time.format/parse x)))))

(defn format-seconds [x]
  (when x
    (let [s (mod x 60)
          m (Math/floor (mod (/ x 60) 60))
          h (Math/floor (/ x 3600))]
      (str h ":" m ":" s))))

(defn format-seconds-fancy [x]
  (when x
    (let [m (Math/floor (mod (/ x 60) 60))
          h (Math/floor (/ x 3600))
          h-str (cond (= h 0) ""
                      true (str h "h"))
          m-str (cond (and (not (empty? h-str)) (= m 0)) ""
                      (and (not (empty? h-str)) (< m 10)) (str "0" m "m")
                      true (str m "m"))]
      (str h-str m-str))))

(defn parse-seconds-fancy [x]
  (let [[_ _ h _ m] (re-find #"((\d+)h)*\ *((\d+)m)*" x)]
    (when (or h m)
      (+ (* (int h) 3600) (* (int m) 60)))))

(comment
  (parse-seconds-fancy "1m"))

(defn calc-so-far [task t]
  (when task
    (+ (:duration task 0) (if (:started_at task) (- t (:started_at task)) 0))))

(defn form->task [form]
  (-> form
      (update :mentor #(if (empty? %) nil %))
      (dissoc :raw)))

(defn form->task-OLD [form]
  (-> form
      (update :mentor #(if (empty? %) nil %))
      (update :duration parse-seconds-fancy)
      (update :estimate parse-seconds-fancy)
      (update :tags (fn [s] (->> (str/split s #" ")
                                 (filter #(not (empty? %)))
                                 set
                                 (into []))))))

(defn task->form [task]
  (-> task
      (update :mentor #(if (empty? %) "" %))
      (assoc-in [:raw :duration] (format-seconds-fancy (:duration task)))
      (assoc-in [:raw :estimate] (format-seconds-fancy (:estimate task)))
      (assoc-in [:raw :finished_at] (datetime->str (t->datetime (:finished_at task))))
      (assoc-in [:raw :deadline] (datetime->str (t->datetime (:deadline task))))))
      ;;(update :tags (fn [tags] (str/join " " tags)))))

(defn find-task-by-user-id [user-id tasks]
  (as-> tasks x
        (filter :current_start_at x)
        (group-by :worker x)
        (get-in x [user-id 0])))

(defn extract-working [users tasks]
  (for [user users
        :let [task (find-task-by-user-id (:id user) tasks)]]
    {:user user
     :task task}))

(defn progress-to-class [x]
  (cond (> x 0.8) "is-danger"
        (> x 0.7) "is-warning"
        true "is-success"))

;;(def twitter-tag-re #"/\B#\w*[a-zA-Z]+\w*/")
(def twitter-tag-re #"#\w*")
;;(def twitter-tag-re #"(?<=^|(?<=[^a-zA-Z0-9-_\\.]))@([A-Za-z]+[A-Za-z0-9_]+)")

(defn word-to-tag-link [x route-fn]
  (if (re-find twitter-tag-re x)
    [:a {:href (route-fn {:tag "UNFINISHED"})}]))

(defn text-to-tag-links-xx [x]
  (re-find twitter-tag-re x))

(defn text-to-tag-links [x]
  (interpose " "
    (for [word (str/split x #" ")]
      (if (str/starts-with? word "#")
        [:a {:href "FUCK"} word]
        word))))

(comment
  (text-to-tag-links "ahoj #tag34! sdf")
  (apply vector (cons :div ["ahoj" " " [:a {:href "fff"} "link"] " " "vole"])))
