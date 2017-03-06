package java;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by snigdhc on 22/2/17.
 */
public class GitFileTreeWalk {

    public static void main(String[] args) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder.setGitDir(new File("/home/snigdhc/TarFiles/Jaa/javaone/.git"))
                .readEnvironment().findGitDir().build();
        Ref head = repository.findRef("HEAD");

            // a RevWalk allows to walk over commits based on some filtering that is defined
            try (RevWalk walk = new RevWalk(repository)) {
                RevCommit commit = walk.parseCommit(head.getObjectId());
                RevTree tree = commit.getTree();

                // now use a TreeWalk to iterate over all files in the Tree recursively
                // you can set Filters to narrow down the results if needed
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    //treeWalk.setFilter();
                    while (treeWalk.next()) {

                        String file = treeWalk.getNameString();
                       if (file.endsWith(".java")) {
                           String path = treeWalk.getPathString();
                           String pa = treeWalk.getNameString();

//                           treeWalk.setFilter(PathFilter.create(path));
//                           System.out.println(" found: " + path);
                           ObjectId objectId = treeWalk.getObjectId(0);
                           ObjectLoader loader = repository.open(objectId);

                           ByteArrayOutputStream out = new ByteArrayOutputStream();
                           loader.copyTo(out);
                           try(OutputStream outputStream = new FileOutputStream("/home/snigdhc/TarFiles/Jaa/"+pa)) {
                               out.writeTo(outputStream);
                           }

                       }
                          //  System.out.println(as);
                        }
                    }
                }
            }

    final static List<String> javaFilePathList = new ArrayList<>();
    static String path="";

    public void getJavaFilesFromGitRepo(String pathName) throws IOException, GitAPIException, ExecutionException, InterruptedException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        path = pathName;

        Repository repository = builder.setGitDir(new File(pathName))
                .readEnvironment().findGitDir().build();
        Ref head = repository.findRef("HEAD");
        List<String> javaFilesList = new ArrayList<>();
        HashMap<String,String> javaFilesMap = new HashMap<>();

        // a RevWalk allows to walk over commits based on some filtering that is defined
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = commit.getTree();

            // now use a TreeWalk to iterate over all files in the Tree recursively
            // you can set Filters to narrow down the results if needed
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {

                    String file = treeWalk.getNameString();
                    if (file.contains(".java")) {
                        String path = treeWalk.getPathString();
//                      System.out.println("FOUND: " + path);
                        String javaFilePath = "/home/snigdhc/TarFiles/Jaa/"+treeWalk.getNameString();

                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        loader.copyTo(out);
                        try(OutputStream outputStream = new FileOutputStream(javaFilePath)) {
                            out.writeTo(outputStream);
                        }

                        String actualPresentPath = path+"------"+javaFilePath;

                        javaFilesList.add(actualPresentPath);

                    }
                }
            }
        }
        javaFilePathList.addAll(javaFilesList);
    }
}