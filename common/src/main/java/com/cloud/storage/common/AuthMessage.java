package com.cloud.storage.common;

public class AuthMessage extends AbstractMessage {
    private String name;

    public AuthMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
