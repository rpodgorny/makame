(ns frontend.subs
  (:require [frontend.utils :as u]
            [re-frame.core :as rf]))

(rf/reg-sub :app-db (fn [db] db))

(rf/reg-sub :t #(:t %))

(rf/reg-sub :countdown #(:countdown %))

(rf/reg-sub :users #(:users %))

;(rf/reg-sub :user-load (fn [db [_ user-id]] (-> db :user-load (get user-id))))
(rf/reg-sub :user-load (fn [db [_ user-id]] (get-in db [:user-load user-id])))

;;(rf/reg-sub :user-load-finished (fn [db [_ user-id]] (-> db :user-load-finished (get user-id))))
(rf/reg-sub :user-load-finished (fn [db [_ user-id]] (get-in db [:user-load-finished user-id])))

(rf/reg-sub :current-user #(:current-user %))

(rf/reg-sub :tasks #(:tasks %))

(rf/reg-sub :page #(:page %))

(rf/reg-sub :page-props #(:page-props %))

(rf/reg-sub :nshow #(:nshow %))

(rf/reg-sub :forms #(:form %))

(rf/reg-sub :form (fn [db [_ task-id k]] (get-in db [:form task-id k])))

(rf/reg-sub :form-datetime (fn [db [_ task-id k]] (get-in db [:form task-id :raw k])))

(rf/reg-sub :form-interval (fn [db [_ task-id k]] (get-in db [:form task-id :raw k])))

(rf/reg-sub :formxx (fn [db [_ task-id]] (get-in db [:form task-id])))

(rf/reg-sub :form2 (fn [db [_ k]] (-> db :form2 k)))

(rf/reg-sub :form2xx #(:form2 %))

(rf/reg-sub :modal #(:modal %))

(rf/reg-sub :modal-props #(:modal-props %))

(rf/reg-sub
 :task-time-str
 (fn [db [_ task]]
   (str (u/format-seconds-fancy (u/calc-so-far task @(rf/subscribe [:t])))
        " ("
        (u/format-seconds-fancy (:estimate task))
        ")")))

(rf/reg-sub
 :form-is-error
 (fn [db [_ task-id]]
   (let [form (get-in  db [:form task-id])]
     (or (empty? (:worker form))
         (empty? (:text form))
         ;;(not (u/parse-seconds-fancy (:estimate form)))
         (not (:estimate form))
         (not (:deadline form))))))

(rf/reg-sub
 :form2-is-error
 (fn [db]
   (let [form (:form2 db)]
     (or (empty? (:worker form))
         (empty? (:text form))
         ;;(not (u/parse-seconds-fancy (:estimate form)))
         (empty? (:estimate form))
         (empty? (:deadline form))))))

(rf/reg-sub
 :working
 #(u/extract-working @(rf/subscribe [:users]) @(rf/subscribe [:tasks])))
