(ns frontend.moria-experiments)

;;(def at (atom {:hovno 5}))

;; (defn onklik [e]
;;   (swap! at #(update-in % [:hovno] inc)))

;; (defn onklikdec [e]
;;   (swap! at #(update-in % [:hovno] dec)))

;; (defn helinc [vnode]
;;   (let [val (get-in @at [:data "MoistureProbe1" "info" "d" "value"])]
;;     (m/m "div.button.is-link" {:onclick onklik} "inc" (:hovno @at) "---" val)))

;; (defn heldec [vnode]
;;   (m/m "div.button.is-link" {:onclick onklikdec} "dec" (:hovno @at) (str (keys (js->clj vnode)))))

;; (def hovinc {:view helinc})

;; (def hovdec {:view heldec})

;; (defn mm [x params]
;;   (m/m (clj->js {:view x}) params))

;; (defn guru [vnode]
;;   (m/m "div" [(m/m (clj->js hovinc))
;;               "volxxx"
;;               (m/m (clj->js hovdec))]))

;; (defn card [vnode]
;;   (m/m "div.card" ["nasrat"
;;                    (str (js->clj vnode.attrs))
;;                    (m/m "img" {:src "https://blog.mozilla.org/firefox/files/2017/12/firefox-logo-300x310.png" :width 100})]))

;; (defn dash [vnode]
;;   (m/m "div.columns" [(m/m "div.column" {:key "xx"} "ahoj")
;;                       ;(m/m (clj->js {:view card}))
;;                       (mm card {:key "vole" :param "xxx"})
;;                       (m/m "div.column" {:key "hoj"} "vole")]))

;; (defn morinit! []
;;   ;(hook-browser-navigation!)
;;   ;;(m/mount (.getElementById js/document "app") hov)
;;   (m/route (.getElementById js/document "app")
;;            "/hhh"
;;            ;;{"/hhh" {:view guru}}
;;            {"/hhh" {:view dash}}))

;(morinit!)


;; (.addEventListener sse "message"
;;   (fn [e]
;;     (swap! at (fn [x] (assoc x :data (js->clj (js/JSON.parse (.-data e))))))
;;     (m/redraw)))
