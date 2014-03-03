import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.swing.SwingBuilder

import java.awt.Font

import javax.swing.JFileChooser
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.impl.client.DefaultHttpClient;

class BlueWhale {
    private static final URL = "http://192.168.1.140:4002/"
    private static final BASE = URL + "v2/keys"
    private static final ROOMS = "whalesong/rooms/"
    private static final NAME = "name"
    private static final CAPTION = "caption"
    private static final MESSAGES = "messages"
    
    public def whalesongRooms = []
    public def currentRoom
    public def currentRoomName
    public def currentRoomCaption
    public def currentRoomMessages
    public def postMessage
    
    public static void main(String[] args) {
        println "BlueWhale starting up..."
        BlueWhale bw = new BlueWhale();
        bw.getRooms()
        bw.listRooms()
        bw.go();
    }
    
    public void listRooms() {
        println "Found " + whalesongRooms.size() + " Rooms: "
        whalesongRooms.each { room->
            println room.name + " - " + room.caption
        }
    }
    
    public void getRooms() {
        def url = BASE + "/" + ROOMS
        def roomsJson = url.toURL().text
        def rooms = new JsonSlurper().parseText(roomsJson)
        rooms.node.nodes.each { n->
            try {
            url = BASE + "/" + n.key + "/" + NAME
            def name = new JsonSlurper().parseText(url.toURL().text).node.value
            
            url = BASE + "/" + n.key + "/" + CAPTION
            def caption = new JsonSlurper().parseText(url.toURL().text).node.value

            Expando room = new Expando()
            room.key = n.key
            room.name = name
            room.caption = caption
            whalesongRooms.add(room)
            
            } catch (Exception e) { 
                println e
            }
        }
    }
    
    public void go() {
        Font heading = new Font("sans", Font.BOLD, 24)
        Font roomHead = new Font("sans", Font.BOLD, 14)
        Font roomCap = new Font("sans", Font.BOLD, 12)
        def swing = new SwingBuilder()
        currentRoomName = swing.label("--no room selected--")
        currentRoomName.setFont(roomHead)
        currentRoomCaption = swing.label("")
        currentRoomCaption.setFont(roomHead)
        currentRoomMessages = swing.textArea(columns: 50, rows:10)

        postMessage = swing.textField(columns: 40)
        def post = swing.button('Post')
        
        def done = swing.button('Quit')
                
        def frame = swing.frame(title: 'Add Client',location:[100,100],size:[500,500]) {
            lookAndFeel("system")
            menuBar() {
                menu(text: "Pick A Room", mnemonic: 'F') {
                    whalesongRooms.each { room->
                        menuItem(text: room.name, actionPerformed: { pickRoom(room) })
                    }
                }
            }
            
            panel {
                tableLayout {
                    tr {
                        td { label(text: 'Whalesong', font: heading) }
                        td { label('                                    ') }
                        td { label(icon:imageIcon('images/whalesong.png')) }
                    }
                    tr {
                        td { widget(currentRoomName) }
                        td (colspan: 2) { widget(currentRoomCaption) }
                    }
                    tr {
                        td (colspan: 3) { scrollPane {widget(currentRoomMessages)} }
                    }
                    tr {
                        td (colspan: 2) { widget(postMessage) }
                        td (align:'LEFT') { widget(post) }
                    }
                    tr {
                        td { label('') }
                        td { label('') }
                        td (align:'RIGHT') { widget(done) }
                    }

                }
            }
        }
        
        frame.pack()
        frame.show()
        
        post.actionPerformed = {
            doPost()
        }
        
        done.actionPerformed = {
            System.exit(0)
        }

    }

    public void doPost() {
        def text = postMessage.getText()
        def room = currentRoom
        def url = BASE + room.key + "/" + MESSAGES + "/"
        
        println "POSTING \'" + text + "\' TO " + url
        
        if((text != null) && (text.length()>1) ) {
            
            DefaultHttpClient httpclient = new DefaultHttpClient();
            
            HttpPost httpost = new HttpPost(url)
            
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("value", text));
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    
            HttpResponse response = httpclient.execute(httpost);
            println "status: " + response
        }
        postMessage.setText("")
        
        pickRoom(room)
    }
    
    public void pickRoom(def room) {
        println "Picked room: " + room.name + " (" + room.key + ")"
        currentRoom = room
        currentRoomName.setText(room.name)
        currentRoomCaption.setText(room.caption)
        
        Map msgHash = [:]
        def url = BASE + room.key + "/" + MESSAGES + "/?sorted=true"
        def messages = new JsonSlurper().parseText(url.toURL().text)
        println "Messages:" + messages
        messages.node.nodes.each { msg->
            if(msg.value != null) {
                msgHash[msg.createdIndex] = msg.value
            }
        }
        Map sortMsg = msgHash.sort { a, b -> 
            if(a.key > b.key) return 1
            else return -1
        }
        
        def text = new StringBuilder()
        sortMsg.each{key, value->
            text.append(value)
            text.append("\n")
        }
        currentRoomMessages.setText(text.toString())
    }

}
