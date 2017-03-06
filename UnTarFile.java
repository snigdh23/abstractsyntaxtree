package java; /**
 * Created by snigdhc on 22/2/17.
 */

import org.apache.hadoop.fs.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.input.PortableDataStream;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UnTarFile {
    final static int BUFFER = 2048;
    final static List<String> fileList = new ArrayList<>();

    public String methd(String filePath) throws IOException, GitAPIException, InterruptedException {
        Path path = new Path(filePath);
        String fileDestination="/home/snigdhc/TarFiles/Jaa/";
        String pathName = path.getName();
        String repoDestination = fileDestination+(pathName.substring(0,pathName.length()-7))+"/.git";
        File file = new File(repoDestination);
        if(!file.exists()){
        String cmd = "tar -xvf "+filePath+" -C "+fileDestination;
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        pr.waitFor();
        }
//        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//        String line = "";
//        while ((line = buf.readLine()) != null) {
//            System.out.println(line+"..............................................................");
//        }
        return repoDestination;
    }


    public static void main(String args[]) throws IOException, URISyntaxException, ExecutionException, InterruptedException {

        SparkConf conf = new SparkConf();
        JavaSparkContext sc = new JavaSparkContext("local", "test", conf);
        JavaPairRDD<String, PortableDataStream> rdd1 = sc.binaryFiles("/home/snigdhc/TarFiles/*.tar.gz");

        List<String> list = rdd1.keys().collect();

//        System.out.println(list);
        JavaRDD<String> rdd2 = sc.parallelize(list).map(new Function<String, String>() {
            @Override
            public String call(String v1) throws Exception {
                return (v1.substring(5));
            }
        });

//        System.out.println(";;;;;;;;;;;;;"+rdd2.collect()+" "+list.size());
          rdd2.collect();
        JavaRDD<String> rdd3;
        rdd3 = sc.parallelize(rdd2.take(list.size())).map(new Function<String, String>() {
            @Override
            public String call(String v1) throws Exception {
                UnTarFile utf = new UnTarFile();
//                System.out.println("!!!!!!!!!!!!!"+v1+"!!!");
                String str = utf.methd(v1);
                return str;
            }
        });

//        System.out.println("::::::::::::::::"+rdd3.collect());
        rdd3.collect();
        JavaRDD<String> rdd4 = sc.parallelize(rdd3.take(list.size())).repartition(150).map(new Function<String, String>() {
            @Override
            public String call(String v1) throws Exception {
                new GitFileTreeWalk().getJavaFilesFromGitRepo(v1);
                return "done";
            }
        });

        //System.out.println(rdd4.collect());
        rdd4.collect();

        List<String> allFiles = GitFileTreeWalk.javaFilePathList;

        JavaRDD<String> rdd5 = sc.parallelize(allFiles);

        rdd5.collect();

        JavaRDD<String> rdd6 = sc.parallelize(rdd5.take(allFiles.size())).map(new Function<String, String>() {
            @Override
            public String call(String v1) throws Exception {
                new Main().mainProcess(v1);
                return "Windy";
            }
        });

        rdd6.collect();

        sc.close();
//        System.out.println(GitFileTreeWalk.javaFilePathList.get(0));
//        new Main().mainProcess(GitFileTreeWalk.javaFilePathList.get(0));
     }
}