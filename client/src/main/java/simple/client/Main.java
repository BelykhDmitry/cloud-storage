package simple.client;

import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Main {

    private static void run() {
        ObjectEncoderOutputStream oeos = null;
        ObjectDecoderInputStream odis = null;
        try {
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Connected");
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            AuthMessage authMessage = new AuthMessage("Dmitrii");
            oeos.writeObject(authMessage);
            oeos.flush();
            ServerCallbackMessage answer = (ServerCallbackMessage) odis.readObject();
            System.out.println(answer.getStatus().name());
            oeos.writeObject(new CmdMessage("Ping"));
            oeos.flush();
            System.out.println(((ServerCallbackMessage) odis.readObject()).getStatus());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                oeos.close();
                odis.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        run();
    }
}
