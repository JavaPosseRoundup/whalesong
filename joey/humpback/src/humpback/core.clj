(ns humpback.core
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.string :as string]))

;; (def carl-ip "192.168.0.14")
(def joey-ip "192.168.0.22")
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

(defn- post-url
  "Executes a POST against the given URL"
  [url payload]
  (let [full-payload {:form-params payload
                      :throw-entire-message? true
                      :force-redirects true
                      :accept :json}]
    (client/post url full-payload)))

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

(defn post-message
  "Send a message to the chat room"
  [url room-name message]
  (let [room-url (add-to-url url (format "/rooms/%s/messages" room-name))]
    (post-url room-url {:value (str message)})))

(defn get-messages-for-room
  "Get all messages from the chat room"
  [url room-name]
  (let [room-url (add-to-url url (format "/rooms/%s/messages" room-name))]
    (get-url room-url)))

(defn show-messages
  "Display relevant information about a room."
  [room]
  (let [body (:body room)
        body-json (parse-string body)
        node (get body-json "node")
        name (get node "key")
        children (get node "nodes")
        sorted-children (sort-by get-numeric-message-id children)
        messages (for [m sorted-children] (get m "value"))]
    messages))

(defn- get-numeric-message-id [node]
  (let [key (get node "key")
        chunks (string/split key #"/")]
    (Integer/valueOf (last chunks))))

(defn- show-output [resp]
  (println (:body resp)))

(def joey (construct-url joey-ip etcd-port))
;; (def carl (construct-url carl-ip etcd-port))

(get-room-list joey)
(get-room joey "room1")

;;
(create-room joey "Test1" "Testing")
(show-room-info (get-room joey "Test1"))
(post-message joey "Test1" "First Post!")
(post-message joey "Test1" "Second Post!")
(show-messages (get-messages-for-room joey "Test1"))
;; (delete-room joey "YYY9")




