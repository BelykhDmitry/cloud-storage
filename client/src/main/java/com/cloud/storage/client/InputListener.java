package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;

public interface InputListener {
    <T extends AbstractMessage> void onMsgReceived(T msg);
}
