package simple.client;

import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    private static void run() {
        //for (int i = 0; i < 5; i++) {
            int j = 0;
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ObjectEncoderOutputStream oeos = null;
                ObjectDecoderInputStream odis = null;
                try {
                    Socket socket = new Socket("localhost", 8189);
                    System.out.println("Connected");
                    System.out.flush();
                    oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
                    odis = new ObjectDecoderInputStream(socket.getInputStream());
                    AuthMessage authMessage = new AuthMessage("User"+j, "12345", false);
                    oeos.writeObject(authMessage);
                    oeos.flush();
                    ServerCallbackMessage answer = (ServerCallbackMessage) odis.readObject();
                    System.out.println(answer.getStatus().name());
                    System.out.flush();
                    oeos.writeObject(new CmdMessage("", CmdMessage.CmdType.REMOVE_FOLDER));
                    oeos.flush();
                    System.out.println(answer.getStatus().name());
                    System.out.flush();
                    /*String fileName = "Задача1.png";
                    FileMessage file = new FileMessage(fileName, Files.readAllBytes(Paths.get(fileName)));
                    oeos.writeObject(file);
                    oeos.flush();
                    answer = (ServerCallbackMessage) odis.readObject();
                    System.out.println(answer.getStatus().name());
                    System.out.flush();
                    fileName = "C:\\Users\\Dmitrii\\Desktop\\Счёт оплата контактов.pdf";
                    file = new FileMessage("Счёт оплата контактов.pdf", Files.readAllBytes(Paths.get(fileName)));
                    oeos.writeObject(file);
                    oeos.flush();
                    answer = (ServerCallbackMessage) odis.readObject();
                    System.out.println(answer.getStatus().name());
                    System.out.flush();*/
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
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).run();
        //}
        /*ObjectEncoderOutputStream oeos = null;
        ObjectDecoderInputStream odis = null;
        try {
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Connected");
            System.out.flush();
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            AuthMessage authMessage = new AuthMessage("User3", "12345", false);
            oeos.writeObject(authMessage);
            oeos.flush();
            ServerCallbackMessage answer = (ServerCallbackMessage) odis.readObject();
            System.out.println(answer.getStatus().name());
            System.out.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FileMessage file = new FileMessage("Example.txt", Files.readAllBytes(Paths.get("Example.txt")));
            oeos.writeObject(file);
            oeos.flush();
            answer = (ServerCallbackMessage) odis.readObject();
            System.out.println(answer.getStatus().name());
            System.out.flush();*/
            /*oeos.writeObject(new CmdMessage("Ping"));
            oeos.flush();
            System.out.println(((ServerCallbackMessage) odis.readObject()).getStatus());
            System.out.flush();*/
        /*} catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                oeos.close();
                odis.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    public static void main(String[] args) {
        run();
    }
}
