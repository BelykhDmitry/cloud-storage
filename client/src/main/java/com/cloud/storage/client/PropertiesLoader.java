package com.cloud.storage.client;

import com.sun.istack.internal.NotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {

    private static volatile PropertiesLoader instance = new PropertiesLoader();

    public final String PATH = "/prop.dtd";

    private Properties prop;

    public static PropertiesLoader getInstance() {return instance;}

    private PropertiesLoader() {
        prop = new Properties();
    }

    public void load(String path) throws IOException {
        prop.load(new FileInputStream(path));
    }

    public void store(String path) throws IOException {
        prop.store(new FileOutputStream(path),"Properties");
    }

    @NotNull
    public String getProperty(String key) {
        return prop.getProperty(key);
    }

    @NotNull
    public void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }
}
