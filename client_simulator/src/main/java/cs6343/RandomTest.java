package cs6343;

import java.util.Random;

public class RandomTest {
    private FileNode sampleRoot;
    private FileNode createdRoot = new FileNode("/",  true);
    private Random random = new Random();


    public RandomTest(FileNode sampleRoot){
        this.sampleRoot = sampleRoot;
    }


    public void walk(int depth, int times){
        for(int i = 0; i < times; i++){
            FileNode current = sampleRoot;
            for(int j = 0; j < depth; j++){
               if(current == null || !current.isDirectory) break;
               current = randomFile(current);
            }
        }
    }

    public FileNode randomFile(FileNode dir){
    }
}
