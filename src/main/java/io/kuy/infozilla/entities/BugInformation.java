package io.kuy.infozilla.entities;

import javax.xml.bind.annotation.XmlElement;

public class BugInformation {
    @XmlElement(name = "summary")
    public String summary;
    @XmlElement(name = "description")
    public String description;

    public BugInformation(String summary, String description) {
        this.summary = summary;
        this.description = description;
    }

    public BugInformation(String description) {
        this.description = description;
    }

    public BugInformation() {
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
