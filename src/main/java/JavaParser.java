/**
 * Created by snigdhc on 16/2/17.
 */

import org.elasticsearch.client.Client;
import org.elasticsearch.action.index.IndexResponse;
import org.eclipse.jdt.core.dom.*;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.google.gson.Gson;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**.
 * Parsing of Java Code
 **/

class JavaParser {

    /**
     * Declaring List and HashMap variable for storing the parameters used and storing (parametername,parameterType)
     **/

    private static List parametersList;
    private static HashMap<String, String> parameterTypeMap = new HashMap<>(); /* private static HashMap<String, String> parameterTypeMap; */
    private int paramtersListLength =0;
    private int flag=0;

    /**
     * Reading the Input File to be parsed
     *
     * @return the file content in a character array
     * @throws IOException
     */

    public void settingParsers(ASTParser parser) {
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
    }


    public void mapParameterNameToType(List paramList, HashMap<String, String> paramterTypeMap,
                                       MethodDeclaration node) {

        for (int i = 0; i < paramList.size(); i++) {
            String parameter = paramList.get(i).toString();
            String parameterType = ((SingleVariableDeclaration) node.parameters().get(i)).getType().toString();
            String parameterName = parameter.substring(parameterType.length() + 1);
            paramterTypeMap.put(parameterName, parameterType);
        } // End of paramterlist for loop
    }

    public int checkingMethodInvocation(MethodDeclaration node, final CompilationUnit compilationUnit,
                                         final HashMap<String, HashMap<String, List<Integer>>> inner) {
        //node is the entire method and block gets the code inside the method
        final Block block = node.getBody();
        int flag = 0;
        if (block != null) {
            flag = 1;
            block.accept(new ASTVisitor() {
                public boolean visit(MethodInvocation node) {

                    List<Integer> methodList = new ArrayList<>();
                    HashMap<String, List<Integer>> methodU = new HashMap<>();
                    Expression paramterName = node.getExpression();//Gives me the parameter calling the method
                    if (paramterName != null) { //checking if methodcalled by a parameter or not

                        String parameterType = parameterTypeMap.get(paramterName.toString());
                        int linenumber = compilationUnit.getLineNumber(node.getStartPosition());
                        if (parameterType != null) {

                            if (!inner.containsKey(parameterType)) {
                                inner.put(parameterType, methodU);
                                inner.get(parameterType).put(node.getName().toString(), methodList);
                                inner.get(parameterType).get(node.getName().toString()).add(linenumber);
                            } else {

                                if (!inner.get(parameterType).containsKey(node.getName().toString())) {
                                    inner.get(parameterType).put(node.getName().toString(), methodList);
                                    inner.get(parameterType).get(node.getName().toString()).add(linenumber);
                                } else {
                                    inner.get(parameterType).get(node.getName().toString()).add(linenumber);
                                }
                            }
                        }//checking if methodcalled by a parameter is null or not
                    }
                    return true;
                } // End of MethodInvocation Visit
            }); // End of Block accept
        }
        return flag;
    }


    public String mainProcess(String filePath,String content) throws IOException, InterruptedException, ExecutionException {
        final JavaParser javaParser = new JavaParser();

        int lastIndex = filePath.lastIndexOf('/');

        final Client client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        final HashMap<String, HashMap<String, HashMap<String, List<Integer>>>> outerMap = new HashMap<>();
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, List<Integer>>>>> resultantMap = new HashMap<>();
        final HashMap<String, String> sourcefiles = new HashMap<>();
        ASTParser parser = ASTParser.newParser(AST.JLS8);

        char[] sourcecontent = content.toCharArray();
        parser.setSource(sourcecontent);
        javaParser.settingParsers(parser);

        final CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);

        // Entering every Declared Method
        compilationUnit.accept(new ASTVisitor() {
            public boolean visit(MethodDeclaration node) {
                SimpleName declaredMethodName = node.getName();
                parametersList = node.parameters();
                paramtersListLength = node.parameters().size();
                if(paramtersListLength >0) {
                    HashMap<String, HashMap<String, List<Integer>>> innerMap = new HashMap<>();
                    javaParser.mapParameterNameToType(parametersList, parameterTypeMap, node);
                    flag = javaParser.checkingMethodInvocation(node, compilationUnit, innerMap);
                    if (innerMap.keySet().contains(null)) {
                        innerMap.remove(null);
                    }
                    outerMap.put(declaredMethodName.toString(), innerMap);
                }
                return false;
            }// End of MethodDeclaration Visit
        });

     if(flag==1){
        resultantMap.put(filePath.substring(0, filePath.lastIndexOf(".java")), outerMap);
        Gson gson = new Gson();
        String json1 = gson.toJson(resultantMap);
        IndexResponse response1 = client.prepareIndex("parsing", "java").setSource(json1).get();

        sourcefiles.put("Java File: " + filePath.substring(lastIndex+1, filePath.lastIndexOf(".java")), content);
        String json2 = gson.toJson(sourcefiles);

        IndexResponse response2 = client.prepareIndex("sourcefiles", "properjava").setSource(json2).get();
    }
        client.close();
     return filePath;
    }
}
