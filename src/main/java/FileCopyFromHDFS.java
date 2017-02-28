import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by snigdhc on 23/2/17.
 */
public class FileCopyFromHDFS {
    public void getFiles() throws  IOException, InterruptedException{
        String hdfspath="";
        String localpath="";
        String cmd = "hdfs dfs -copyToLocal "+hdfspath+" "+ localpath;
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
        while ((line = buf.readLine()) != null) {
            System.out.println(line);
        }

    }
}