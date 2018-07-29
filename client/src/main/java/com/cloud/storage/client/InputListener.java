package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;

public interface InputListener {
    void onMsgReceived(AbstractMessage msg);
}
