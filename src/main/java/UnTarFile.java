/**
 * Created by snigdhc on 22/2/17.
 */

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.apache.hadoop.fs.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.input.PortableDataStream;

import org.apache.spark.api.java.*;
import org.eclipse.jgit.api.errors.GitAPIException;

public class UnTarFile {

    public String unTarringFiles(String filePath) throws IOException, GitAPIException, InterruptedException {
        Path path = new Path(filePath);
        String fileDestination="/home/snigdhc/TarFiles/Jaa/";
        String pathName = path.getName();
        String repoDestination = fileDestination+(pathName.substring(0,pathName.indexOf(".tar.gz")))+"/.git";
        File file = new File(repoDestination);
        if(!file.exists()){
        String cmd = "tar -xvf "+filePath+" -C "+fileDestination;
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        pr.waitFor();
        }
        return repoDestination;
    }

    public void doParsing(HashMap<String,String> filePathContentMap) throws InterruptedException, ExecutionException, IOException {
        for(String key:filePathContentMap.keySet()){
            String value = filePathContentMap.get(key);
            new JavaParser().mainProcess(key,value);
        }
    }

    public static void main(String args[]) throws IOException, URISyntaxException, ExecutionException, InterruptedException {

        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter the HDFS Local Path here: ");
        String hdfsPath = sc.next();
        System.out.print("Please enter the Local File Path here: ");
        String localPath = sc.next();
        FileCopyFromHDFS fileCopyFromHDFS = new FileCopyFromHDFS();
        fileCopyFromHDFS.getFiles(localPath,hdfsPath);

        SparkConf conf = new SparkConf();
        SparkContext sparkContext = new JavaSparkContext("local", "test", conf);
        JavaPairRDD<String, PortableDataStream> rdd1 = sparkContext.binaryFiles(localPath);

        List<String> zippedFileList = rdd1.keys().collect();

        JavaRDD<String> rdd2 = sparkContext.parallelize(zippedFileList,zippedFileList.size()).map(new Function<String, String>() {
            @Override
            public String call(String v1) throws Exception {
                String properJavaFilePath = v1.substring(v1.lastIndexOf(":")+1);
                UnTarFile utf = new UnTarFile();
                String repositoryDestination = utf.unTarringFiles(properJavaFilePath);
                HashMap<String,String> javaFileContentMap = new GitFileTreeWalk().getJavaFilesFromGitRepo(repositoryDestination);
                if(javaFileContentMap.size()>0)
                    utf.doParsing(javaFileContentMap);
                return (repositoryDestination);
            }
        });
        rdd2.count();
        sparkContext.close();
     }
}
