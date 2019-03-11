(ns frontend.routes
  (:require [secretary.core :as secretary]
            [re-frame.core :as rf]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

(secretary/set-config! :prefix "#")

(secretary/defroute future "/future" []
  (rf/dispatch [:set-current-user nil])
  (rf/dispatch [:users-get])
  (rf/dispatch [:tasks-get])
  (rf/dispatch [:go-to-page :future nil]))

(secretary/defroute past "/past" []
  (rf/dispatch [:set-current-user nil])
  (rf/dispatch [:users-get])
  (rf/dispatch [:tasks-get])
  (rf/dispatch [:go-to-page :past nil]))

(secretary/defroute dashboard "/dashboard" []
  (rf/dispatch [:set-current-user nil])
  (rf/dispatch [:users-get])
  (rf/dispatch [:tasks-get])
  (rf/dispatch [:go-to-page :dashboard nil]))

(secretary/defroute user "/user/:id" [id]
  (rf/dispatch [:set-current-user id])
  (rf/dispatch [:form-change nil :worker id])
  (rf/dispatch [:users-get])
  (rf/dispatch [:tasks-get])
  (rf/dispatch [:nshow-reset])
  (rf/dispatch [:set-countdown 10])
  (rf/dispatch [:go-to-page :user {:user-id id}]))

(secretary/defroute tasks "/tasks" []
  (rf/dispatch [:set-current-user nil])
  (rf/dispatch [:users-get])
  (rf/dispatch [:tasks-get])
  (rf/dispatch [:go-to-page :tasks nil]))

(secretary/defroute tag "/tag/:tag" [tag]
  (rf/dispatch [:users-get])
  (rf/dispatch [:tasks-get])
  (rf/dispatch [:go-to-page :tag {:tag tag}]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      #(secretary/dispatch! (.-token %)))
    (.setEnabled true)))

(defn initial-dispatch []
  (secretary/dispatch! (.-location.href js/window)))
