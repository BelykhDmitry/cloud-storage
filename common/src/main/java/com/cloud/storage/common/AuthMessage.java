package com.cloud.storage.common;

public class AuthMessage extends AbstractMessage {
    // Сообщение с информацией об авторизации.
    // TODO: Продумать механизм защиты
    private String name;
    private String pass;

    public AuthMessage(String name, String pass) {
        this.name = name;
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }
}
