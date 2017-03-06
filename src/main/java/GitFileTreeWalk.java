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
import scala.Tuple2;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by snigdhc on 22/2/17.
 */
public class GitFileTreeWalk {

//    final static List<String> javaFilePathList = new ArrayList<>();

    public HashMap<String, String> getJavaFilesFromGitRepo(String pathName) throws IOException, GitAPIException, ExecutionException, InterruptedException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        File file = new File(pathName);
        HashMap<String, String> javaFilesMap = new HashMap<>();
        if (file.exists()) {
            Repository repository = builder.setGitDir(new File(pathName)).readEnvironment().findGitDir().build();
            Ref head = repository.findRef("HEAD");

            // a RevWalk allows to walk over commits based on some filtering that is defined
            try (RevWalk walk = new RevWalk(repository)) {
                if (walk != null) {
                    RevCommit commit = walk.parseCommit(head.getObjectId());
                    RevTree tree = commit.getTree();

                    // now use a TreeWalk to iterate over all files in the Tree recursively
                    // you can set Filters to narrow down the results if needed
                    try (TreeWalk treeWalk = new TreeWalk(repository)) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(true);
                        while (treeWalk.next()) {

                            String filePath = treeWalk.getNameString();
                            if (filePath.endsWith(".java")) {
                                String path = treeWalk.getPathString();

                                ObjectId objectId = treeWalk.getObjectId(0);
                                ObjectLoader loader = repository.open(objectId);

                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                loader.copyTo(out);
                                String content = out.toString();

                                javaFilesMap.put(path, content);
                            }
                        }
                    }
                }
            }
            return javaFilesMap;
        }
        else{
            return javaFilesMap;
        }
    }
}