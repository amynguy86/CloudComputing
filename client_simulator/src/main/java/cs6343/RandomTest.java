package cs6343;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class RandomTest {
    private FileNode sampleRoot;
    private FileNode createdRoot = new FileNode("/",  true);
    private Random random = new Random();
    private IMetaData mds;


    public RandomTest(FileNode sampleRoot, IMetaData mds){
        this.sampleRoot = sampleRoot;
        this.mds = mds;
    }


    public void walk(int depth, int times){
        for(int i = 0; i < times; i++){
            FileNode current = sampleRoot;
            String currentPath = "/";
            FileNode createdCurrent = createdRoot;
            for(int j = 0; j < depth; j++){
               if(!current.isDirectory) break;
               current = randomFile(current);
               if(current == null) break;
               currentPath += current.filename;
               if(current.isDirectory){
                   mds.mkdir(currentPath);
                   currentPath += "/";
                   FileNode updatedFile = updateCreatedTree(createdCurrent, current);
                   createdCurrent.addSubFile(updatedFile);
                   createdCurrent = createdCurrent.getSubFile(current.filename);
               } else  {
                   createdCurrent.addSubFile(new FileNode(current.filename, false));
                   mds.touch(currentPath);
               }
            }
        }
        System.out.println(createdRoot.prettyPrint());
    }

    private FileNode updateCreatedTree(FileNode createdCurrent, FileNode current) {
        if(createdCurrent.getSubFile(current.filename) != null) {
            return createdCurrent.getSubFile(current.filename);
        } else {
            FileNode newNode = new FileNode(current.filename, current.isDirectory);
            createdCurrent.addSubFile(newNode);
            return newNode;
        }
    }

    public void destructiveWalk(int times, int depth, double destroyProb){
        for(int i = 0; i < times; i++){
            String currentPath = "/";
            FileNode createdCurrent = createdRoot;
            FileNode current = null;
            for(int j = 0; j < depth; j++){
                if(!current.isDirectory) break;
                current = randomFile(current);
                if(current == null) break;
                currentPath += current.filename;
                if(random.nextDouble() < destroyProb) continue;
                if(current.isDirectory){
                    currentPath += "/";
                    FileNode updatedFile = updateCreatedTree(createdCurrent, current);
                    createdCurrent.addSubFile(updatedFile);
                    createdCurrent = createdCurrent.getSubFile(current.filename);
                } else  {
                    createdCurrent.addSubFile(new FileNode(current.filename, false));
                    mds.touch(currentPath);
                }
            }
        }
    }

    public FileNode randomFile(FileNode dir){
        if(dir.getSubFiles() == null) return null;
        Collection<FileNode> nodes = dir.getSubFiles().values();
        if(nodes == null || nodes.isEmpty()) return null;
        int index = random.nextInt(nodes.size());
        Iterator<FileNode> iter = nodes.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }
}
