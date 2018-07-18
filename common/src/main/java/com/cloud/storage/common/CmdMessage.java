package com.cloud.storage.common;

public class CmdMessage extends AbstractMessage {
    private String cmd;

    public CmdMessage(String cmd) {
        this.cmd = cmd;
    }

    public String getCmd() {
        return cmd;
    }

    // TODO: Add Folders (TreeItem)
    // Functions: 1. File Download? Maybe -> FileMassage
}
