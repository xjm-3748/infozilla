package io.kuy.infozilla.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "bugrepository")
public class BugRepository {
    @XmlAttribute(name = "name")
    public String name;

    @XmlElement(name = "bug")
    public List<Bug> bugs;

    public BugRepository(String name, List<Bug> bugs) {
        this.name = name;
        this.bugs = bugs;
    }

    public BugRepository(String name) {
        this.name = name;
    }

    public BugRepository() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBugs(List<Bug> bugs) {
        this.bugs = bugs;
    }

    public int findBug(String id) {
        for(int i = 0; i < bugs.size(); i++) {
            if(bugs.get(i).id.equals(id)) {
                return i;
            }
        }

        return -1;
    }
}
