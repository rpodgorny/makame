(ns frontend.core
  (:require [frontend.dbs :as dbs]
            [frontend.utils :as u]
            [frontend.subs :as fs]
            [frontend.events :as fe]
            [frontend.routes :as routes]
            [clojure.pprint]
            [reagent.core :as reagent]
            [re-frame.core :as rf]
            [cljs-time.core :as t]))

(enable-console-print!)

(defn debug-app-db [props]
  (let [db @(rf/subscribe [:app-db])]
    [:pre (clojure.pprint/write db :stream nil)]))

(defn debug-props [props]
  [:pre (clojure.pprint/write props :stream nil)])

(defn modal-modal []
  (if-let [m @(rf/subscribe [:modal])]
    [:div.modal.is-active
     [:div.modal-background]
     [:div.modal-content
      [:div.box.content
       [m @(rf/subscribe [:modal-props])]]]
     [:button.modal-close.is-large {:on-click #(rf/dispatch [:set-modal nil nil])}]]
    [:div]))

(defn countdown [props]
  [:div (str "countdown: " @(rf/subscribe [:countdown]))])

(defn user-select-options [users]
  (cons [:option {:key ""} ""]
        (for [user users]
          [:option {:key (:id user)} (:id user)])))

(defn task-form-horiz [props]
  (let [task-id (-> props :task :_id)
        is-simple (:is-simple props)]
    [:div.columns
     (when-not 0;;is-simple
       [:div.column.is-narrow
        [:div.field
         [:label.label "id"]
         @(rf/subscribe [:form task-id :_id])]])
     [:div.column.is-narrow
      [:div.field
       [:label.label "Makac"]
       [:div.control
        [:div.select
         [:select.input
          {:value @(rf/subscribe [:form task-id :worker])
           :class (when (empty? @(rf/subscribe [:form task-id :worker])) "is-danger")
           :on-change #(rf/dispatch [:form-change task-id :worker (.-target.value %)])}
          (user-select-options @(rf/subscribe [:users]))]]]]]
     [:div.column.is-narrow
      [:div.field
       [:label.label "Mentor"]
       [:div.control
        [:div.select
         [:select.input
          {:value @(rf/subscribe [:form task-id :mentor])
           :on-change #(rf/dispatch [:form-change task-id :mentor (.-target.value %)])}
          (user-select-options @(rf/subscribe [:users]))]]]]]
     [:div.column
      [:div.field
       [:label.label "Popis"]
       [:div.control
        [:input.input
         {:type "text"
          :value @(rf/subscribe [:form task-id :text])
          :class (when (empty? @(rf/subscribe [:form task-id :text])) "is-danger")
          :on-change #(rf/dispatch [:form-change task-id :text (.-target.value %)])}]]]]
     [:div.column.is-narrow
      [:div.field
       [:label.label "Odhad"]
       [:div.control
        [:input.input
         {:type "text"
          :style {:width "70px"}
          :placeholder "1h"
          :value @(rf/subscribe [:form-interval task-id :estimate])
          :class (when-not @(rf/subscribe [:form task-id :estimate]) "is-danger")
          :on-change #(rf/dispatch [:form-change-interval task-id :estimate (.-target.value %)])}]]]]
     [:div.column.is-narrow
      [:div.field
       [:label.label "Dedlajn"]
       [:div.control @(rf/subscribe [:form-datetime task-id :deadline])]
       [:div.control
        [:input.input
         {:type "date"
          :value @(rf/subscribe [:form-datetime task-id :deadline])
          :class (when-not @(rf/subscribe [:form task-id :deadline]) "is-danger")
          :on-change #(rf/dispatch [:form-change-datetime task-id :deadline (.-target.value %)])}]]]]
     (when-not is-simple
       [:div.column.is-narrow
        [:div.field
         [:label.label "Trvalo"]
         [:div.control
          [:input.input
           {:type "text"
            :style {:width "70px"}
            :placeholder "1h"
            :value @(rf/subscribe [:form-interval task-id :duration])
            :on-change #(rf/dispatch [:form-change-interval task-id :duration (.-target.value %)])}]]]])
     (when-not is-simple
       [:div.column.is-narrow
        [:div.field
         [:label.label "Dokonceno"]
         [:div.control @(rf/subscribe [:form-datetime task-id :finished_at])]
         [:div.control
          [:input.input
           {:type "date"
            :value @(rf/subscribe [:form-datetime task-id :finished_at])
            :on-change #(rf/dispatch [:form-change-datetime task-id :finished_at (.-target.value %)])}]
          [:input.input
           {:type "time"
            :value @(rf/subscribe [:form-datetime task-id :finished_at])
            :on-change #(rf/dispatch [:form-change-datetime task-id :finished_at (.-target.value %)])}]]]])
     [:div.column.is-narrow
      [:div.field.is-grouped
       [:div.control
        [:button.button.is-link
         {:disabled @(rf/subscribe [:form-is-error task-id])
          :on-click #(rf/dispatch [:form-save (u/form->task @(rf/subscribe [:formxx task-id]))])}
         "Ulozit"]]
       [:div.control
        [:button.button.is-test {:on-click #(rf/dispatch [:form-reset task-id])}
         "Zrusit"]]]]]))

(defn task-form-vert [props]
  [:div
   [:div.field
    [:label.label "id"]
    @(rf/subscribe [:form2 :_id])]
   [:div.columns
    [:div.column
     [:div.field
      [:label.label "Makac"]
      [:div.control
       [:div.select
        [:select.input {:value @(rf/subscribe [:form2 :worker])
                        :class (when (empty? @(rf/subscribe [:form2 :worker])) "is-danger")
                        :on-change #(rf/dispatch [:form2-change :worker (.-target.value %)])}
         (user-select-options @(rf/subscribe [:users]))]]]]]
    [:div.column
     [:div.field
      [:label.label "Mentor"]
      [:div.control
       [:div.select
        [:select.input {:value @(rf/subscribe [:form2 :mentor])
                        :on-change #(rf/dispatch [:form2-change :mentor (.-target.value %)])}
         (user-select-options @(rf/subscribe [:users]))]]]]]]
   [:div.columns
    [:div.column
     [:div.field
      [:label.label "Popis"]
      [:div.control
       [:input.input {:type "text"
                      :value @(rf/subscribe [:form2 :text])
                      :class (when (empty? @(rf/subscribe [:form2 :text])) "is-danger")
                      :on-change #(rf/dispatch [:form2-change :text (.-target.value %)])}]]]]]
   [:div.columns
    [:div.column.is-narrow
     [:div.field
      [:label.label "Odhad"]
      [:div.control
       [:input.input {:type "text"
                      :placeholder "1h"
                      :value @(rf/subscribe [:form2 :estimate])
                      :class (when-not @(rf/subscribe [:form2 :estimate]) "is-danger")
                      :on-change #(rf/dispatch [:form2-change :estimate (.-target.value %)])}]]]]
    [:div.column.is-narrow
     [:div.field
      [:label.label "Dedlajn"]
      [:div.control @(rf/subscribe [:form2 :deadline])]
      [:div.control
       [:input.input {:type "date"
                      :value @(rf/subscribe [:form2 :deadline])
                      :class (when-not @(rf/subscribe [:form2 :deadline]) "is-danger")
                      :on-change #(rf/dispatch [:form2-change :deadline (.-target.value %)])}]]]]]
   [:div.field.is-grouped
    [:div.control
     [:button.button.is-link
      {:disabled @(rf/subscribe [:form2-is-error])
       :on-click (fn []
                   (rf/dispatch [:form-save (u/form->task @(rf/subscribe [:form2xx]))])
                   (rf/dispatch [:set-modal nil nil]))}
      "Ulozit"]]
    [:div.control
     [:button.button.is-test {:on-click #(rf/dispatch [:form2-reset])}
      "Zrusit"]]]])

(defn task-karta [props]
  (let [task (:task props)
        t @(rf/subscribe [:t])
        is-running (:current_start_at task)
        is-overdue (> t (:deadline task))
        is-finished (:finished_at task)
        is-verified (:verified_at task)
        current-user @(rf/subscribe [:current-user])
        current-user-is-worker (= (:worker task) current-user)
        current-user-is-mentor (and (:mentor task) (= (:mentor task) current-user))]
    [:div.card.box {:class (cond is-running "has-background-success"
                                 is-verified "has-background-grey"
                                 is-finished "has-background-grey-light"
                                 is-overdue "has-background-danger")}
     [:div.columns
      [:div.column.is-narrow
       [:figure.image.is-32x32
        [:a {:href (routes/user {:id (:worker task)})}
         [:img {:src (str (:worker task) ".jpg")}]]]]
      [:div.column.is-narrow
       [:a {:href (routes/user {:id (:worker task)})} (:worker task)]
       (when (:mentor task)
         (str " (" (:mentor task) ")"))]
      [:div.column
       [:div.content
        ;;(:text task)
        (apply vector (cons :div (u/text-to-tag-links (:text task))))
        (for [tag (:tags task)]
          [:a {:href (routes/tag {:tag tag})
               :key tag}
              (str " #" tag)])]]
      [:div.column.is-narrow
       (u/format-seconds-fancy (u/calc-so-far task t))
       " ("
       (u/format-seconds-fancy (:estimate task))
       ")"]
      [:div.column.is-narrow (u/datetime->str (u/t->datetime (:deadline task)))]
      [:span.icon.is-large
       (cond
         current-user-is-worker
         (if is-running
           [:a {:on-click #(rf/dispatch [:task-pause (:_id task)])}
            [:i.fa.fa-pause.has-text-black]]
           [:a {:on-click #(rf/dispatch [:task-start (:_id task)])}
            [:i.fa.fa-play.has-text-black]])
         current-user-is-mentor
         (if (and is-finished (not is-verified))
           [:a {:on-click #(rf/dispatch [:task-verify (:_id task)])}
            [:i.fa.fa-check-double.has-text-black]]))]
      [:span.icon.is-large
       (cond
         current-user-is-worker
         (if is-finished
           [:a {:on-click #(rf/dispatch [:task-unfinish (:_id task)])}
            [:i.fa.fa-times.has-text-black]]
           [:a {:on-click #(rf/dispatch [:task-finish (:_id task)])}
            [:i.fa.fa-check.has-text-black]])
         current-user-is-mentor
         [:a {:on-click #(rf/dispatch [:task-unfinish (:_id task)])}
          [:i.fa.fa-times.has-text-black]])]
      [:span.icon.is-large
       [:a {:on-click #(rf/dispatch [:task-edit (:_id task)])}
        [:i.fa.fa-edit.has-text-black]]]
      [:span.icon.is-large
       [:a {:on-click #((when (js/confirm "fakt?")
                          (rf/dispatch [:task-delete (:_id task)])))}
        [:i.fa.fa-trash.has-text-black]]]]]))

(defn task-sort-fn-user [user-id]
  (juxt #(when user-id
           (cond (:current_start_at %) -10
                 (and (= (:mentor %) user-id) (:finished_at %) 0) -9
                 (not (:finished_at %)) -8
                 true 0))
        :deadline))

(defn task-sort-fn-user-BACKUP [user-id]
  (juxt #(when user-id
           (if-not (:finished_at %)
             0
             (if (= (:mentor %) user-id) -1 1)))
        :deadline))

(defn task-sort-fn-future []
  :deadline)

(defn task-sort-fn-past []
  #(- (:deadline %)))

(defn task-list [props]
  (let [user-id (:user-id props)
        tasks @(rf/subscribe [:tasks])
        forms @(rf/subscribe [:forms])
        nshow @(rf/subscribe [:nshow])
        sort-fn (:sort-fn props :deadline)]
    (if-not tasks
      [:div "...loading tasks..."]
      [:div
       (for [task (take nshow (sort-by sort-fn tasks))
             :let [task-id (:_id task)]]
                   ;;_ (prn (task-sort-fn task))]]
         [:div.column
          {:key task-id}
          (if (get forms task-id)
            [task-form-horiz {:task task}]
            [task-karta {:user-id user-id
                         :task task}])])
       (if (> (count tasks) nshow)
         [:div.columns
          [:div.column]
          [:div.column.is-narrow
           [:span.icon.is-large
            [:a {:on-click #(rf/dispatch [:nshow-inc])}
             [:i.fa.fa-chevron-circle-down.fa-3x]]]]
          [:div.column]])])))

(defn xicht [props]
  [:figure.image
   [:a {:href (routes/user {:id (:user-id props)})}
    [:img {:src (str (:user-id props) ".jpg")}]]])

(defn karta [props]
  (let [t (rf/subscribe [:t])]
    [:div.card
     [:div.card-image
      [xicht {:user-id (-> props :user :id)}]]
     [:div.card-content
      [:div.content
       (when-let [task (:task props)]
         [:span.title.is-4
          (apply vector (cons :div (u/text-to-tag-links (:text task))))
          (for [i (:tags task)]
            [:a {:href (routes/tag {:tag i}) :key i} (str " #" i)])])]]
          ;;" - "])]]
          ;;(u/format-seconds-fancy (u/calc-so-far task @t))])]]
      ;;[:div]
       ;;[:progress.progress.is-small.is-danger {:value 0.7}]
       ;;[:progress.progress.is-small.is-warning {:value 0.5}]
       ;;[:progress.progress.is-small.is-success {:value 0.3}]]]]))
     [:footer.card-footer
      [:div.card-footer-item (u/format-seconds-fancy (u/calc-so-far (:task props) @t))]]]))
     ;;  [:div.card-footer-item
     ;;   [:span.icon
     ;;    [:i.fa.fa-user]]]]]))

(defn loads [props]
  (let [load (:load props)]
   [:div
    (for [k [:load-1d :load-7d :load-30d]
          :let [load-value (-> load k :load)
                load-percent (int (* load-value 100))
                free (-> load k :free)
                free-fmt (u/format-seconds-fancy free)]]
      [:div {:key (str k)}
       (str load-percent) "%" [:div.is-pulled-right free-fmt]
       [:progress.progress.is-small {:value load-value
                                     :class (u/progress-to-class load-value)}]])]))

(defn karta-future [props]
  [:div.card
   [:div.card-image
    [xicht {:user-id (-> props :user :id)}]]
   [:div.card-content
    [:div.content
     (when-let [task (:task props)]
       [:div.content
        (apply vector (cons :div (u/text-to-tag-links (:text task))))])
        ;;" - "
        ;;@(rf/subscribe [:task-time-str task])])
     (let [load @(rf/subscribe [:user-load (-> props :user :id)])]
       [loads {:load load}])]]])
   ;;[:footer.card-footer
   ;; [:div.card-footer-item (u/format-seconds-fancy (u/calc-so-far (:task props) @t))]]]))
   ;;  [:div.card-footer-item
   ;;   [:span.icon
   ;;    [:i.fa.fa-user]]]]]))

(defn karta-past [props]
  [:div.card
   [:div.card-image
    [xicht {:user-id (-> props :user :id)}]]
   [:div.card-content
    [:div.content
     (when-let [task (:task props)]
       [:div.content
        (apply vector (cons :div (u/text-to-tag-links (:text task))))])
        ;;" - "
        ;;@(rf/subscribe [:task-time-str task])])
     (let [load @(rf/subscribe [:user-load-finished (-> props :user :id)])]
       [loads {:load load}])]]])
   ;;[:footer.card-footer
   ;; [:div.card-footer-item (u/format-seconds-fancy (u/calc-so-far (:task props) @t))]]]))
   ;;  [:div.card-footer-item
   ;;   [:span.icon
   ;;    [:i.fa.fa-user]]]]]))

(defn neco-jako-karta [props]
  (let [load @(rf/subscribe [:user-load (:user-id props)])
        load-finished @(rf/subscribe [:user-load-finished (:user-id props)])]
    [:div.level
     [loads {:load load-finished}]
     [:div {:style {:width "200px"}}
      [xicht {:user-id (:user-id props)}]]
     [loads {:load load}]]))

(defn navbar []
  [:nav.navbar {:role "navigation"}
   [:div.navbar-menu
    [:div.navbar-start
     [:a.navbar-item {:href (routes/dashboard)} "dashboard"]
     [:a.navbar-item {:href (routes/tasks)} "tasks"]
     [:a.navbar-item {:href (routes/past)} "past"]
     [:a.navbar-item {:href (routes/future)} "future"]]]])

(defn page-user [props]
  [:div
   [countdown]
   [:div.section
    [neco-jako-karta {:user-id (:user-id props)}]]
   [:div.section
    [:div.columns
     [:div.column
      [:button.button.is-danger {:style {:width "100%"}
                                 :on-click #(rf/dispatch [:quick-task (:user-id props)])}
       [:i.fa.fa-exclamation-triangle.has-text-black]
       "RYCHLEJ UKOL !!!!"
       [:i.fa.fa-exclamation-triangle.has-text-black]]]]
    [task-form-horiz {:is-simple 1}]
    [task-list {:user-id (:user-id props)
                :sort-fn (task-sort-fn-user (:user-id props))}]]])

(defn page-tasks [props]
  [:div
   [:div.section
    [task-form-horiz {:is-simple 1}]
    [task-list]]])

(defn page-dashboard-xx []
  [:div
   [:section.section
    [:div
     (for [i @(rf/subscribe [:working])]
       [:div.is-pulled-left {:style {:width "200px"}}
        [karta i]])]]])

(defn page-dashboard []
  [:div
   [:section.section
    (let [working @(rf/subscribe [:working])
          lines 2
          per-line (int (Math/ceil (/ (count working) lines)))]
      (for [i (partition per-line per-line [] working)]
        [:div.columns {:key (str "HACK" i)}
         (for [j i]
           [:div.column.is-2 {:key (str "HACK" i j)}
            [karta j]])]))]])

(defn page-future []
  [:div
   [:section.section
     [:div.columns
      (for [i @(rf/subscribe [:working])]
        [:div.column
         [karta-future i]])]]
   [:section.section
    [task-form-horiz]
    [task-list {:sort-fn (task-sort-fn-future)}]]])

(defn page-past []
  [:div
   [:section.section
     [:div.columns
      (for [i @(rf/subscribe [:working])]
        [:div.column
         [karta-past i]])]]
   [:section.section
    [task-list {:sort-fn (task-sort-fn-past)}]]])

(defn page-tag [props]
  [:div
   [:section.section
    [:div.columns
     [:div.column
      "hovno tags"]]]
   [:section.section
    [task-list]]])

(defn mejn []
  [:div
   [modal-modal]
   [navbar]
   (let [page @(rf/subscribe [:page])
         page-props @(rf/subscribe [:page-props])]
     (case page
       :dashboard [page-dashboard page-props]
       :user [page-user page-props]
       :tasks [page-tasks page-props]
       :future [page-future page-props]
       :past [page-past page-props]
       :tag [page-tag page-props]
       [:div "error"]))
   [debug-app-db]])

(defn init-sse! []
  (let [sse (js/EventSource. "http://127.0.0.1:8888/stream")]
    (.addEventListener sse "message"
      (fn [e] (prn "stream") (rf/dispatch [:tasks-get nil])))))
      ;;(fn [e] (rf/dispatch [:new-data (js->clj (js/JSON.parse (.-data e)))])))

(defn init! []
  (routes/hook-browser-navigation!)
  (rf/dispatch-sync [:initialize])
  (rf/dispatch [:timer])
  (rf/dispatch [:countdown])
  (routes/initial-dispatch)
  (reagent/render-component [mejn] (.getElementById js/document "app")))

(init!)
;;(init-sse!)
