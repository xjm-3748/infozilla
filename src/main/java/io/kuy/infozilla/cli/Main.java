package io.kuy.infozilla.cli;

import io.kuy.infozilla.interact_interface.InteractClass;
import io.kuy.infozilla.interact_interface.InteractInterface;

import java.util.ArrayList;

/**
 * Main
 */
public class Main{

  public static void main(String[] args) {
    String bugReportPath = "data/bugReports/report1.txt";
//    String bugReportPath = "demo/demo0001.txt";
    String projectPath = "data/youTrust-master";
    InteractInterface interact = new InteractClass();
    ArrayList<String> topTenList = interact.getSimilarTopTen(bugReportPath, projectPath);
    for(String item : topTenList) {
      System.out.println(item);
    }

//    InteractInterface interact = new InteractClass();
//    String coloredReport = interact.getColoredReport("data/bugReports/report1.txt");
//    System.out.println(coloredReport);
  }
}