(ns humpback.core-test
  (:require [clojure.test :refer :all]
            [humpback.core :refer :all]))

(def ip "192.168.219.144")
(def etcd-port "4001")
(def url (construct-url ip etcd-port))

;(deftest a-test
;  (testing "FIXME, I fail."
;    (is (= 0 1))))

;(deftest test-get-room-list
;  (testing "get-room-list"
;    (let [room-list (get-room-list url)]
;      (is (not (nil? room-list))))))
;
;
;;; (def carl (construct-url carl-ip etcd-port))
;
;(get-room-list joey)
;(get-room joey "room1")
;
;;;
;(create-room joey "Test1" "Testing")
;(show-room-info (get-room joey "Test1"))
;(post-message joey "Test1" "First Post!")
;(post-message joey "Test1" "Second Post!")
;(show-messages (get-messages-for-room joey "Test1"))
;
;(create-room joey "Test3" "Foo bar bar")
;
;(delete-room joey "Test1")
;
