package com.cloud.storage.common;

public class AuthMessage extends AbstractMessage {
    // Сообщение с информацией об авторизации.
    // TODO: Продумать механизм защиты
    private String name;
    private String pass;
    private boolean isRegistration;

    public AuthMessage(String name, String pass, boolean isRegistration) {
        this.name = name;
        this.pass = pass;
        this.isRegistration = isRegistration;
    }

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public boolean isRegistration() {
        return isRegistration;
    }
}
