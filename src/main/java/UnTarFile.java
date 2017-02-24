/**
 * Created by snigdhc on 22/2/17.
 */

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.input.PortableDataStream;
import scala.Function1;
import scala.Tuple2;

import com.google.common.base.Charsets;
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.input.PortableDataStream;

import org.apache.hadoop.fs.Hdfs;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class UnTarFile {
    final static int BUFFER = 2048;



    public static void main(String[] args) throws IOException, URISyntaxException {

        SparkConf conf = new SparkConf();
        JavaSparkContext sc = new JavaSparkContext("local", "test", conf);
        JavaPairRDD<String, PortableDataStream> rdd = sc.binaryFiles("/home/snigdhc/JavaCodes.tar.gz");

        //JavaRDD<Tuple2<String, String>> extracted = new TarGzExtractor().extractAndDecode(rdd, Charsets.UTF_8);
        //System.out.println(extracted.collect());
        //sc.para
        sc.stop();
        System.out.println("Starts");

        FileInputStream fin = new FileInputStream("/home/snigdhc/JavaCodes.tar.gz");
        BufferedInputStream in = new BufferedInputStream(fin);
        GzipCompressorInputStream gzIn = new GzipCompressorInputStream(fin);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);
        TarArchiveEntry entry = null;

        /** Read the tar entries using the getNextEntry method **/

        // sc.binaryFiles() => map{ something }
        // PortableDataStream pds = new PortableDataStream();


        while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {

            if (!entry.isDirectory()) {
                String name = entry.getName();
                if(name.endsWith(".java"))
                    System.out.println("Extracting: "+entry.getName());
            }

   else {



            }
        }
        /** Close the input stream **/

        tarIn.close();
                System.out.println("untar completed successfully!!");
     }
}