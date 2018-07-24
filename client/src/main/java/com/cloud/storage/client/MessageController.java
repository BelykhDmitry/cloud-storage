package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.FilesMessage;
import com.cloud.storage.common.ServerCallbackMessage;

import java.util.function.Consumer;

public class MessageController implements InputListener{

    @Override
    public <T extends AbstractMessage> void onMsgReceived(T msg) {
        // Methods from Controller
        if (msg instanceof ServerCallbackMessage) {
            System.out.println("New Callback:");
            System.out.println(((ServerCallbackMessage)msg).getStatus());
        } else if (msg instanceof FileMessage) {
            System.out.println("New File msg:");
            System.out.println(((FileMessage)msg).getFileRelativePathName() + " received");
        } else if (msg instanceof FilesMessage) {
            System.out.println("New Files List:");
            ((FilesMessage)msg).getList().forEach(fileMessage -> System.out.println(fileMessage.getFileRelativePathName() + " " + fileMessage.getSize()/1024 + " kB"));
        } else {
            System.out.println("Неопознанный тип сообщения");
        }
    }
}
