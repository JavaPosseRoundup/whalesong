(ns humpback.gui
  (:require [humpback.core :refer :all])
  (:use seesaw.core))

;;(defn -main [& args]
;;  (invoke-later
;;    (-> (frame :title "Hello",
;;          :content "Hello, Seesaw",
;;          :on-close :exit)
;;      pack!
;;      show!)))

(def etcd-ip "192.168.219.147")
(def etcd-port "4001")
(def etcd (construct-url etcd-ip etcd-port))

(println etcd)
(native!)

(def f (frame :title "Humpback v0.1"))
(defn display [content]
  (config! f :content content)
  content)

(def b (button :text "Send!"))

(def rooms-lb (listbox :model (get-room-names etcd)))

(def messages-lb (listbox))

(def area (text :multi-line? true :font "MONOSPACED-PLAIN-14"))

(def edit-split (left-right-split (scrollable area) b :divider-location 3/4))

(def messages-split (top-bottom-split (scrollable messages-lb) edit-split :divider-location 3/4))

(def major-split (left-right-split (scrollable rooms-lb) messages-split :divider-location 1/3))

(defn update-messages [url room listbox]
  (let [messages (show-messages (get-messages-for-room url room))]
    (println messages)
    (config! listbox :model messages)))

(def room-name-listener
  (listen rooms-lb :selection (fn [e] (println e) (update-messages etcd (selection e) messages-lb))))
;; (room-name-listener)

(def button-listener
  (listen b :action (fn [e]
                      (post-message etcd (selection rooms-lb) (text area))
                      (update-messages etcd (selection rooms-lb) messages-lb)
                      (text! area ""))))
;; (button-listener)

(config! rooms-lb :model (get-room-names url))
(display major-split)
;(update-messages etcd "Test1" messages-lb)
(config! f :size [640 :by 480])

(pack! f)
(show! f)
