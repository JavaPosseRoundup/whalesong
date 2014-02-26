(ns hello-seesaw.core
  (:use seesaw.core))

;;(defn -main [& args]
;;  (invoke-later
;;    (-> (frame :title "Hello",
;;          :content "Hello, Seesaw",
;;          :on-close :exit)
;;      pack!
;;      show!)))

(native!)

(def f (frame :title "Seesaw Test"))

(-> f pack! show!)

(config f :title)