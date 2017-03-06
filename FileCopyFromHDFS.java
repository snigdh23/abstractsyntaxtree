package java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by snigdhc on 23/2/17.
 */
public class FileCopyFromHDFS {
    public static void main(String args[]) throws  IOException, InterruptedException{
        Package[] packages = Package.getPackages();
        System.out.println(packages);
        for(int i=0;i<packages.length;i++){
            System.out.println(packages[i]);
        }
        String lala="asd";
        Scanner sc =new Scanner(System.in);
        System.out.println(lala.toLowerCase());
        List<String> list = new ArrayList<>();
        System.out.println(ClassLoader.getSystemClassLoader().getResources("/home/snigdhc/Projects/WordCount/src/main/java/Main.java").toString());

//        String hdfspath="";
//        String localpath="";
//        String cmd = "hdfs dfs -copyToLocal "+hdfspath+" "+ localpath;
//        Runtime run = Runtime.getRuntime();
//        Process pr = run.exec(cmd);
//        pr.waitFor();
//        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//        String line = "";
//        while ((line = buf.readLine()) != null) {
//            System.out.println(line);
//        }

    }
}