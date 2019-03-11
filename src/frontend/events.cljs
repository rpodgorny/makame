(ns frontend.events
  (:require [frontend.utils :as u]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]))

(defn xhrio-json [x]
  (assoc x :format (ajax.core/json-request-format)
           :response-format (ajax.core/json-response-format {:keywords? true})))

(rf/reg-event-db :initialize (fn [db] {:page :dashboard
                                       :countdown 10
                                       :nshow 5}))

(rf/reg-event-db :new-data (fn [db [_ x]] (assoc db :data x)))

(rf/reg-event-db :set-current-user (fn [db [_ x]] (assoc db :current-user x)))

(rf/reg-event-db :set-countdown (fn [db [_ x]] (assoc db :countdown x)))

(rf/reg-event-db :go-to-page (fn [db [_ name props]] (assoc db :page name :page-props props)))

(rf/reg-event-db :set-modal (fn [db [_ x props]] (assoc db :modal x :modal-props props)))

(rf/reg-event-db :form-change (fn [db [_ task-id k v]] (assoc-in db [:form task-id k] v)))

(rf/reg-event-db :form-change-datetime
                 (fn [db [_ task-id k v]]
                   (-> db
                       (assoc-in [:form task-id :raw k] v)
                       (assoc-in [:form task-id k] (u/parse-date-t v)))))

(rf/reg-event-db :form-change-interval
                 (fn [db [_ task-id k v]]
                   (-> db
                       (assoc-in [:form task-id :raw k] v)
                       (assoc-in [:form task-id k] (u/parse-seconds-fancy v)))))

(rf/reg-event-db :form-reset (fn [db [_ task-id]] (assoc-in db [:form task-id] nil)))

(rf/reg-event-fx
 :form-save
 (fn [cofx [_ x]]
   {:http-xhrio (if (:_id x)
                  (xhrio-json
                   {:method :post
                    :uri (str "http://127.0.0.1:8888/tasks/" (:_id x))
                    :params x
                    :on-success [:tasks-get nil]
                    :on-failure [:tasks-get nil]})
                  (xhrio-json
                   {:method :post
                    :uri "http://127.0.0.1:8888/tasks"
                    :params x
                    :on-success [:tasks-get nil]
                    :on-failure [:tasks-get nil]}))
    :dispatch [:form-reset (:_id x)]}))

(rf/reg-event-db :form2-change (fn [db [_ k v]] (assoc-in db [:form2 k] v)))

(rf/reg-event-db :form2-reset (fn [db _] (dissoc db :form2)))

(rf/reg-event-fx
 :quick-task
 (fn [cofx [_ user-id]]
   {:http-xhrio (xhrio-json
                  {:method :get
                   :uri (str "http://127.0.0.1:8888/users/" user-id "/quick_task")
                   :on-success [:tasks-get nil]
                   :on-failure [:tasks-get nil]})}))


(rf/reg-event-db :nshow-inc (fn [db] (update db :nshow #(+ 5 %))))

(rf/reg-event-db :nshow-reset (fn [db] (assoc db :nshow 5)))

(rf/reg-event-db :tasks-get-success (fn [db [_ x]] (assoc db :tasks x)))

(rf/reg-event-db :no-op (fn [db] db))

(rf/reg-event-fx
 :tasks-get
 (fn [cofx _]
    {:db (dissoc (:db cofx) :tasks)
     :http-xhrio (if-let [current-user @(rf/subscribe [:current-user])]
                   (xhrio-json {:method :get
                                :uri (str "http://127.0.0.1:8888/users/" current-user "/tasks")
                                :on-success [:tasks-get-success]
                                :on-failure [:no-op]})
                   (xhrio-json {:method :get
                                :uri (str "http://127.0.0.1:8888/tasks")
                                :on-success [:tasks-get-success]
                                :on-failure [:no-op]}))}))

(rf/reg-event-db
 :users-get-success
 (fn [db [_ x]]
   (doseq [user x]
     (rf/dispatch [:user-get-load (:id user)])
     (rf/dispatch [:user-get-load-finished (:id user)]))
   (assoc db :users x)))

(rf/reg-event-fx
 :users-get
 (fn [cofx _]
    {:db (dissoc (:db cofx) :users)
     :http-xhrio (xhrio-json
                  {:method :get
                   :uri (str "http://127.0.0.1:8888/users")
                   :on-success [:users-get-success]
                   :on-failure [:no-op]})}))

(rf/reg-event-db :user-get-load-success (fn [db [_ user-id x]] (assoc-in db [:user-load user-id] x)))

(rf/reg-event-fx
 :user-get-load
 (fn [cofx [_ id]]
   {:db (assoc-in (:db cofx) [:user-load id] nil)
    :http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/users/" id "/load")
                  :on-success [:user-get-load-success id]
                  :on-failure [:no-op]})}))

(rf/reg-event-db :user-get-load-finished-success (fn [db [_ user-id x]] (assoc-in db [:user-load-finished user-id] x)))

(rf/reg-event-fx
 :user-get-load-finished
 (fn [cofx [_ id]]
   {:db (assoc-in (:db cofx) [:user-load id] nil)
    :http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/users/" id "/load_finished")
                  :on-success [:user-get-load-finished-success id]
                  :on-failure [:no-op]})}))

(rf/reg-event-fx
 :task-start
 (fn [cofx [_ id]]
   {:http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/tasks/" id "/start")
                  :on-success [:tasks-get nil]
                  :on-failure [:tasks-get nil]})}))

(rf/reg-event-fx
 :task-pause
 (fn [cofx [_ id]]
   {:http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/tasks/" id "/pause")
                  :on-success [:tasks-get nil]
                  :on-failure [:tasks-get nil]})}))

(rf/reg-event-fx
 :task-finish
 (fn [cofx [_ id]]
   {:http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/tasks/" id "/finish")
                  :on-success [:tasks-get nil]
                  :on-failure [:tasks-get nil]})}))

(rf/reg-event-fx
 :task-unfinish
 (fn [cofx [_ id]]
   {:http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/tasks/" id "/unfinish")
                  :on-success [:tasks-get nil]
                  :on-failure [:tasks-get nil]})}))

(rf/reg-event-fx
 :task-verify
 (fn [cofx [_ id]]
   {:http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/tasks/" id "/verify")
                  :on-success [:tasks-get nil]
                  :on-failure [:tasks-get nil]})}))

(rf/reg-event-fx
 :task-edit-orig
 (fn [cofx [_ id]]
   {:http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/tasks/" id)
                  :on-success [:fill-form2-with-task]
                  :on-failure [:no-op]})}))

(rf/reg-event-db :fill-form2-with-task (fn [db [_ x]] (assoc db :form2 (u/task->form x))))

(rf/reg-event-fx
 :task-edit
 (fn [cofx [_ id]]
   {:http-xhrio (xhrio-json
                 {:method :get
                  :uri (str "http://127.0.0.1:8888/tasks/" id)
                  :on-success [:fill-form-with-task id]
                  :on-failure [:no-op]})}))

(rf/reg-event-db :fill-form-with-task (fn [db [_ task-id x]] (assoc-in db [:form task-id] (u/task->form x))))

(rf/reg-event-fx
 :task-delete
 (fn [cofx [_ id]]
   {:http-xhrio (xhrio-json
                 {:method :delete
                  :uri (str "http://127.0.0.1:8888/tasks/" id)
                  :on-success [:tasks-get]
                  :on-failure [:tasks-get]})}))

(rf/reg-event-db
 :timer
 (fn [db [_ e]]
   (js/setTimeout #(rf/dispatch [:timer]) 1000)
   (assoc db :t (u/get-current-t))))

(rf/reg-event-db
 :countdown
 (fn [db [_ e]]
   (js/setTimeout #(rf/dispatch [:countdown]) 1000)
   (if-not (:countdown db)
     db
     (if (> (:countdown db) 0)
       (update db :countdown #(when % (dec %)))
       (do
         ;;(rf/dispatch [:go-to-page :dashboard nil])
         ;;(secretary.core/dispatch! "/future")
         (set! (.-href js/window.location) "/#/dashboard")
         (dissoc db :countdown))))))
