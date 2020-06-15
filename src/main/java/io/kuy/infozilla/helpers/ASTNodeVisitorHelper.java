package io.kuy.infozilla.helpers;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

//实现visit节点的方法，继承ASTNodeVisitor
public class ASTNodeVisitorHelper extends ASTVisitor {
    private Set<String> codeToken;
    private Set<String> naturalLanguageToken;
    private Set<String> stackToken;
    private Set<String> javaReservedWords = new HashSet<>();  //将类似java util等词除去

    public ASTNodeVisitorHelper(Set<String> codeToken, Set<String> naturalLanguageToken, Set<String> stackToken) {
        this.codeToken = codeToken;
        this.naturalLanguageToken = naturalLanguageToken;
        this.stackToken = stackToken;
    }

    @Override
    public boolean visit(SimpleName name) {
        //TODO 对于stackTrace的特殊处理还要进行商榷
        this.resetIdentifier(name);
        return super.visit(name);
    }

    @Override
    public boolean visit(QualifiedName qName){
        Name qualifier = qName.getQualifier();
        resetIdentifier(qName.getName());
        while(!qualifier.getClass().equals(SimpleName.class)) {
            resetIdentifier(((QualifiedName) qualifier).getName());
            qualifier = ((QualifiedName) qualifier).getQualifier();
        }

        return super.visit(qName);
    }

    private void resetIdentifier(SimpleName name) {
        String identifier = name.getIdentifier();
        File reservedFile = new File("src/main/resources/Java_Reserved_Words.txt");
        String javaReservedTxt;
        try{
            //要将版本降到java8，所以读取文件的方法要变一下
            javaReservedTxt = new String(Files.readAllBytes(Paths.get(reservedFile.getAbsolutePath())));
//            javaReservedTxt = Files.readString(reservedFile.toPath());
            this.javaReservedWords = this.setTokenForString(javaReservedTxt);
        } catch(Exception ex) {ex.printStackTrace();}

        //首先不能被ReservedWords包含
        if(!this.javaReservedWords.contains(identifier)){
            if(codeToken.contains(identifier) && naturalLanguageToken.contains(identifier) && stackToken.contains(identifier)) {
                String coloredIdentifier = "$code_natural_stack$" + identifier + "$$";
                name.setIdentifier(coloredIdentifier);
            } else if(codeToken.contains(identifier) && naturalLanguageToken.contains(identifier)) {
                String coloredIdentifier = "$code_natural$" + identifier + "$$";
                name.setIdentifier(coloredIdentifier);
            } else if(codeToken.contains(identifier) && stackToken.contains(identifier)) {
                String coloredIdentifier = "$code_stack$" + identifier + "$$";
                name.setIdentifier(coloredIdentifier);
            } else if(naturalLanguageToken.contains(identifier) && stackToken.contains(identifier)) {
                String coloredIdentifier = "$natural_stack$" + identifier + "$$";
                name.setIdentifier(coloredIdentifier);
            } else if(codeToken.contains(identifier)) {
                String coloredIdentifier = "$code$" + identifier + "$$";
                name.setIdentifier(coloredIdentifier);
            } else if(naturalLanguageToken.contains(identifier)) {
                String coloredIdentifier = "$natural$" + identifier + "$$";
                name.setIdentifier(coloredIdentifier);
            } else if(stackToken.contains(identifier)) {
                String coloredIdentifier = "$stack$" + identifier + "$$";
                name.setIdentifier(coloredIdentifier);
            }
//            if(codeToken.contains(identifier) && naturalLanguageToken.contains(identifier)) {
//                String coloredIdentifier = "$blue$" + identifier + "$$";
//                name.setIdentifier(coloredIdentifier);
//            } else if(codeToken.contains(identifier)) {
//                String coloredIdentifier = "$green$" + identifier + "$$";
//                name.setIdentifier(coloredIdentifier);
//            } else if(naturalLanguageToken.contains(identifier)) {
//                String coloredIdentifier = "$yellow$" + identifier + "$$";
//                name.setIdentifier(coloredIdentifier);
//            }
        }
    }

    public Set<String> setTokenForString(String resultTxt) {
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
}
