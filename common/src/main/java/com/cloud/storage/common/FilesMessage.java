package com.cloud.storage.common;

        import javafx.scene.control.TreeItem;
        import jdk.internal.util.xml.XMLStreamException;
        import jdk.internal.util.xml.impl.XMLWriter;
        import sun.reflect.generics.tree.Tree;

        import java.beans.XMLDecoder;
        import java.beans.XMLEncoder;
        import java.io.File;
        import java.util.ArrayList;
        import java.util.List;

public class FilesMessage extends AbstractMessage {
    private String XML;

    public FilesMessage(String XML) {
        this.XML = XML;
    }

    public String getXML() {
        return XML;
    }
}
