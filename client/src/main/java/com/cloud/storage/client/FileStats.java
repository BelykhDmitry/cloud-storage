package com.cloud.storage.client;

import javafx.beans.property.*;

public class FileStats {
    private BooleanProperty isDirectory;
    private StringProperty fileRelativePathName;
    private StringProperty size;
    private LongProperty longSize;

    public FileStats(String fileRelativePathName, boolean isDirectory, String size) {
        this.fileRelativePathName = new SimpleStringProperty(fileRelativePathName);
        this.isDirectory = new SimpleBooleanProperty(isDirectory);
        this.size = new SimpleStringProperty(size);
        this.longSize = new SimpleLongProperty(Long.parseLong(size));
    }

    public StringProperty getRelativeNameProperty() {return fileRelativePathName;}
    public boolean isDirectory() {return isDirectory.get();}
    public StringProperty getSizeProperty() {return size;}
    public LongProperty getLongSizeProperty() {return longSize;}
}
