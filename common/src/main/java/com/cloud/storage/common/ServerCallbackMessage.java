package com.cloud.storage.common;

public class ServerCallbackMessage extends AbstractMessage {
    public enum Answer {
        OK,
        FAIL
    }
    private Answer answer;

    public ServerCallbackMessage(Answer answer) {
        this.answer = answer;
    }

    public Answer getStatus() {
        return this.answer;
    }
}
