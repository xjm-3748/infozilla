//package io.kuy.infozilla.test;
//
//import io.kuy.infozilla.interact_interface.InteractClass;
//import io.kuy.infozilla.interact_interface.InteractInterface;
//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.api.ResetCommand;
//import org.eclipse.jgit.lib.Repository;
//import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
//
//import java.io.*;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//// 利用7个数据库对算法效果进行测试，测试难点：连接和操作sqlite数据库，java对git的操作，最后结果的统计
//public class TestMain {
//
//    public static void main(String[] args) {
//        try{
////            FactExtractor.extractEclipseFacts("C:/Users/82646/Documents/git repository/vnv_hw/final_proj/ExampleProject/swt/src/org", "C:/Users/82646/Documents/git repository/vnv_hw/final_proj/testRunOut/docs");
////            InteractInterface interact = new InteractClass();
////            interact.getSimilarTopTenUsingBLUiR("data/bugReports/testReport1.txt", "data/youTrust-master");
//            TestMain testMain = new TestMain();
//            ArrayList<Double> results = testMain.testProject("jdbc:sqlite:C:\\SQLite\\maven.sqlite3", "C:\\Users\\82646\\Documents\\git repository\\maven");
//            for(double result : results) {
//                System.out.println(result);
//            }
//        }catch (Exception ex) {ex.printStackTrace();}
//    }
//
//    public void pretestDataset(){
//        TestMain testMain = new TestMain();
//        String mavenDBPath = "jdbc:sqlite:C:\\SQLite\\maven.sqlite3";
//        String mavenProjectPath = "C:\\Users\\82646\\Documents\\git repository\\maven";
//        Set<String> resultList1 = testMain.testcaseNum(mavenDBPath);
//        testMain.testProject(mavenDBPath, "");
//        System.out.println("Maven: " + resultList1.size());
//        String seam2DBPath = "jdbc:sqlite:C:\\SQLite\\seam2.sqlite3";
//        Set<String> resultList2 = testMain.testcaseNum(seam2DBPath);
//        testMain.testProject(seam2DBPath, "");
//        System.out.println("Seam2: " + resultList2.size());
//        String pigDBPath = "jdbc:sqlite:C:\\SQLite\\pig.sqlite3";
//        Set<String> resultList3 = testMain.testcaseNum(pigDBPath);
//        testMain.testProject(pigDBPath, "");
//        System.out.println("Pig: " + resultList3.size());
//        String infinispanDBPath = "jdbc:sqlite:C:\\SQLite\\infinispan.sqlite3";
//        Set<String> resultList4 = testMain.testcaseNum(infinispanDBPath);
//        testMain.testProject(infinispanDBPath, "");
//        System.out.println("Infinispan: " + resultList4.size());
//        String derbyDBPath = "jdbc:sqlite:C:\\SQLite\\derby.sqlite3";
//        Set<String> resultList5 = testMain.testcaseNum(derbyDBPath);
//        testMain.testProject(derbyDBPath, "");
//        System.out.println("Derby: " + resultList5.size());
//        String groovyDBPath = "jdbc:sqlite:C:\\SQLite\\groovy.sqlite3";
//        Set<String> resultList6 = testMain.testcaseNum(groovyDBPath);
//        testMain.testProject(groovyDBPath, "");
//        System.out.println("Groovy: " + resultList6.size());
//        String droolsDBPath = "jdbc:sqlite:C:\\SQLite\\drools.sqlite3";
//        Set<String> resultList7 = testMain.testcaseNum(droolsDBPath);
//        testMain.testProject(droolsDBPath, "");
//        System.out.println("Drools: " + resultList7.size());
//    }
//
//    //对bugReport的读取正确
//    public void testBugReportTrim() {
//        TestMain testMain = new TestMain();
//        String testString = "";
//        System.out.println(trimBugReport(testString));
//    }
//
//    public ArrayList<Double> testProject(String dbPath, String projectPath) {
//        //先连接数据库
//        Connection conn = null;
//        Statement statement = null;
//        ResultSet rs = null;
//        ArrayList<IssueAndChangeSet> issues = new ArrayList<>();
//
//        try{
//            Class.forName("org.sqlite.JDBC");
//            conn = DriverManager.getConnection(dbPath);
//            statement = conn.createStatement();
//            String sql = "select issue.issue_id, issue.summary, issue.description, change_set_link.commit_hash from issue inner join change_set_link on issue.issue_id = change_set_link.issue_id where issue.description is not null and issue.type='Bug' and (issue.status='Closed' or status='Resolved') and (issue.resolution='Done' or issue.resolution='Fixed') order by issue.issue_id;";
//            rs = statement.executeQuery(sql);
//            while(rs.next()) {
//                String issueId = rs.getString(1);
//                String bugReportDescription = rs.getString(2) +"\r\n" + rs.getString(3);
//                String commit_hash = rs.getString(4);
//                //因为结果是升序排列，所以如果重复只会跟最后一个相同
//                if(!issues.isEmpty() && issueId.equals(issues.get(issues.size()-1).getIssue_id())) {
//                    issues.get(issues.size()-1).getCommit_hash().add(commit_hash);
//                }else {
//                    bugReportDescription = this.trimBugReport(bugReportDescription);
//                    ArrayList<String> commit_hashes = new ArrayList<>();
//                    commit_hashes.add(commit_hash);
//                    IssueAndChangeSet item = new IssueAndChangeSet(issueId, bugReportDescription, commit_hashes);
//                    issues.add(item);
//                }
//            }
//
//            Map<String, ArrayList<String>> hashFilePath = new HashMap<>();
//            statement = conn.createStatement();
//            String queryCodeChange = "select commit_hash, file_path from code_change;";
//            rs = statement.executeQuery(queryCodeChange);
//            while(rs.next()) {
//                String commitHash = rs.getString(1);
//                String filePath = rs.getString(2);
//                ArrayList<String> fileList = hashFilePath.get(commitHash);
//                if(fileList == null) {
//                    fileList = new ArrayList<>();
//                }
//                File file = new File(filePath.trim());
//                filePath = file.getName();  //只取文件名，不取路径
//                if(filePath.length() > 5 && filePath.substring(filePath.length()-5).equals(".java")) {  //只取文件后缀为.java的修改
//                    fileList.add(filePath);
//                }
//                hashFilePath.put(commitHash, fileList);
//            }
//
//            Iterator<IssueAndChangeSet> iter = issues.iterator();
//            while(iter.hasNext()) {
//                IssueAndChangeSet item = iter.next();
//                ArrayList<String> hashes = item.getCommit_hash();
//                ArrayList<String> itemChangeSet = new ArrayList<>();
//                for(String hash : hashes) {
//                    ArrayList<String> filePath = hashFilePath.get(hash);
//                    if(filePath != null && filePath.size() > 0) {
//                        itemChangeSet.addAll(filePath);
//                    }
//                }
//
//                if(itemChangeSet.size() > 0){
//                    item.setChange_list(itemChangeSet);
//                }
//                else {
//                    iter.remove();
//                }
//            }
//
//            int top1Count = 0;
//            int top5Count = 0;
//            int top10Count = 0;
//            double totalAvgPrecision = 0;
//            double totalReciprocalRank = 0;
//            iter = issues.iterator();
//            int now_count = 0;
//            for(int k = 0; k < 50; k++) {  //在这里修改代码分批次跑，一批跑500个issue
//                now_count++;
//                IssueAndChangeSet item = issues.get(k);
//                //TODO 操作仓库并进行统计，将版本恢复到Item所指的任意一个版本都行
//                String versionThatTime = item.getCommit_hash().get(0);
//                //利用JGit API操作仓库
//                Repository projectRepo;
//                FileRepositoryBuilder builder = new FileRepositoryBuilder();
//                try{
//                    projectRepo = builder.setGitDir(new File(projectPath+"\\.git"))
//                        .readEnvironment()
//                        .findGitDir()
//                        .build();
//                    Git git = new Git(projectRepo);
//                    git.reset().setMode(ResetCommand.ResetType.HARD).setRef(versionThatTime).call();
//                }catch(Exception ex) {
//                    ex.printStackTrace();
//                    now_count--;
//                    continue;
//                }
//
//                InteractInterface interact = new InteractClass();
//                //将bugReport读入data/bugReports文件夹
//                File f = new File("data/bugReports/" + item.getIssue_id() + ".txt");
//                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getAbsolutePath())));
//                try{
//                    out.write(item.getBug_report_content());
//                    out.close();
//                }catch (Exception ex) {ex.printStackTrace();}
//
//                ArrayList<String> topTenList = interact.getSimilarTopTenUsingBLUiR("data/bugReports/" + item.getIssue_id() + ".txt", projectPath);
////                ArrayList<String> topTenList = interact.getSimilarTopTen("data/bugReports/" + item.getIssue_id() + ".txt", projectPath);
//                //先解决三个top的统计
//                int firstFoundIndex = -1;
//                double found_by_k = 0;
//                if(topTenList == null) {
//                    now_count--;
//                    continue;
//                }
//                int tenOrSize = topTenList.size() >= 10 ? 10 : topTenList.size();
//
//                for(int i = 0; i < tenOrSize; i++) {
//                    File file = new File(topTenList.get(i));
//                    String fileName = file.getName();
//                    if(item.getChange_list().contains(fileName)) {
//                        found_by_k = found_by_k + 1.0;
//                        if(firstFoundIndex < 0) {
//                            firstFoundIndex = i+1;
//                        }
//                        //在这里计算MAP,只用前十个文件算MAP
//                        totalAvgPrecision += ((found_by_k/(i+1.0)) / item.getChange_list().size());
//                    }
//                }
//
//                if(firstFoundIndex >= 0) {
//                    totalReciprocalRank += 1.0/firstFoundIndex;
//                    if(firstFoundIndex == 1) {
//                        top1Count++;
//                        top5Count++;
//                        top10Count++;
//                    } else if(firstFoundIndex < 5) {
//                        top5Count++;
//                        top10Count++;
//                    } else {
//                        top10Count++;
//                    }
//                } else {
//                    totalReciprocalRank += 1.0/20;
//                }
//
//                for(int i = 10; i < topTenList.size(); i++) {
//                    File file = new File(topTenList.get(i));
//                    String fileName = file.getName();
//                    if(item.getChange_list().contains(fileName)) {
//                        if(firstFoundIndex < 0) {
//                            totalReciprocalRank += 1.0/(i+1.0);
//                            firstFoundIndex = i+1;
//                        }
//                        found_by_k += 1.0;
//                        totalAvgPrecision += ((found_by_k/(i+1.0)) / item.getChange_list().size());
//                    }
//                }
//
//                if(now_count % 20 == 0) {
//                    File deleteFile = new File("data/bugReports");
//                    deleteFile(deleteFile);
//                }
//            }
//            double top1Percent = (top1Count+0.0)/now_count;
//            double top5Percent = (top5Count+0.0)/now_count;
//            double top10Percent = (top10Count+0.0)/now_count;
//            double MAP = totalAvgPrecision/now_count;
//            double MRR = totalReciprocalRank/now_count;
//            System.out.println(now_count);
//            ArrayList<Double> result = new ArrayList<>(Arrays.asList(top1Percent, top5Percent, top10Percent, MAP, MRR));
//            return result;
//        }catch (Exception ex) {
//            ex.printStackTrace();}
//
//        return null;
//    }
//
//    //统计issue的数量
//    public Set<String> testcaseNum(String dbPath) {
//        //先连接数据库
//        Connection conn = null;
//        Statement statement = null;
//        ResultSet rs = null;
//        ArrayList<IssueAndChangeSet> issues = new ArrayList<>();
//        Set<String> results = new HashSet<>();
//
//        try{
//            Class.forName("org.sqlite.JDBC");
//            conn = DriverManager.getConnection(dbPath);
//            statement = conn.createStatement();
//            String sql = "select issue.issue_id, issue.description, change_set_link.commit_hash from issue inner join change_set_link on issue.issue_id = change_set_link.issue_id where issue.description is not null and issue.type='Bug' and (issue.status='Closed' or status='Resolved') and (issue.resolution='Done' or issue.resolution='Fixed') order by issue.issue_id;";  //这行报错不影响编译
//            rs = statement.executeQuery(sql);
//            while(rs.next()) {
//                String issue_id = rs.getString(1);  //从1开始
//                results.add(issue_id);
//            }
//        }catch (Exception ex) {ex.printStackTrace();}
//
//        return results;
//    }
//
//    public String trimBugReport(String reportDescription) {
//        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
//        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
//        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式
//
//        Pattern p_script= Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
//        Matcher m_script=p_script.matcher(reportDescription);
//        reportDescription=m_script.replaceAll(""); //过滤script标签
//
//        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
//        Matcher m_style=p_style.matcher(reportDescription);
//        reportDescription=m_style.replaceAll(""); //过滤style标签
//
//        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
//        Matcher m_html=p_html.matcher(reportDescription);
//        reportDescription=m_html.replaceAll(""); //过滤html标签
//        reportDescription=reportDescription.replaceAll("&lt;", "<");
//        reportDescription=reportDescription.replaceAll("&gt;", ">");
//
//        return reportDescription.trim(); //返回文本字符串
//    }
//
//    private void deleteFile(File file){
//        if (file.isFile()){//判断是否为文件，是，则删除
//            file.delete();
//        }else{//不为文件，则为文件夹
//            String[] childFilePath = file.list();//获取文件夹下所有文件相对路径
//            for (String path:childFilePath) {
//                File childFile = new File(file.getAbsoluteFile() + "/" + path);
//                deleteFile(childFile);//递归，对每个都进行判断
//            }
//        }
//    }
//
//}
