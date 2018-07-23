package simple.client;

import com.cloud.storage.common.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

public class Main {

    private static String rootFolder = "C:\\Users\\Dmitrii\\Cloud\\";

    private static void run() {
//        try {
//            getFiles("User10", "");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //for (int i = 0; i < 5; i++) {
        client();
        //System.out.println(Paths.get("C:\\Users\\Dmitrii\\Cloud\\User0\\Example.txt").getParent().toString());
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

    private static FilesMessage getFiles(String user, String filePath) throws IOException {
        Path p = Paths.get(rootFolder + user + "\\" + filePath);
        FilesMessage msg = new FilesMessage();
        Files.walkFileTree(p, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if(dir.compareTo(p) != 0) {
                    msg.addToList(p.relativize(dir).toString(), attrs.isDirectory(), null, attrs.size());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                msg.addToList(p.relativize(file).toString(), attrs.isDirectory(), null, attrs.size());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return msg;
    }

    private static void client() {
        int j = 1;
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
                /*oeos.writeObject(new CmdMessage("", CmdMessage.CmdType.GET_PATHS_LIST));
                oeos.flush();
                Object obj = odis.readObject();
                if(obj instanceof FilesMessage)
                    ((FilesMessage) obj).getList().forEach(fileMessage -> System.out.println(fileMessage.getFileRelativePathName()));*/
//                    oeos.writeObject(new CmdMessage("Задача1.png", CmdMessage.CmdType.REMOVE_FILE));
//                    oeos.flush();
//                    System.out.println(answer.getStatus().name());
//                    System.out.flush();
                String fileName = "Задача1.png";
                FileMessage file = new FileMessage(fileName, false, Files.readAllBytes(Paths.get(fileName)), Paths.get(fileName).toFile().length());
                oeos.writeObject(file);
                oeos.flush();
                answer = (ServerCallbackMessage) odis.readObject();
                System.out.println(answer.getStatus().name());
                System.out.flush();
                /*fileName = "C:\\Users\\Dmitrii\\Desktop\\Счёт оплата контактов.pdf";
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
    }

    public static void main(String[] args) {
        run();
    }
}
