package io.kuy.infozilla.interact_interface;

import io.kuy.infozilla.entities.Bug;
import io.kuy.infozilla.entities.BugInformation;
import io.kuy.infozilla.entities.BugRepository;
import io.kuy.infozilla.entities.FixedFile;
import io.kuy.infozilla.filters.FilterChainEclipse;
import io.kuy.infozilla.helpers.ASTNodeVisitorHelper;
import io.kuy.infozilla.helpers.DataExportUtility;
import io.kuy.infozilla.vsm_algorithm.document.LinksList;
import io.kuy.infozilla.vsm_algorithm.document.SingleLink;
import io.kuy.infozilla.vsm_algorithm.document.TextDataset;
import io.kuy.infozilla.vsm_algorithm.ir.IR;
import io.kuy.infozilla.vsm_algorithm.preprocess.DataPreprecess;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

public class InteractClass implements InteractInterface{

    //用于运行信息分类任务
    private boolean withStackTraces = true;
    private boolean withPatches = true;
    private boolean withCode = true;
    private boolean withLists = true;
    private String inputCharset = "ISO-8859-1";

    //将读出的bugReport内容以成员形式保存
    private String codeTxt = "";
    private String stackTxt = "";
    private String naturalLanguageTxt = "";

    //bugReport三种内容的token
    private Set<String> codeToken = new HashSet<>();
    private Set<String> stackToken = new HashSet<>();
    private Set<String> naturalLanguageToken = new HashSet<>();

    @Override
    public ArrayList<String> getSimilarTopTenUsingBLUiR(String bugReportPath, String projectPath) {
        //infozilla -> 转化bugReport到xml格式 -> 接连调用BLUiR的createQuery, createDocs, index, retrieve -> 将最后结果从txt中读出并返回
        clearAndSetup();
        File bugReportFile = new File(bugReportPath);
        try{
            process(bugReportFile);
            String xmlPath = bugReportFile.getPath() + ".result.xml";
            parseXML(xmlPath);
            String bugReportName = bugReportFile.getName().substring(0, bugReportFile.getName().length()-4);

            //对reportsForVSM下的all.txt转化为xml，放到reportsForBLUiR文件夹下
            BugRepository bugRepository = transBugRepositoryEntity(bugReportFile);
            JAXBContext jaxbContext = JAXBContext.newInstance(BugRepository.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            String queryXMLPath = "data/reportsForBLUiR/" + bugReportName + ".xml";
            marshaller.marshal(bugRepository, new File(queryXMLPath));

            //将调用BLUiR做成单独方法，降低代码重复
            callBLUiR(queryXMLPath, bugReportName, projectPath);

            String resultTxtPath = "result/BLUiRRunOut/" + bugReportName + "Result.txt";
            File resultFile = new File(resultTxtPath);
            ArrayList<String> topTenList = new ArrayList<>();
            if(resultFile.isFile() && resultFile.exists()) {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(resultFile));
                BufferedReader bufferedReader = new BufferedReader(reader);
                String lineTxt = null;

                while((lineTxt = bufferedReader.readLine()) != null) {
                    String[] items = lineTxt.split(" ");
                    String[] paths = items[2].split("\\.");  //用点分割的时候要转义
                    String name = paths[paths.length-2] + "." + paths[paths.length-1];
                    String path = FindFileInProject(projectPath, name);
                    topTenList.add(path);
                }
            }

            for(String item : topTenList) {
                setUpHighLighting(item);
            }

            return topTenList;
        } catch(Exception ex) {ex.printStackTrace();}
        return null;
    }

    //这个方法仅供测试的时候使用，不会对testReport染色以及对相似度文件染色。
    @Override
    public BugRepository getSimilarForRepository(String projectPath, BugRepository source) {
        clearAndSetup();
        for(Bug bug : source.bugs) {
            String bug_txt = bug.information.description;
            String bug_path = "data/bugReports/" + source.name + ".txt";
            //用写文件的方式对每个bug写文件后process
            try{
                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bug_path), Charset.forName(inputCharset)));
                out.write(bug_txt);
                out.close();
            }catch (Exception ex) {ex.printStackTrace();}

            File file = new File(bug_path);
            try {
                process(file);
                String xmlPath = file.getPath() + ".result.xml";
                parseXML(xmlPath);
                String filePath = "data/reportsForVSM/" + file.getName().substring(0, file.getName().length()-3) + "all.txt";
                File extractedFile = new File(filePath);
                String data = new String(Files.readAllBytes(Paths.get(extractedFile.getAbsolutePath())), Charset.forName(inputCharset));
                bug.setDescription(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //JAXB合成新的BugRepository
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(BugRepository.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            String queryXMLPath = "data/reportsForBLUiR/" + source.name + ".xml";
            marshaller.marshal(source, new File(queryXMLPath));
            callBLUiR(queryXMLPath, source.name, projectPath);

            String resultTxtPath = "result/BLUiRRunOut/" + source.name + "Result.txt";
            File resultFile = new File(resultTxtPath);
            if(resultFile.isFile() && resultFile.exists()) {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(resultFile));
                BufferedReader bufferedReader = new BufferedReader(reader);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null) {
                    String[] items = lineTxt.split(" ");
                    String[] paths = items[2].split("\\.");  //用点分割的时候要转义
                    String name = paths[paths.length-2] + "." + paths[paths.length-1];
                    int index = source.findBug(items[0]);
                    if(index >= 0) {
                        source.bugs.get(index).guessFixedFiles.add(new FixedFile(name));
                    }
                }
            }

            return source;
        }catch (Exception ex) {ex.printStackTrace();}

        return null;
    }

    private void callBLUiR(String queryXMLPath, String bugReportName, String projectPath) throws Exception {
        //调用BLUiR四步
        //1. createQuery
        Runtime runtime = Runtime.getRuntime();
        String createQueryCommand = "java -jar BLUiR/BLUiR.jar -task=\"createquery\" -bugRepoLocation=\""
                + queryXMLPath + "\" -queryFilePath=\"result/BLUiRRunOut/" + bugReportName + "Query\"";
        final Process queryProcess = runtime.exec(createQueryCommand);
        printMessage(queryProcess.getInputStream());
        printMessage(queryProcess.getErrorStream());
        int exitVal = queryProcess.waitFor();
        System.out.println("Create query exited with error code " + exitVal);

        //2. createDocs
        String createDocsCommand = "java -jar BLUiR/BLUiR.jar -task=\"createdocs\" -codeLocation=\""
                + projectPath + "\" -docLocation=\"result/BLUiRRunOut/" + bugReportName + "Docs\"";
        final Process createDocsProcess = runtime.exec(createDocsCommand);
        printMessage(createDocsProcess.getErrorStream());
        printMessage(createDocsProcess.getInputStream());
        exitVal = createDocsProcess.waitFor();
        System.out.println("Create docs exited with error code " + exitVal);

        //3. index
        String indexCommand = "java -jar BLUiR/BLUiR.jar -task=\"index\" -indexLocation=\"result/BLUiRRunOut/"
                + bugReportName + "Index\"" +  " -docLocation=\"result/BLUiRRunOut/" + bugReportName + "Docs\"";
        final Process indexProcess = runtime.exec(indexCommand);
        printMessage(indexProcess.getErrorStream());
        printMessage(indexProcess.getInputStream());
        exitVal = indexProcess.waitFor();
        System.out.println("Index exited with error code " + exitVal);

        //4. retrieve
        String retrieveCommand = "java -jar BLUiR/BLUiR.jar -task=\"retrieve\" -queryFilePath=\"result/BLUiRRunOut/"
                + bugReportName + "Query\"" + " -indexLocation=\"result/BLUiRRunOut/" + bugReportName + "Index\" -resultPath=\"result/BLUiRRunOut/"
                + bugReportName + "Result.txt\"";
        final Process retrieveProcess = runtime.exec(retrieveCommand);
        printMessage(retrieveProcess.getInputStream());
        printMessage(retrieveProcess.getErrorStream());
        exitVal = retrieveProcess.waitFor();
        System.out.println("Retrieve exited with error code" + exitVal);
    }

    private void printMessage(final InputStream input) {
        new Thread(new Runnable() {
        public void run() {
            Reader reader = new InputStreamReader(input);
            BufferedReader bf = new BufferedReader(reader);
            String line = null;
            try {
                while((line=bf.readLine())!=null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }
        }).start();
    }

    @Override
    public ArrayList<String> getSimilarTopTen(String bugReportPath, String projectPath) {
        //TODO 在开启新一轮的查询前对遗留文件进行删除，包括result文件夹下的所有文件以及data/reportsForVSM下所有文件
        clearAndSetup();
        File bugReportFile = new File(bugReportPath);
        try{
            process(bugReportFile);
            String xmlPath = bugReportFile.getPath() + ".result.xml";
            parseXML(xmlPath);
            //将解析好的all.txt和整个文本库放到VSM算法中
            ArrayList<String> topTenList = computeSimilarity(projectPath, "data/reportsForVSM");

            //在这里对topTenList在project里面找到对应源文件并返回
//            for(String file : topTenList) {
//                String source = FindFileInProject(projectPath, file);
//                sourceList.add(source);
//            }

//            for(String file : topTenList) {
//                setUpHighLighting("result/class/" + file);
//            }

//            return sourceList;
            return topTenList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getColoredReport(String bugReportPath){
        File bugReportFile = new File(bugReportPath);
        String result = "";
        try {
            process(bugReportFile);
            String xmlPath = bugReportFile.getPath() + ".result.xml";
            result = parseXML(xmlPath);
        }catch(Exception ex) {ex.printStackTrace();}

        return result;
    }

    private BugRepository transBugRepositoryEntity(File bugReportFile) throws IOException {
        //对reportsForVSM下的all.txt转化为xml，放到reportsForBLUiR文件夹下
        String filePath = "data/reportsForVSM/" + bugReportFile.getName().substring(0, bugReportFile.getName().length()-3) + "all.txt";
        File extractedFile = new File(filePath);
        String data = new String(Files.readAllBytes(Paths.get(extractedFile.getAbsolutePath())), Charset.forName(inputCharset));
        BugRepository bugRepository = new BugRepository(bugReportFile.getName());
        BugInformation information = new BugInformation("null", data);  //summary属性一定不能为空
        Bug bug = new Bug(bugReportFile.getName().substring(0, bugReportFile.getName().length()-4), information);
        List<FixedFile> files = new ArrayList<>();
        files.add(new FixedFile("aa.java"));
        bug.setFixedFiles(files);  //一定要有fixedFiles属性
        List<Bug> bugList = new ArrayList<>();
        bugList.add(bug);
        bugRepository.setBugs(bugList);

        return bugRepository;
    }

    private void clearAndSetup() {
        File reportsForVSM = new File("data/reportsForVSM");
        File BLUiRRunOut = new File("result/BLUiRRunOut");
        File reportsForBLUiR = new File("data/reportsForBLUiR");
        File result = new File("result");
        deleteFile(reportsForVSM);
        deleteFile(BLUiRRunOut);
        deleteFile(reportsForBLUiR);
        deleteFile(result);
    }

    private void deleteFile(File file){
        if (file.isFile()){//判断是否为文件，是，则删除
            file.delete();
        }else{//不为文件，则为文件夹
            String[] childFilePath = file.list();//获取文件夹下所有文件相对路径
            for (String path:childFilePath) {
                File childFile = new File(file.getAbsoluteFile() + "/" + path);
                deleteFile(childFile);//递归，对每个都进行判断
            }
        }
    }

    private void process(File f) throws Exception {
        // Read file
        String data = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())), Charset.forName(inputCharset));
//        String data = Files.readString(f.toPath(), Charset.forName(inputCharset));

        // Run infozilla
        FilterChainEclipse infozilla_filters = new FilterChainEclipse(data, withPatches, withStackTraces, withCode, withLists);
        String filtered_text = infozilla_filters.getOutputText();

        System.out.println("Extracted Structural Elements from " + f.getAbsolutePath());
        System.out.println(infozilla_filters.getPatches().size() + "\t Patches");
        System.out.println(infozilla_filters.getTraces().size() + "\t Stack Traces");
        System.out.println(infozilla_filters.getRegions().size() + "\t Source Code Fragments");
        System.out.println(infozilla_filters.getEnumerations().size() + "\t Enumerations");

        System.out.println("Writing Cleaned Output");
        //TODO 写文件也用java8替代
//        Files.write(Path.of(f.getAbsolutePath() + ".cleaned"), filtered_text.getBytes(), StandardOpenOption.CREATE);
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getAbsolutePath() + ".cleaned"), Charset.forName(inputCharset)));
        try{
            out.write(filtered_text);
            out.close();
        }catch (Exception ex) {ex.printStackTrace();}
//        Files.writeString(Path.of(f.getAbsolutePath() + ".cleaned"), filtered_text, Charset.forName(inputCharset), StandardOpenOption.CREATE );

        Element rootE = new Element("infozilla-output");
        rootE.addContent(DataExportUtility.getXMLExportOfPatches(infozilla_filters.getPatches(), true));
        rootE.addContent(DataExportUtility.getXMLExportOfStackTraces(infozilla_filters.getTraces(), true, new Timestamp(new Date().getTime())));
        rootE.addContent(DataExportUtility.getXMLExportOfSourceCode(infozilla_filters.getRegions(), true));
        rootE.addContent(DataExportUtility.getXMLExportOfEnumerations(infozilla_filters.getEnumerations(), true));

        Document doc = new Document(rootE);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        String xmlDoc = outputter.outputString(doc);

        System.out.println("Writing XML Output");
        Writer out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getAbsolutePath() + ".result.xml"), Charset.forName(inputCharset)));
        try{
            out1.write(xmlDoc);
            out1.close();
        }catch (Exception ex) {ex.printStackTrace();}
//        Files.write(Path.of(f.getAbsolutePath() + ".result.xml"), xmlDoc.getBytes(), StandardOpenOption.CREATE);
//        Files.writeString(Path.of(f.getAbsolutePath() + ".result.xml"), xmlDoc, Charset.forName(inputCharset), StandardOpenOption.CREATE );
    }

    //将上步返回的xml文件进行转化，提取出stacktrace和sourcecode并创建两个新的txt
    private String parseXML(String xmlPath) throws Exception{
        String pathPrefix = xmlPath.substring(0,xmlPath.length()-14);
        SAXBuilder builder = new SAXBuilder();
        String codeResult = "";
        Document doc = builder.build(new File(xmlPath));

        Element rootElement = doc.getRootElement();

        Element stackElement = rootElement.getChild("Stacktraces");
        Attribute amountAttr = stackElement.getAttribute("amount");
        int amountStack = amountAttr.getIntValue();
        if(amountStack > 0) {  //说明有stacktrace
            StringBuilder stackBuilder = new StringBuilder();
            List<Element> stackTraces = stackElement.getChildren();
            for(int i = 0; i < stackTraces.size(); i++) {
                Element stackTrace = stackTraces.get(i);
                stackBuilder.append(stackTrace.getChild("Exception").getText());
                stackBuilder.append("\r\n");
                stackBuilder.append(stackTrace.getChild("Reason").getText());
                stackBuilder.append("\r\n");
                List<Element> frames = stackTrace.getChild("Frames").getChildren();
                for(Element frame : frames) {
                    stackBuilder.append(frame.getText());
                    stackBuilder.append("\r\n");
                }

                String stackResult = stackBuilder.toString();
                this.stackTxt = stackResult;
                //将提取出的结果写道一个txt文件中，命名规则类似于demo0001.stack.txt
                String stackName = pathPrefix + "stack.txt";
                File file = new File(stackName);
                try(OutputStream os = new FileOutputStream(file)) {
                    byte[] data = stackResult.getBytes();
                    os.write(data);
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }

        Element codeElement = rootElement.getChild("SourceCodeRegions");
        int amountCode = codeElement.getAttribute("amount").getIntValue();
        if(amountCode > 0 ) {
            StringBuilder codeBuilder = new StringBuilder();
            List<Element> codeParts = codeElement.getChildren();
            for(Element codePart : codeParts) {
                codeBuilder.append(codePart.getChild("code").getText());
                codeBuilder.append("\r\n");
            }

            codeResult = codeBuilder.toString();
            this.codeTxt = codeResult;
            String codeName = pathPrefix + "code.txt";
            File file = new File(codeName);
            try(OutputStream os = new FileOutputStream(file)) {
                byte[] data = codeResult.getBytes();
                os.write(data);
            } catch (Exception ex) {ex.printStackTrace();}
        }

        //将枚举也追加到txt.clean后面
        Element enumerationElement = rootElement.getChild("Enumerations");
        int amountEnum = enumerationElement.getAttribute("amount").getIntValue();
        if(amountEnum > 0) {
            StringBuilder enumBuilder = new StringBuilder();
            List<Element> Enums = enumerationElement.getChildren();
            for(Element eachEnum : Enums) {
                List<Element> lines = eachEnum.getChild("Lines").getChildren();
                for(Element line : lines) {
                    enumBuilder.append(line.getText());
                    enumBuilder.append("\r\n");
                }
            }
            //追加
            String cleanedPath = pathPrefix + "txt.cleaned";
            String enumResult = enumBuilder.toString();
            try{
                FileWriter writer = new FileWriter(cleanedPath, true);
                writer.write(enumResult);
                writer.close();
            } catch (Exception ex) {ex.printStackTrace();}
        }

        //最后将code.txt的内容和txt.cleaned中整合在一起, 放到all.txt中, 注意all.txt要放到reportsForVSM目录下
        File sourceFile = new File(pathPrefix + "txt.cleaned");
        Long fileLength = sourceFile.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        try{
            InputStream in = new FileInputStream(sourceFile);
            in.read(fileContent);
            in.close();
        } catch (Exception e) {e.printStackTrace();}

        String sourceResult = new String(fileContent);  //读出内容
        this.naturalLanguageTxt = sourceResult;

        //现在不要求bugReport在data/bugReports目录下，但是要求一定要是txt文件
        File file = new File(pathPrefix + "txt");
        String fileName = file.getName();
        String allPath = "data/reportsForVSM/" + fileName.substring(0, fileName.length()-3) + "all.txt";

        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(allPath));
            out.write(codeResult);
            out.write("\r\n");
            out.write(sourceResult);
            out.close();
        } catch(Exception ex) {ex.printStackTrace();}

        //TODO 将bugReport分类标记好复原供前端展示
        String[] codeTemp = codeTxt.split("\\r?\\n");
        String[] naturalTemp = naturalLanguageTxt.split("\\r?\\n");
        String[] stackTemp = stackTxt.split("\\r?\\n");
        File f = new File(pathPrefix + "txt");

        String allTxt = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())), Charset.forName(inputCharset));
//        String allTxt = Files.readString(f.toPath(), Charset.forName(inputCharset));
        String[] allTemp = allTxt.split("\\r?\\n");
        ArrayList<String> codeList = this.trimList(codeTemp);
        ArrayList<String> naturalList = this.trimList(naturalTemp);
        ArrayList<String> stackList = this.trimList(stackTemp);
        StringBuilder markedTxtBuilder = new StringBuilder();

        for(String item: allTemp) {
            String trimItem = item.trim();
            if(trimItem == null || trimItem.length() == 0){
                markedTxtBuilder.append(System.lineSeparator());
            } else if(naturalList.contains(trimItem)) {
                String markedItem = "$natural$" + item + "$$";
                markedTxtBuilder.append(markedItem);
                markedTxtBuilder.append(System.lineSeparator());
            } else if(codeList.contains(trimItem)) {
                String markedItem = "$code$" + item + "$$";
                markedTxtBuilder.append(markedItem);
                markedTxtBuilder.append(System.lineSeparator());
            } else {
                //如果没找到匹配默认为stackTrace
                String markedItem = "$stack$" + item + "$$";
                markedTxtBuilder.append(markedItem);
                markedTxtBuilder.append(System.lineSeparator());
            }
        }

        String markedTxt = markedTxtBuilder.toString();
        String markedPath = pathPrefix + "marked.txt";
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(markedPath));
            out.write(markedTxt);
            out.close();
        } catch(Exception ex) {ex.printStackTrace();}

        return markedTxt;

    }

    private ArrayList<String> trimList(String[] temp) {
        ArrayList<String> result = new ArrayList<>();
        for(String item : temp){
            String trimItem = item.trim();
            if(item.length() != 0){
                result.add(trimItem);
            }
        }

        return result;
    }

    private Set<String> setTokenForString(String resultTxt) {
        Set<String> resultToken = new HashSet<>();
        char[] charArray = resultTxt.toCharArray();

        StringBuilder itemBuilder = new StringBuilder();
        for(char c : charArray) {
            if((c <= 'z' && c >= 'a' || (c <= 'Z' && c >= 'A'))) {
                itemBuilder.append(c);
            }else {
                if(itemBuilder.length() > 0) {
                    String itemString = itemBuilder.toString();
                    resultToken.add(itemString);
                    itemBuilder = new StringBuilder();  //初始化StringBuilder
                }
            }
        }
        return resultToken;
    }

    private ArrayList<String> computeSimilarity(String projectPath, String bugReportPath) {
        String irModelName = "io.kuy.infozilla.vsm_algorithm.ir.VSM";
        DataPreprecess dataProcess = new DataPreprecess();
        DataPreprecess.setProjectPath(projectPath);
        DataPreprecess.setUcPath(bugReportPath);
        dataProcess.arrangeData();
        IR ir = new IR();
        //TODO 由于业务性质的不同，我觉得在这个应用中source应该是bugReport, target应该是源文件集合
        TextDataset textDataset = new TextDataset(dataProcess.getUcPreProcessedPath(), dataProcess.getClassProcessedPath());
        LinksList topTenList = ir.compute(textDataset,irModelName);

        ArrayList<String> result = new ArrayList<>();
        for(SingleLink link : topTenList) {
            String target = link.getTargetArtifactId();
            target += ".java";
            result.add(target);
        }

        return result;
    }

    private void setUpHighLighting(String classPath) {
        // 先将token列表赋值
        if(this.naturalLanguageToken.isEmpty() && this.naturalLanguageTxt != null && this.naturalLanguageTxt.length() != 0) {
            this.naturalLanguageToken = this.setTokenForString(this.naturalLanguageTxt);
        }
        if(this.codeToken.isEmpty() && this.codeTxt != null && this.codeTxt.length() != 0) {
            this.codeToken = this.setTokenForString(this.codeTxt);
        }
        if(this.stackToken.isEmpty() && this.stackTxt != null && this.stackTxt.length() != 0) {
            this.stackToken = this.setTokenForString(stackTxt);
        }

        String javaResult = null;
        try{
            File file = new File(classPath);
            FileInputStream inputStream = new FileInputStream(file);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            javaResult = new String(b);
        } catch (Exception ex) {ex.printStackTrace();}

        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(javaResult.toCharArray());

        CompilationUnit resultRoot = (CompilationUnit) parser.createAST(null);
        ASTNodeVisitorHelper visitorHelper = new ASTNodeVisitorHelper(this.codeToken, this.naturalLanguageToken, this.stackToken);
        resultRoot.accept(visitorHelper);

        String fileResult = resultRoot.toString();
//        fileResult = fileResult.replace("$blue$","<font color=\"blue\">");
//        fileResult = fileResult.replace("$green$","<font color=\"green\">");
//        fileResult = fileResult.replace("$yellow$","<font color=\"yellow\">");
//        fileResult = fileResult.replace("$$","</font>");

        //TODO 在这里将染色后文件路径设为result/colored_class
        File file = new File(classPath);
        String outputFilePath = "result/colored_class/" +  file.getName();

        File outputFile = new File(outputFilePath);
        try(OutputStream os = new FileOutputStream(outputFile)) {
            byte[] data = fileResult.getBytes();
            os.write(data);
        } catch (Exception ex) {ex.printStackTrace();}
    }

    //用分层遍历的方式而非递归调用
    private String FindFileInProject(String projectPath, String fileName){
        Queue<String> fileQueue = new LinkedList<>();
        fileQueue.offer(projectPath);

        while(!fileQueue.isEmpty()) {
            String nowSearching = fileQueue.poll();
            File file = new File(nowSearching);
            if(file.isFile()) {
                if(file.getName().equals(fileName)) {
                    return file.getAbsolutePath();
                }
            }
            else{
                String[] childFilePath = file.list();
                for(String path : childFilePath) {
                    fileQueue.offer(file.getAbsoluteFile() + "/" + path);
                }
            }
        }

        //如果遍历结束都没有找到，返回null
        return null;
    }
}
