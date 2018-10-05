package com.example.itdevjr1.kiosk.WebSocket;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import java.net.URISyntaxException;
/**
 * Created by paolohilario on 2/8/18.
 */

public class SocketApplication {

    private Socket mSocket;{
        try {

            mSocket = IO.socket("https://socket.lay-bare.com");
//            https://lbo-express.azurewebsites.net
//            https://socket.lay-bare.com/
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

}