(ns humpback.xxx
  (:require [humpback.core :refer :all])
  (:use seesaw.core))


(def joey-ip "192.168.219.144")
(def etcd-port "4001")
(def joey (construct-url joey-ip etcd-port))
(get-room-list joey)

(def my-ip "192.268.219.144")
(def etcd-port "4001")
(def foo (construct-url my-ip etcd-port))
(get-room-list foo)