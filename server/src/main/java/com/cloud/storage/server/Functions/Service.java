package com.cloud.storage.server.Functions;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Service {

    private enum CmdType {
        ABORT,
        SOMETHING
    }

    private static Logger log = Logger.getLogger(Service.class.getName());
    public static void runService (String[] args) {
        log.info("In service");
        int login = "admin".hashCode();
        int pass = "12345".hashCode();
        try {
            if (args[0].hashCode() == login && pass == args[1].hashCode()) {
                log.info("Login success");
                process();
            } else {
                log.info("Login error! Abort");
            }
        } catch (NullPointerException e) {
            log.log(Level.SEVERE, "Exception: ", e);
        }
    }

    private static void process() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        CmdType type = CmdType.SOMETHING;
        String command;
        try {
            while(type != CmdType.ABORT) {
                command = reader.readLine();
                type = getCmdType(command);
                log.info("New command: " + type);
            }
        } catch (IOException | NullPointerException e) {
            log.log(Level.SEVERE,"Exception: ", e);
        }
    }

    private static CmdType getCmdType(String cmd) {
        if(cmd.contains("exit")) {
            return CmdType.ABORT;
        } else return CmdType.SOMETHING;
    }
}
