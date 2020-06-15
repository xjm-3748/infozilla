package io.kuy.infozilla.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(value = XmlAccessType.FIELD)
public class Bug {
    @XmlAttribute(name = "id")
    public String id;

    private String openDate;
    private String fixDate;

    @XmlElement(name = "buginformation")
    public BugInformation information;

    @XmlElement(name = "fixedFiles")
    public List<FixedFile> fixedFiles;

    //相似度排名前十的files
    public List<FixedFile> guessFixedFiles = new ArrayList<>();

    public Bug(String id, BugInformation information) {
        this.id = id;
        this.information = information;
    }

    public Bug(String id, String openDate, String fixDate, BugInformation information) {
        this.id = id;
        this.openDate = openDate;
        this.fixDate = fixDate;
        this.information = information;
    }

    public Bug() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getFixDate() {
        return fixDate;
    }

    public void setFixDate(String fixDate) {
        this.fixDate = fixDate;
    }

    public void setInformation(BugInformation information) {
        this.information = information;
    }

    public void setFixedFiles(List<FixedFile> fixedFiles) {
        this.fixedFiles = fixedFiles;
    }

    public void setDescription(String description) {
        this.information.setDescription(description);
    }
}
