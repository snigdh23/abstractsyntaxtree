import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by snigdhc on 22/2/17.
 */
public class GitFileTreeWalk {

    public static void main(String[] args) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();


        Repository repository = builder.setGitDir(new File("/home/snigdhc/Projects/AST/.git"))
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
                    while (treeWalk.next()) {

                        String file = treeWalk.getNameString();
                       if (file.endsWith(".java")) {
                          System.out.println("found: " + treeWalk.getPathString());
                        }
                          //  System.out.println(as);
                        }
                    }
                }
            }
        }
