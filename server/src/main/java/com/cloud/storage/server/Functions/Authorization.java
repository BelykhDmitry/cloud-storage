package com.cloud.storage.server.Functions;

import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.ServerCallbackMessage;

public class Authorization {
    private static volatile Authorization instance;

    public static Authorization getInstance() {
        Authorization localInstance = instance;
        if (localInstance == null) {
            synchronized (Authorization.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Authorization();
                }
            }
        }
        return localInstance;
    }

    public boolean authorize (AuthMessage msg) {
        System.out.println(msg.getName());
        return true; // TODO: Add DB check
    }
}
