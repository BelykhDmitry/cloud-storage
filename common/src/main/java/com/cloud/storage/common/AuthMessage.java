package com.cloud.storage.common;

public class AuthMessage extends AbstractMessage {
    // Сообщение с информацией об авторизации.
    // TODO: Продумать механизм защиты
    private String name;
    private int pass;
    private boolean isRegistration;

    public AuthMessage(String name, String pass, boolean isRegistration) {
        this.name = name;
        this.pass = pass.hashCode();
        this.isRegistration = isRegistration;
    }

    public String getName() {
        return name;
    }

    public int getPass() {
        return pass;
    }

    public boolean isRegistration() {
        return isRegistration;
    }
}
