package io.kuy.infozilla.interact_interface;

import io.kuy.infozilla.entities.BugRepository;

import java.util.ArrayList;

public interface InteractInterface {
    //整体方法，覆盖全流程，返回的是源文件的10个文件路径
    ArrayList<String> getSimilarTopTen(String bugReportPath, String projectPath);

    //提供一个整体的方法，传入一个BugRepository, 返回这个BugRepository的拷贝，填入所有相似度前十的文件
    BugRepository getSimilarForRepository(String projectPath, BugRepository source);

    //得到染色后的bugReport, 如果传入的路径
    String getColoredReport(String bugReportPath);

    //使用BLUiR的整体方法返回10个文件路径
    ArrayList<String> getSimilarTopTenUsingBLUiR(String bugReportPath, String projectPath);

    //TODO 根据源文件路径得到染色后文本的方法，感觉这个方法必要性不是很大
}
