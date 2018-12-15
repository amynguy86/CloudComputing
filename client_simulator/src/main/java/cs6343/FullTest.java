package cs6343;

import org.springframework.util.CollectionUtils;

import java.util.*;

public class FullTest{
    private FileNode sampleRoot;
    private Random random = new Random();
    private IMetaData mds;


    public FullTest(FileNode sampleRoot, IMetaData mds){
        this.sampleRoot = sampleRoot;
        this.mds = mds;
    }


    public void walk(){
        for(FileNode f : sampleRoot.getSubFiles().values()){
            walk(f, "");
        }
    }

    public void walk(FileNode f, String prefix){
        if(f.isDirectory){
            mds.mkdir(prefix + "/" + f.filename);
            if(f.getSubFiles() != null){
                for(FileNode c : f.getSubFiles().values()){
                    walk(c, prefix + "/" +  f.filename);
                }
            }
        } else {
            mds.touch(prefix + "/" + f.filename);
        }
    }

    public void destroy(){
        for(FileNode f : sampleRoot.getSubFiles().values()){
            destroy(f, "");
        }
    }

    public void destroy(FileNode f, String prefix){
        if(f.isDirectory){
            if(f.getSubFiles() != null){
                for(FileNode c : f.getSubFiles().values()){
                    destroy(c, prefix + "/" + f.filename);
                    ensureNotInside(mds.ls(prefix + "/" + f.filename), c.filename);
                }
            }
            mds.rmdir(prefix + "/" + f.filename);
        } else {
            mds.rm(prefix + "/" + f.filename);
        }
    }


    private void ensureNotInside(List<FileNode> ls, String filename) {
        if(ls == null) return;
        for(FileNode f : ls){
           if(filename.equals(f.getFileName())){
               throw new RuntimeException("Deleted a file and it was still present in response");
           }
        }
    }

}
