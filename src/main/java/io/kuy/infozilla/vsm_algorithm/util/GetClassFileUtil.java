package io.kuy.infozilla.vsm_algorithm.util;

import java.io.*;

public class GetClassFileUtil {
    public static void getClassFromProject(String originPath,String targetPath) throws IOException {
        File dir = new File(originPath);
        if(dir.isDirectory()) {
            System.out.println("direct name: "+dir.getName());
            if(!dir.getName().equals("test")) {
                File[] childs = dir.listFiles();
                System.out.println("num of child files: "+childs.length);
                for(File child:childs) {
                    getClassFromProject(child.getAbsolutePath(), targetPath);
                }
            }
        }
        else {
            String fileName = dir.getName();
            if(fileName.endsWith(".java")) {
                System.out.println("java file name: "+fileName);
                String className = fileName.substring(0,fileName.lastIndexOf("."));
                writeFile(dir,targetPath);
            }
        }
    }

    private static void writeFile(File javaFile, String targetPath) throws IOException {
        String name = javaFile.getName();
        BufferedReader br = new BufferedReader(new FileReader(javaFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(targetPath+File.separator+name));

        String line;
        while((line=br.readLine())!=null) {
            bw.write(line);
            bw.newLine();
        }
        br.close();
        bw.close();
    }
}
