(ns humpback.core
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.pprint :as pprint]))

(def carl-ip "192.168.0.14")
(def joey-ip "192.168.0.27")
(def etcd-port "4001")

(defn- construct-url
  "Builds a URL string from the specified parts"
  [ip port]
  (format "http://%s:%s/v2/keys/whalesong" ip port))

(defn- get-url
  "Fetches the specified URL from the server"
  [url]
  (client/get url {:throw-entire-message? true
                   :accept :json}))

(defn- put-url
  "Executes a PUT against the given URL"
  [url payload]
  (let [full-payload {:form-params payload
                      :content-type :x-www-form-urlencoded
                      :force-redirects true
                      :throw-entire-message? true
                      :accept :json}]
    (client/put url full-payload)))

(defn- delete-url
  "Executes a DELETE against the given URL"
  [url]
  (client/delete url {:throw-entire-message? true
                      :force-redirects true
                      :accept :json}))

(defn- add-to-url
  "Appends the given string to the url"
  [url rest]
  (format "%s%s" url rest))

(defn get-room-list
  "Connect to the specified server:port and fetch the list of available rooms."
  [url]
  (let [room-list-url (add-to-url url "/rooms")]
    (get-url room-list-url)))

(defn get-room
  "Get the node for the specified chat room from the server"
  [url room-name]
  (let [room-url (add-to-url url (format "/rooms/%s" room-name))]
    (get-url room-url)))

(defn show-room-info
  "Display relevant information about a room."
  [room]
  (let [body (:body room)
        body-json (parse-string body)
        node (get body-json "node")
        name (get node "key")
        children (get node "nodes")
        caption (get (get children 0) "value")]
    [name caption]))

(defn create-room
  "Create a new room on the chat server"
  [url room-name caption]
  (let [room-url (add-to-url url (format "/rooms/%s/caption" room-name))]
    (put-url room-url {:value (str caption)})))

(defn delete-room
  "Permanently delete an entire chat room"
  [url room-name]
  (let [room-url (add-to-url url (format "/rooms/%s?dir=true" room-name))]
    (delete-url room-url)))

(defn- show-output [resp]
  (println (:body resp)))

(def joey (construct-url joey-ip etcd-port))
(def carl (construct-url carl-ip etcd-port))

(get-room-list joey)
(get-room joey "room1")

(create-room joey "Boo-Yah" "This is a test room")
(show-output (get-room joey "Boo-Yah"))
(show-output (get-room joey "XXX"))

(def xxx (get-room joey "XXX"))
(show-room-info (get-room joey "XXX"))

(create-room joey "YYY9" "Testing")
(show-room-info (get-room joey "YYY9"))

(delete-room joey "YYY9")




