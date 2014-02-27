# whalesong

Peer to peer chat system with etcd in a Docker container.

Theme song: [http://upload.wikimedia.org/wikipedia/commons/1/13/Humpbackwhale2.ogg]

## Discovery Token

This discovery token can be passed to etcd to tell it to associate with our cluster.
```
-discovery https://discovery.etcd.io/66ea6c39d90bfab819bede8661041181
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

See populate for a sample script for creating and populating a couple of rooms.
