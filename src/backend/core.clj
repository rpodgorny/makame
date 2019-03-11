(ns backend.core
  (:require [common.common :as c]
            [backend.utils :as u]
            [compojure.route :as cr]
            [compojure.core :as cc]
            [org.httpkit.server :as hks]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response]]
            [monger.core :as mg]
            [monger.collection :as mc]
            ;;[monger.result :as mr]
            [monger.json]
            [cheshire.core :as json])
  (:import org.bson.types.ObjectId))

;; fucking ugly
(def conn (mg/connect))
(def db (mg/get-db conn "makame"))

(defn stream [req]
  (hks/with-channel req ch
    (prn "jsem in")
    (hks/on-close ch (fn [status] (prn "closed" status)))
    (hks/send! ch
               (-> (ring.middleware.cors/handle-cors
                     identity
                     req
                     (ring.middleware.cors/normalize-config [:access-control-allow-origin #".*" :access-control-allow-methods [:get :put :post :delete]])
                     ring.middleware.cors/add-access-control)
                   (ring.util.response/header "Content-Type" "text/event-stream; charset=utf-8"))
               false)
      ;;(wrap-cors :access-control-allow-origin [#".*"]
      ;;           :access-control-allow-methods [:get :put :post :delete])
    (loop []
      (hks/send! ch
                 (str "data: " (json/encode {:ahoj "vole" :t (u/get-current-t)}) "\n\n")
                 false)
      (Thread/sleep 1000)
      (if (hks/open? ch)
        (recur)))
    (prn "jsem out")
    (hks/close ch)))

(defn user-calc-load [user-id t till]
  (let [user (mc/find-one-as-map db "users" {:id user-id})
        tasks (mc/find-maps db "tasks" {:worker user-id})]
    (u/user-calc-load user tasks t till)))

(defn user-calc-load-finished [user-id t since]
  (let [user (mc/find-one-as-map db "users" {:id user-id})
        tasks (mc/find-maps db "tasks" {:worker user-id :finished_at {"$exists" true}})]
    (u/user-calc-load-finished user tasks t since)))

(defn pull-update-push [db coll id f]
  (let [x (mc/find-map-by-id db coll (ObjectId. id))
        xx (f x)]
    (mc/update-by-id db coll (ObjectId. id) xx)
    xx))

(defn pause-all-user-tasks [user-id t]
  (let [tasks (mc/find-maps db "tasks" {:worker user-id})]
    (doseq [task tasks]
      (mc/update-by-id db "tasks" (:_id task) (c/pause-task task t)))))

(defn users [query]
  (mc/find-maps db "users" query))

(defn user-find [id]
  (mc/find-one-as-map db "users" {:id id}))

(defn user-patch [id x]
  (mc/update db "users" {:id id} {"$set" x})
  (mc/find-one-as-map db "users" {:id id}))

(defn user-tasks [id]
  (mc/find-maps db "tasks" {"$or" [{:worker id}
                                   {"$and" [{:mentor id}
                                            {:finished_at {"$exists" true}}
                                            {:verified_at {"$exists" false}}]}]}))

(defn task-find [id]
  (mc/find-map-by-id db "tasks" (ObjectId. id)))

(defn task-insert [x]
  (mc/insert-and-return db "tasks" x))

(defn task-start [id]
  (let [task (task-find id)
        t (u/get-current-t)
        user-id (:worker task)
        _ (pause-all-user-tasks user-id t)
        updated-task (c/start-task task t)]
    (mc/update-by-id db "tasks" (ObjectId. id) updated-task)
    updated-task))

(defn user-quick-task [id]
  (let [t (u/get-current-t)
        _ (pause-all-user-tasks id t)
        task (task-insert {:worker id
                           :text (str "rychlej ukol z " t " #pruser")
                           :estimate 1
                           :deadline (+ t 1)})]
    (task-start (str (:_id task)))))

(defn user-load [id]
  (let [user (mc/find-one-as-map db "users" {:id id})
        t (u/get-current-t)]
    {:load-1d (user-calc-load id t (+ t (* 24 3600)))
     :load-7d (user-calc-load id t (+ t (* 7 24 3600)))
     :load-30d (user-calc-load id t (+ t (* 30 24 3600)))}))

(defn user-load-finished [id]
  (let [user (mc/find-one-as-map db "users" {:id id})
        t (u/get-current-t)]
    {:load-1d (user-calc-load-finished id t (- t (* 24 3600)))
     :load-7d (user-calc-load-finished id t (- t (* 7 24 3600)))
     :load-30d (user-calc-load-finished id t (- t (* 30 24 3600)))}))

(defn tasks [query]
  (mc/find-maps db "tasks" query))

(defn task-update [id x]
  (mc/update-by-id db "tasks" (ObjectId. id) (dissoc x :_id))
  nil)

(defn task-patch [id x]
  (mc/update-by-id db "tasks" (ObjectId. id) {"$set" (dissoc x :_id)})
  (mc/find-map-by-id db "tasks" (ObjectId. id)))

(defn task-remove [id]
  (mc/remove-by-id db "tasks" (ObjectId. id))
  nil)

(defn task-pause [id]
  (pull-update-push db "tasks" id #(c/pause-task % (u/get-current-t))))

(defn task-finish [id]
  (pull-update-push db "tasks" id #(c/finish-task % (u/get-current-t))))

(defn task-unfinish [id]
  (pull-update-push db "tasks" id #(c/unfinish-task %)))

(defn task-verify [id]
  (pull-update-push db "tasks" id #(c/verify-task % (u/get-current-t))))

(defn body-or-params [req]
  (if-let [body (-> req :body)]
    body
    (-> req :params)))

(cc/defroutes all-routes
  (cc/GET "/" [] (response ""))
  (cc/GET "/users" [] (fn [req] (response (users (-> req :body)))))
  (cc/GET "/users/:id" [id] (response (user-find id)))
  (cc/PATCH "/users/:id" [id] (fn [req] (response (user-patch id (-> req :body)))))
  (cc/GET "/users/:id/tasks" [id] (response (user-tasks id)))
  (cc/GET "/users/:id/quick_task" [id] (response (user-quick-task id)))
  (cc/GET "/users/:id/load" [id] (response (user-load id)))
  (cc/GET "/users/:id/load_finished" [id] (response (user-load-finished id)))
  (cc/GET "/tasks" [] (fn [req] (response (tasks (body-or-params req)))))
  (cc/POST "/tasks" [] (fn [req] (response (task-insert (-> req :body)))))
  (cc/GET "/tasks/:id" [id] (response (task-find id)))
  (cc/POST "/tasks/:id" [id] (fn [req] (response (task-update id (-> req :body)))))
  (cc/PATCH "/tasks/:id" [id] (fn [req] (response (task-patch id (-> req :body)))))
  (cc/DELETE "/tasks/:id" [id] (response (task-remove id)))
  (cc/GET "/tasks/:id/start" [id] (response (task-start id)))
  (cc/GET "/tasks/:id/pause" [id] (response (task-pause id)))
  (cc/GET "/tasks/:id/finish" [id] (response (task-finish id)))
  (cc/GET "/tasks/:id/unfinish" [id] (response (task-unfinish id)))
  (cc/GET "/tasks/:id/verify" [id] (response (task-verify id)))
  (cc/GET "/stream" [] stream)
  (cr/files "/static/" {:root "resources/public"})
  (cr/not-found "hovno (not-found)"))

(defn wrapxxx [handler]
  (-> handler
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      wrap-nested-params
      wrap-params
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(defn -main [& args]
  (println "ahoj")
  ;;(hks/run-server all-routes-cors {:port 8888})
  (hks/run-server (wrapxxx all-routes) {:port 8888})
  (println "vole"))

(comment
  (def conn (mg/connect))
  (def db (mg/get-db conn "makame"))
  (mc/insert db "tasks" {:pokus "Vole"})
  (mc/find-by-id db "tasks" (ObjectId. "5c5347d6c8694344598540f6"))
  (mc/count db "tasks"))
