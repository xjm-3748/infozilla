package io.kuy.infozilla.entities;

import javax.xml.bind.annotation.XmlElement;

public class FixedFile {
    @XmlElement(name = "file")
    public String fileName;

    public FixedFile(String fileName) {
        this.fileName = fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
