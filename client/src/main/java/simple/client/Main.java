package simple.client;

import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    private static void run() {
        ObjectEncoderOutputStream oeos = null;
        ObjectDecoderInputStream odis = null;
        try {
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Connected");
            System.out.flush();
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            AuthMessage authMessage = new AuthMessage("User1", "1234");
            oeos.writeObject(authMessage);
            oeos.flush();
            ServerCallbackMessage answer = (ServerCallbackMessage) odis.readObject();
            System.out.println(answer.getStatus().name());
            System.out.flush();
            FileMessage file = new FileMessage("Example.txt", Files.readAllBytes(Paths.get("Example.txt")));
            oeos.writeObject(file);
            oeos.flush();
            answer = (ServerCallbackMessage) odis.readObject();
            System.out.println(answer.getStatus().name());
            System.out.flush();
            /*oeos.writeObject(new CmdMessage("Ping"));
            oeos.flush();
            System.out.println(((ServerCallbackMessage) odis.readObject()).getStatus());
            System.out.flush();*/
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
