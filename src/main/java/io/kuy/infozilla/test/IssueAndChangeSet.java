package io.kuy.infozilla.test;

import java.util.ArrayList;
import java.util.Objects;

//对于没有commit_hash的issue进行忽略
public class IssueAndChangeSet {
    private String issue_id;
    private String bug_report_content;
    //当一个issue对应多个commit_hash的时候找出所有的commit_hash
    private ArrayList<String> commit_hash = new ArrayList<>();
    private ArrayList<String> change_list = new ArrayList<>();  //所改变的文件名，只包含文件名，不包含路径名

    public IssueAndChangeSet() {
        super();
    }

    public IssueAndChangeSet(String issue_id, String bug_report_content, ArrayList<String> commit_hash) {
        this.issue_id = issue_id;
        this.bug_report_content = bug_report_content;
        this.commit_hash = commit_hash;
    }

    public String getIssue_id() {
        return issue_id;
    }

    public void setIssue_id(String issue_id) {
        this.issue_id = issue_id;
    }

    public String getBug_report_content() {
        return bug_report_content;
    }

    public void setBug_report_content(String bug_report_content) {
        this.bug_report_content = bug_report_content;
    }

    public ArrayList<String> getCommit_hash() {
        return commit_hash;
    }

    public void setCommit_hash(ArrayList<String> commit_hash) {
        this.commit_hash = commit_hash;
    }

    public ArrayList<String> getChange_list() {
        return change_list;
    }

    public void setChange_list(ArrayList<String> change_list) {
        this.change_list = change_list;
    }

    //判断两个issue是否相同只需要使用issue_id来判断
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IssueAndChangeSet)) return false;
        IssueAndChangeSet that = (IssueAndChangeSet) o;
        return issue_id.equals(that.issue_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issue_id);
    }
}
