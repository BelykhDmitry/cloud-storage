package com.cloud.storage.client;

import com.sun.istack.internal.NotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collector;

public class PropertiesLoader {

    private static volatile PropertiesLoader instance = new PropertiesLoader();

    public final String PATH = "prop.dtd";
    private Properties prop;
    private boolean status = false;

    public static PropertiesLoader getInstance() {return instance;}

    private PropertiesLoader() {
        prop = new Properties();
    }

    public void load(String path) throws IOException {
        status = false;
        prop.load(new FileInputStream(path));
        status = checkProperties(Arrays.asList("host","port")); //FIXME
        System.err.println("Properties status: " + status);
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

    public boolean getStatus() {
        return status;
    }

    public boolean checkProperties(List<String> keys) {
        return keys.stream().map(s -> getProperty(s) != null).reduce(true, (a, b) -> a && b);
    }
}
