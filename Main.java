package java; /**
 * Created by snigdhc on 16/2/17.
 */

import com.google.gson.Gson;
import org.apache.spark.SparkConf;
import org.eclipse.jdt.core.dom.*;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

//import org.elasticsearch.transport.client.PreBuiltTransportClient;

//import org.eclipse

/**.
 * Parsing of Java Code
 **/

class Main {

    /**
     * .
     * Declaring List and HashMap variable for storing the parameters used and storing (parametername,parameterType)
     */
    private static List parameterList;
    private static HashMap<String, String> parameterTypeMap = new HashMap<>(); /* private static HashMap<String, String> parameterTypeMap; */
    static int i = 0;


    /**
     * Reading the Input File to be parsed
     *
     * @return the file content in a character array
     * @throws IOException
     */

    public static char[] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        FileReader fileReader;
        fileReader = new FileReader(file);
        int size = (int) file.length();
        int emptyFileFlag = 1;
        char[] filecontent = new char[size];
        if (fileReader.read(filecontent) != -1) {
            emptyFileFlag = 1;
        } else {
            emptyFileFlag = 0;
//            System.out.println("Empty File");
            System.exit(0);
        }

        return (filecontent);
    }


    public void settingParsers(ASTParser parser) {
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

    }


    public void mapParameterNameToType(List paramList, HashMap<String, String> paramterTypeMap,
                                       MethodDeclaration node) {

        for (int i = 0; i < paramList.size(); i++) {
            String parameterName = paramList.get(i).toString();
//            System.out.println(((SingleVariableDeclaration) node.parameters().get(i)).getName().getFullyQualifiedName()+".........");
            String parameterType = ((SingleVariableDeclaration) node.parameters().get(i)).getType().toString();
//            ((SingleVariableDeclaration) node.parameters().get(i)).get
//            ((SingleVariableDeclaration)node.parameters().get(0)).getType()

            String actualparametername = parameterName.substring(parameterType.length() + 1);
            paramterTypeMap.put(actualparametername, parameterType);
        } // End of paramterlist for loop
    }

    public int checkingMethodInvocation(MethodDeclaration node, final CompilationUnit compilationUnit,
                                         final HashMap<String, HashMap<String, List<Integer>>> inner) {
        //node is the entire method and block gets the code inside the method
        final Block block = node.getBody();
        int flag = 0;
        if (block != null) {
            flag = 1;
            Package[] packages = Package.getPackages();

            block.accept(new ASTVisitor() {
                public boolean visit(MethodInvocation node) {

                    List<Integer> methodList = new ArrayList<>();
                    HashMap<String, List<Integer>> methodU = new HashMap<>();
                    Expression paramterName = node.getExpression();//Gives me the parameter calling the method
                    if (paramterName != null) { //checking if methodcalled by a parameter or not

                        String a = node.getName().getFullyQualifiedName();
                        String parameterType = parameterTypeMap.get(paramterName.toString());
                        int linenumber = compilationUnit.getLineNumber(node.getStartPosition());
                        if (parameterType != null) {

                            if (!inner.containsKey(parameterType)) {
                                //String na = String.valueOf(node.getExpression());
                                inner.put(parameterType, methodU);
                                inner.get(parameterType).put(node.getName().toString(), methodList);
                                inner.get(parameterType).get(node.getName().toString()).add(linenumber);
                            } else {

                                if (!inner.get(parameterType).containsKey(node.getName().toString())) {
                                    //  String na = parameterType.getClass().toString();
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

    static int flag,paramterslist=0;
    public String mainProcess(String filePath) throws IOException, InterruptedException, ExecutionException {
        SparkConf conf = new SparkConf();
        UnTarFile utf = new UnTarFile();
        final Main main = new Main();
        String split = "------";
        String[] strings = filePath.split(split);
        int last = strings[0].lastIndexOf('/');
//        JavaSparkContext sc = new JavaSparkContext("local", "appName", conf);

        final Client client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        final HashMap<String, HashMap<String, HashMap<String, List<Integer>>>> outerMap = new HashMap<>();
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, List<Integer>>>>> resultantMap = new HashMap<>();
        final HashMap<String, String> sourcefiles = new HashMap<>();
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        String fileName = strings[1];
        parser.setSource(main.readFile(fileName));
        main.settingParsers(parser);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {
            public boolean visit(MethodDeclaration node) { //Enters every method
                System.out.println();
//                System.out.println(node.getAST().);
//                node.
                SimpleName name = node.getName();
                parameterList = node.parameters();
                paramterslist = parameterList.size();
                if(paramterslist>0) {
                    HashMap<String, HashMap<String, List<Integer>>> innerMap = new HashMap<>();
                    main.mapParameterNameToType(parameterList, parameterTypeMap, node);
                    flag = main.checkingMethodInvocation(node, cu, innerMap);
                    if (innerMap.keySet().contains(null)) {
                        innerMap.remove(null);
                    }
                    outerMap.put(name.toString(), innerMap);
                }
                return false;
            }// End of MethodDeclaration Visit
        });
     if(flag==1){
        String temp = strings[0];
        resultantMap.put(temp.substring(0, temp.length() - 5), outerMap);
        Gson gson = new Gson();
        String json1 = gson.toJson(resultantMap);
        IndexResponse response1 = client.prepareIndex("parsing", "java").setSource(json1).get();
        String id = response1.getId();

        String sourceFile = "";
        BufferedReader br = new BufferedReader(new FileReader(strings[1]));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            sourceFile = sb.toString();
        } finally {
            br.close();
        }
        sourcefiles.put("Java File: " + temp.substring(last+1, temp.length() - 5), sourceFile);
        String json2 = gson.toJson(sourcefiles);

        IndexResponse response2 = client.prepareIndex("sourcefiles", "properjava").setSource(json2).get();
        System.out.println(id);

    }
        client.close();
        return strings[0];
    }
/*
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException, ExecutionException {

        SparkConf conf = new SparkConf();
        UnTarFile utf = new UnTarFile();
        utf.main(args);
        JavaSparkContext sc = new JavaSparkContext("local","appName",conf);
//        List<String> allFiles = ListOfFiles.getJavaFiles();
        System.out.println("LALALALALALA LALALALALA");
        final Client client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        System.out.println("HERE");
//        Iterator<String> tata = allFiles.iterator();
//        while(tata.hasNext()){
//            System.out.println(tata.next());
//        }
//        System.out.println(allFiles.size());
//        JavaRDD<String> rdd5 = sc.parallelize(allFiles).map(new Function<String, String>() {
//            @Override
//            public String call(String v1) throws Exception {
//                Main main1 = new Main();
//                Main main2 = new Main();
//                String str =  main1.mainProcess(main2,"/home/snigdhc/Projects/AST/src/main/java/SampleCode.java");
//                return str;
//            }
//        });
//
//        System.out.println(rdd5.collect());
//        client.close();
//        sc.close();
    }
}
*/

}