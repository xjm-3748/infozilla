package io.kuy.infozilla.vsm_algorithm.preprocess;

import io.kuy.infozilla.vsm_algorithm.util.GetClassFileUtil;
import io.kuy.infozilla.vsm_algorithm.util.TransferToTxtUtil;

import java.io.File;

/* 在调用arrangeData前先修改projectPath和ucPath
* */
public class DataPreprecess {


    private static String projectPath = "data/iTrust-master";
    private static String ucPath = "data/uc_origin";

    private static String classPath = "result/class";
    private static String classTxtPath =  "result/class_txt";
    private static String classProcessedPath =  "result/class_preprocessed";
    private static String ucPreProcessedPath =  "result/uc_preprocessed";




    private TransferToTxtUtil getSrcTXT = new TransferToTxtUtil();

    public static String getClassProcessedPath() {
        return classProcessedPath;
    }

    public static void setClassProcessedPath(String classProcessedPath) {
        DataPreprecess.classProcessedPath = classProcessedPath;
    }

    public static String getProjectPath() {
        return projectPath;
    }

    public static void setProjectPath(String projectPath) {
        DataPreprecess.projectPath = projectPath;
    }

    public static String getUcPath() {
        return ucPath;
    }

    public static void setUcPath(String ucPath) {
        DataPreprecess.ucPath = ucPath;
    }

    public static String getClassPath() {
        return classPath;
    }

    public static void setClassPath(String classPath) {
        DataPreprecess.classPath = classPath;
    }

    public static String getClassTxtPath() {
        return classTxtPath;
    }

    public static void setClassTxtPath(String classTxtPath) {
        DataPreprecess.classTxtPath = classTxtPath;
    }

    public static String getUcPreProcessedPath() {
        return ucPreProcessedPath;
    }

    public static void setUcPreProcessedPath(String ucPreProcessedPath) {
        DataPreprecess.ucPreProcessedPath = ucPreProcessedPath;
    }

    private void cleanData() {
        deleteFileInDir(classPath);
        deleteFileInDir(classTxtPath);
        deleteFileInDir(classProcessedPath);
        deleteFileInDir(ucPreProcessedPath);
    }

    private void deleteFileInDir(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        } else {
            for (File f : files) {
                f.delete();
            }
        }
    }

    public void arrangeData() {
        try {
            cleanData();
            GetClassFileUtil.getClassFromProject(projectPath, classPath);  //对文件夹进行遍历搜索并提取java文件复制到classPath中
            getSrcTXT.transferTXT(classPath, classTxtPath);
            BatchingPreprocess preprocess = new BatchingPreprocess(ucPath, classTxtPath, classProcessedPath, ucPreProcessedPath);
            preprocess.doProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        DataPreprecess dataProcess = new DataPreprecess();
        dataProcess.arrangeData();
        long endTime = System.currentTimeMillis();
        System.out.println("time cost:" + (endTime - startTime) * 1.0 / 1000 / 60);
    }

}
