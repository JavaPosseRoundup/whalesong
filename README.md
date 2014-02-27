# whalesong

Peer to peer chat system with etcd in a Docker container.

Theme song: [http://upload.wikimedia.org/wikipedia/commons/1/13/Humpbackwhale2.ogg]

## Discovery Token

This discovery token can be passed to etcd to tell it to associate with our cluster.
```
-discovery https://discovery.etcd.io/91ddc8a36d3e101c22a88e67c92b6f91
```

## Etcd Structure

```
/whalesong/
  rooms/
    room1/
      name='A room name'
      caption='This room is really cool'
      messages/
        1='hi'
        2='dood!'
        3='huh?'
        ...
    room2/
      name='Another nice room'
      caption='This room is way better'
      messages/
        1='hmmm'
        2='I like docker'
        3='I like etcd!'

```

## Etcd Startup

See carl/start for a sample shell script for starting an etcd docker container that joins our cluster.

## Etcd initial load (use this to create some rooms and messages for testing)

curl -L -X PUT http://192.168.0.14:4002/v2/keys/whalesong/rooms/room1/name -d value="Java%20Posse%20House"

curl -L -X PUT http://192.168.0.14:4002/v2/keys/whalesong/rooms/room1/caption -d value="Where%20the%20elite%20meet"

curl -L -X POST http://192.168.0.14:4002/v2/keys/whalesong/rooms/room1/messages -d value="Carl%20says%20hi"

curl -L -X POST http://192.168.0.14:4002/v2/keys/whalesong/rooms/room1/messages -d value="Rob%20says%20hi%20back"

curl -L -X PUT http://192.168.0.14:4002/v2/keys/whalesong/rooms/room2/name -d value="Crested%20Butte%20Food"

curl -L -X PUT http://192.168.0.14:4002/v2/keys/whalesong/rooms/room2/caption -d value="Talk%20About%20CB%20Food"

curl -L -X POST http://192.168.0.14:4002/v2/keys/whalesong/rooms/room2/messages -d value="Teocali%20is%20yummy"

curl -L -X POST http://192.168.0.14:4002/v2/keys/whalesong/rooms/room2/messages -d value="Fig%20pizza%20is%20best"

