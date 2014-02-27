#!/bin/sh
# -*- mode: python; coding: utf-8 -*-
""":"
cd $(dirname $0)
/usr/bin/env python $0 "$@"
exit $?
":"""

import sys, os, inspect
import SocketServer
import urllib2, json
import threading

class Json(urllib2.Request):
    @classmethod
    def request(cls, url, *args, **kw):
        request = cls(url, *args,**kw)
        try:
            response = urllib2.build_opener(urllib2.HTTPHandler).open(request)
        except urllib2.HTTPError, e:
            response = e
        data = json.load(response)
        print request.get_method(), url,
        if request.has_data():
            print request.data,
        print '->', data
        return data
    def get_method(self):
        return self.__class__.__name__

class GET(Json): pass
class PUT(Json): pass
class POST(Json): pass
class DELETE(Json): pass

VERBS = {'GET':GET,'PUT':PUT,'POST':POST,'DELETE':DELETE}

class Etcd(object):
    _nick = "etcd"
    def __init__(self,
                 server=os.environ.get("ETCD","http://172.17.42.1:4001/")):
        self._server = server
    def _key(self, key):
        if key.startswith('/'): key = key[1:]
        return os.path.join(self._server,"v2/keys",key)
    def mkdir(self, key):
        return PUT.request(self._key(key), data="dir=true")
    def poll(self, key):
        return self.get(key+"?recursive=true&wait=true")
    def get(self, key):
        return GET.request(self._key(key))
    def put(self, key, value):
        return PUT.request(self._key(key), data="value="+value)
    def post(self, key, value):
        return POST.request(self._key(key), data="value="+value)
    def delete(self, key):
        return DELTE.request(self._key(key))
    def _write(self, sender, topic, message):
        "PRIVMSG sent to 'etcd' - diagnostics..."
        verb = 'GET'
        more = message.split(' ', 1)
        if len(more) > 1:
            verb, more = more
        else:
            more = more[0]
        action = VERBS.get(verb)
        if action is None:
            return ['PRIVMSG etcd :Bad verb: ' + verb]
        more = more.split(' ', 1)
        path = more[0]
        if path.startswith('/'): path = path[1:]
        path = os.path.join(self._server,path)
        if len(more) > 1:
            response = action.request(path, data=more[1])
        else:
            response = action.request(path)
        return [':etcd PRIVMSG %s :%s' % (sender, json.dumps(response))]

class Disconnect(Exception): pass

class Handler(object):
    def __init__(self, connection, **args):
        self.connection = connection
        self.args = args

    def __call__(self, request, client_address, server):
        out = request.makefile('wb', 0)
        args = dict(self.args)
        args['client_address'] = client_address
        args['server'] = server
        args['handler'] = self
        args['out'] = out
        connection = self.connection(**args)
        try:
            for line in request.makefile('rb', -1):
                line = line.strip()
                if not line: return
                print '>', line
                line = line.split(" ", 1)
                if len(line) > 1:
                    cmd, args = line
                else:
                    cmd = line[0]; args = ""
                handler = getattr(connection, cmd)
                argcount = len(inspect.getargspec(handler).args) - 2
                try:
                    result = handler(*args.split(" ", max(0, argcount)))
                except Disconnect:
                    return
                if result:
                    for line in result:
                        print '<', line
                        out.write(line + "\r\n")
        finally:
            connection._done()

class Connection(object):
    def __init__(self, out, etcd, server, client_address, handler, **junk):
        self._client_address = client_address
        self._handler = handler
        self._etcd = etcd
        self._server = server
        self._out = out
    def __getattr__(self, name):
        if name.startswith('_'):
            return object.__getattr__(self,name)
        def handler(args):
            print "!!! Unsupported command:", name, args
        return handler
    def _done(self):
        pass

class IRC(Connection):
    _servername = "localhost"
    _nick = None
    _name = None
    def _write(self, sender, topic, message):
        data = ":%s PRIVMSG %s :%s\r\n" % (sender, topic, message)
        print '<', data[:-2]
        self._out.write(data)
    def _done(self):
        for room in self._handler.rooms.values():
            room.remove(self)

    def NICK(self, nick):
        owner = self._handler.clients.get(nick)
        if owner is not None and owner is not self:
            return [":%s 433 :%s"%(self._servername,nick)]
        for c in nick:
            if (c < 'a' or c > 'z') and (c < 'A' or c > 'Z'):
                return [":%s 432 :%s"%(self._servername,nick)]
        try:
            del self._handler.clients[self._nick]
        except KeyError:
            pass
        self._nick = nick
        self._handler.clients[nick] = self
        return self._login()

    def USER(self, name, number, star, more):
        self._name = name
        return self._login()

    def _login(self):
        if self._nick is not None and self._name is not None:
            return [":%s 001 %s :Welcome to etcd IRC" % (
                    self._servername,self._nick)]

    def PING(self, data):
        return [":%s PONG %s :%s"%(self._servername,self._servername, data)]

    def JOIN(self, name):
        if not name.startswith('#'):
            return [":%s 475 %s :Cannot join channel"%(self._servername,name)]
        name = name[1:]
        room = self._handler.rooms.get(name)
        if room is None:
            room = Room(name, self._etcd)
            self._handler.rooms[name] = room
        room.add(self)
        title = room.title
        response = [":%s JOIN #%s"%(self,name)]
        if title is not None:
            response.append(":%s 332 #%s :%s"%(self._servername, name, title))
        response.append(":%s 353 %s @ #%s :%s"%(
                self._servername, self._nick, name, self._nick))
        response.append(":%s 366 %s #%s :End of /NAMES list."%(
                self._servername, self._nick, name))
        return response

    def PRIVMSG(self, name, message):
        if message.startswith(':'):
            message = message[1:]
        if name.startswith('#'):
            target = self._handler.rooms.get(name[1:])
        else:
            target = self._handler.clients.get(name)
        if target is None:
            print "No such user / room:", name
        else:
            return target._write(self._nick, name, message)

    def QUIT(self, nothing):
        raise Disconnect

    def TOPIC(self, name, message):
        room = self._handler.rooms.get(name[1:])
        if room is None:
            return []
        room.title = message[1:]
        return [":%s TOPIC %s %s"%(self, name, message)]

    def __str__(self):
        return "%s!~%s@%s"%(self._nick,self._name,self._client_address[0])

class Room(threading.Thread):
    def __init__(self, name, etcd):
        threading.Thread.__init__(self, name=name)
        self.daemon = True
        self._room = name
        self._key = "/whalesong/rooms/" + name
        etcd.mkdir(self._key)
        etcd.mkdir(self._key+"/messages")
        self._members = set()
        self._etcd = etcd
        self._running = True
        self.start()
    @property
    def title(self):
        caption = self._etcd.get(self._key + '/caption').get('node',{})
        return caption.get("value")
    @title.setter
    def title(self, value):
        self._etcd.put(self._key + '/caption', value)
    def run(self):
        while self._running:
            update = self._etcd.poll(self._key).get('node')
            key = update['key']
            if key.endswith('/caption') or key.endswith('/name'):
                continue
            value = update.get('value')
            if value is not None:
                for member in self._members:
                    member._write("UnknownSender", "#"+self._room, value)
    def _write(self, sender, topic, message):
        self._etcd.post(self._key+"/messages", message)
    def add(self, member):
        if member not in self._members:
            self._members.add(member)
            messages = self._etcd.get(self._key+'/messages?recursive=true&sorted=true')
            for message in messages.get('node',{}).get('nodes',[]):
                value = message.get('value')
                if value is not None:
                    member._write("History", "#"+self._room, value)
    def remove(self, member):
        try:
            self._members.remove(member)
        except KeyError:
            pass

def irc_server(host=os.environ.get("HOST", "0.0.0.0"),
               port=int(os.environ.get("PORT","6667"))):
    etcd = Etcd()
    etcd.mkdir("/whalesong")
    etcd.mkdir("/whalesong/rooms")
    handler = Handler(IRC, etcd=etcd)
    handler.clients = {'etcd':etcd}
    handler.rooms = {}
    for room in etcd.get("/whalesong/rooms")['node'].get('nodes',[]):
        name = room['key'].split('/')[-1]
        handler.rooms[name] = Room(name, etcd)
    server = SocketServer.ThreadingTCPServer((host, port), handler)
    server.daemon_threads = True
    return server

if __name__ == '__main__':
    server = irc_server()
    print "Running etcd IRC server:", server.server_address, "..."
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        server.shutdown()
        print "bye."
        sys.exit(0)
