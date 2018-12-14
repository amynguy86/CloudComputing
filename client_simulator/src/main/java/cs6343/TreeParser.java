package cs6343;

import cs6343.FileNode;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class TreeParser{
    FileNode root = new FileNode("/", true);

    public FileNode readFile(String filename) throws URISyntaxException, IOException {
        Stream<String> lines = Files.lines(Paths.get(filename));
        Iterator<ParsedLine> iter =  lines.map(this::cleanUp).iterator();
        Deque<FileNode> path = new ArrayDeque<>();
        int currDepth = 0;
        int numProcessed = 0;
        FileNode curr = root;
        FileNode lastDir = null;
        while(iter.hasNext()){
            numProcessed++;
            ParsedLine line = iter.next();
            FileNode file = new FileNode(line.filename, line.isDirectory);
            while(currDepth > line.depth){ 
                curr = path.pop();
                currDepth--;
            }
            if(line.isDirectory){
                currDepth++;
                curr.addSubFile(file);
                path.push(curr);
                curr = file;
            } else{
                curr.addSubFile(file);
            }
        }
        return root;
    }

    public ParsedLine cleanUp(String in){
        if(in.startsWith("directory")){
            return new ParsedLine(0, in, true);
        }
        int depth = in.indexOf("[")/4;
        int fileStartIndex = in.indexOf("]") + 3;
        boolean isDirectory = in.endsWith("/");
        return new ParsedLine(depth,in.substring(fileStartIndex, isDirectory ? in.length() - 1 : in.length()), isDirectory);
    }

}

class ParsedLine {
    int depth;
    String filename;
    boolean isDirectory;

    public ParsedLine(int depth, String filename, boolean isDirectory){
        this.depth = depth;
        this.filename = filename;
        this.isDirectory = isDirectory;
    }

    public String toString(){
        return "Filename: " + filename + " isDirectory: " + isDirectory + " depth: " + depth;
    }
}

