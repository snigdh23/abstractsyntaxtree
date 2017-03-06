package java;

import java.util.List;

/**
 * Created by snigdhc on 28/2/17.
 */
public class ListOfFiles {
    private static List<String> javaFiles;

    public static List<String> getJavaFiles() {
        return javaFiles;
    }

    public static void setJavaFiles(String file) {

        javaFiles.add(file);
    }
}
