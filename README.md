# whalesong

Peer to peer chat system with etcd in a Docker container.

Theme song: [http://upload.wikimedia.org/wikipedia/commons/1/13/Humpbackwhale2.ogg]

## Discovery Token

```
https://discovery.etcd.io/91ddc8a36d3e101c22a88e67c92b6f91
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

## Etcd Container startup

First export the name, ports and IP addresses involved:
```
export NODE=node4
export LOCAL_IP=192.168.0.32
export LOCAL_PORT=4001
export LOCAL_PPORT=7001
export REMOTE_IP=192.168.0.32
export REMOTE_PPORT=7001
```

Then run the docker container with all the ports and names wired up:
```
docker run -d -p $LOCAL_PPORT:$LOCAL_PPORT -p $LOCAL_PORT:$LOCAL_PORT coreos/etcd -peer-addr $LOCAL_IP:$LOCAL_PPORT -addr $LOCAL_IP:$LOCAL_PORT -name $NODE -peers $REMOTE_IP:$REMOTE_PPORT
```
