/**
 * Created by snigdhc on 16/2/17.
 */

import com.google.gson.Gson;
import org.apache.hadoop.util.hash.Hash;
import org.eclipse.jdt.core.dom.*;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.*;

import java.net.InetAddress;
import java.util.*;

//import org.eclipse

/**.
 * Parsing of Java Code
 **/

class Main {

    /**.
     * Declaring List and HashMap variable for storing the parameters used and storing (parametername,parameterType)
     */
    static String fileName;
    private static List parameterList;
    private static HashMap<String, String> parameterTypeMap = new HashMap<>(); /* private static HashMap<String, String> parameterTypeMap; */


    /**
     * Reading the Input File to be parsed
     * @return the file content in a character array
     * @throws IOException
     */

    public static char[] readFile() throws IOException{
        fileName = "/home/snigdhc/Projects/AST/src/main/java/Main.java";
        File file = new File(fileName);
        FileReader fileReader;
        fileReader = new FileReader(file);
        int size = (int) file.length();
        int emptyFileFlag = 1;
        char[] filecontent = new char[size];
        if (fileReader.read(filecontent) != -1) {
            emptyFileFlag = 1;
        }
        else { emptyFileFlag = 0; System.out.println("Empty File"); System.exit(0); }

        return (filecontent);
    }


    public void settingParsers(ASTParser parser){
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

    }


    public void mapParameterNameToType(List paramList, HashMap<String, String> paramterTypeMap,
                                       MethodDeclaration node){

        for (int i = 0; i < paramList.size(); i++) {
            String parameterName = paramList.get(i).toString();
            String parameterType = ((SingleVariableDeclaration) node.parameters().get(i)).getType().toString();
            String actualparametername = parameterName.substring(parameterType.length() + 1);
            paramterTypeMap.put(actualparametername, parameterType);
        } // End of paramterlist for loop
    }

    public void checkingMethodInvocation(MethodDeclaration node, final CompilationUnit compilationUnit,
     final HashMap<String,HashMap<String, List<Integer>>> inner) {
        //node is the entire method and block gets the code inside the method
        final Block block = node.getBody();

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
                            String na = String.valueOf(node.getExpression());
                            inner.put(parameterType, methodU);
                            inner.get(parameterType).put(node.getName().toString(), methodList);
                            inner.get(parameterType).get(node.getName().toString()).add(linenumber);
                        }
                        else {

                            if (!inner.get(parameterType).containsKey(node.getName().toString())) {
                                String na = parameterType.getClass().toString();
                                inner.get(parameterType).put(node.getName().toString(), methodList);
                                inner.get(parameterType).get(node.getName().toString()).add(linenumber);
                            }
                             else {
                                inner.get(parameterType).get(node.getName().toString()).add(linenumber);
                            }
                        }
                    }//checking if methodcalled by a parameter is null or not
                }
                return true;
            } // End of MethodInvocation Visit
        }); // End of Block accept
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        final Main main = new Main();
        final HashMap<String, HashMap<String, HashMap<String, List<Integer>>>> outerMap = new HashMap<>();
        HashMap<String,HashMap<String, HashMap<String, HashMap<String, List<Integer>>>>> resultantMap = new HashMap<>();
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(readFile());
        main.settingParsers(parser);

        new FileCopyFromHDFS().getFiles();

        Client client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));


        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {
            public boolean visit(MethodDeclaration node) { //Enters every method


                System.out.println();
                SimpleName name = node.getName();

                parameterList = node.parameters();
                HashMap<String,HashMap<String,List<Integer>>> innerMap = new HashMap<>();
                main.mapParameterNameToType(parameterList, parameterTypeMap, node);
                main.checkingMethodInvocation(node,cu,innerMap);

                if(innerMap.keySet().contains(null)){
                    innerMap.remove(null);
                }

                //System.out.println(innerMap);

                outerMap.put(name.toString(),innerMap);
                return false;
            }// End of MethodDeclaration Visit
        });
        resultantMap.put("Main",outerMap);
        int a = 10;


        Gson gson = new Gson();
        String json = gson.toJson(resultantMap);
        IndexResponse indexResponse = client.prepareIndex("parsing","java").setSource(json).get();
        client.close();
    }
}