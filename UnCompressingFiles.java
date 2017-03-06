package java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by snigdhc on 28/2/17.
 */
public class UnCompressingFiles {
    public static void main(String args[]) throws IOException, InterruptedException{

    String cmd = "tar -xvf /home/snigdhc/TarFiles/Dhinchak.tar.gz";
    Runtime run = Runtime.getRuntime();
    Process pr = run.exec(cmd);
        pr.waitFor();
    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
    String line = "";
        while ((line = buf.readLine()) != null) {
        System.out.println(line);
    }
//        SparkConf conf = new SparkConf();
//        JavaSparkContext sc = new JavaSparkContext("local", "test", conf);
//        JavaPairRDD<String,List<String>> files;
    }
    }