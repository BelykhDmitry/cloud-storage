package com.cloud.storage.common;

public class ServerCallbackMessage extends AbstractMessage {

    // Ответ на:
    // * Авторизацию
    // * Регистрацию
    // * Приём файла
    // * Удаление, добавление директории

    public enum Answer {
        OK,
        FAIL,
        DISCONNECTED
    }
    private Answer answer;

    public ServerCallbackMessage(Answer answer) {
        this.answer = answer;
    }

    public Answer getStatus() {
        return this.answer;
    }
}
