(ns hello-seesaw.core
  (:require [humpback.core :as humpback])
  (:use seesaw.core))

;;(defn -main [& args]
;;  (invoke-later
;;    (-> (frame :title "Hello",
;;          :content "Hello, Seesaw",
;;          :on-close :exit)
;;      pack!
;;      show!)))

(native!)

(def f (frame :title "Humpback v0.1"))

(-> f pack! show!)
(config! f :title "Humpback v0.1")
(println (config f :title))

;; change it
;; (config! f :title "No RLY, get to know Seesaw!")

(config! f :content "This is some content")

(def lbl (label "I'm another label"))
(config! f :content lbl)

(defn display [content]
  (config! f :content content)
  content)
;=> #'user/display

(display lbl)

(config! lbl :background :pink :foreground "#00f")

(config! lbl :font "ARIAL-BOLD-21")

(use 'seesaw.font)
;=> nil
(config! lbl :font (font :name :monospaced :style #{:bold :italic}
                     :size 18))

(def b (button :text "Send!"))
;=> #'user/b

; Sometimes we might want to show a message ...
;(alert "I'm an alert")
;=> nil

; or get input from the user ...
;(input "What's your favorite color?");

(display b)

;;(def button-listener (listen b :action (fn [e] (alert e "Thanks!"))))
;; the returned function can be called to remove the listener
;; (button-listener)
;; (*1)

;; (listen) can register multiple event handlers at once
(listen b :mouse-entered #(config! % :foreground :blue)
  :mouse-exited #(config! % :foreground :red))

;; (def lb (listbox :model (-> 'seesaw.core ns-publics keys sort)))
;; (def lb (listbox :model [:foo :bar :baz])) ;; any list can provide the data
(def lb (listbox :model (humpback/get-room-names "http://192.168.1.114:4001/v2/keys/whalesong")))
;; (display lb)

;; Make it scrollable
(display (scrollable lb))

;; Get what is selected
(selection lb)

;; (type *1)

(def messages-lb (listbox))

(def area (text :multi-line? true :font "MONOSPACED-PLAIN-14"))

(def edit-split (left-right-split (scrollable area) b :divider-location 3/4))

(def messages-split (top-bottom-split (scrollable messages-lb) edit-split :divider-location 3/4))
;; (display split)

(def major-split (left-right-split (scrollable lb) messages-split :divider-location 1/3))

(defn update-messages [room listbox]
  (let [messages (humpback/show-messages (humpback/get-messages-for-room "http://192.168.1.114:4001/v2/keys/whalesong" room))]
    (config! listbox :model messages)))

(def room-name-listener
  (listen lb :selection (fn [e] (config! messages-lb :model (update-messages (selection e) messages-lb)))))
;; (room-name-listener)

(def button-listener
  (listen b :action
    (fn [e]
      (humpback/post-message "http://192.168.1.114:4001/v2/keys/whalesong" (selection lb) (text area))
      (update-messages (selection lb) messages-lb))))
;; (button-listener)

(config! lb :model (humpback/get-room-names "http://192.168.1.114:4001/v2/keys/whalesong"))
(display major-split)

(config! area :size [200 :by 100])
(config area :size)

(config! f :size [640 :by 480])

(config! b :size [100 :by 100])
;; (config f :size)
(pack! f)





