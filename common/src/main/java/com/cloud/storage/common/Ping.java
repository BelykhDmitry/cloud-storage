package com.cloud.storage.common;

public class Ping extends AbstractMessage {
    final byte FF = 0xF;

    public byte getFF() {
        return FF;
    }
}
