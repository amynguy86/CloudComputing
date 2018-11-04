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
    FileNode root = new FileNode("/");

    public FileNode readFile(String filename) throws URISyntaxException, IOException {
        Stream<String> lines = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + filename))).lines();
        Iterator<ParsedLine> iter =  lines.map(this::cleanUp).iterator();
        Deque<FileNode> path = new ArrayDeque<>();
        int currDepth = 0;
        FileNode curr = root;
        FileNode lastDir = null;
        while(iter.hasNext()){
            ParsedLine line = iter.next();
            System.out.println(line);
            FileNode file = new FileNode(line.filename);
            if(line.isDirectory){
                currDepth++;
                curr.addSubFile(file);
                path.push(curr);
                curr = file;
            } else if (line.depth < currDepth){
                curr = path.pop();
                curr.addSubFile(file);
                currDepth--;
            } else {
                curr.addSubFile(file);
            }
        }
        return root;
    }

    public ParsedLine cleanUp(String in){
       int depth = StringUtils.countOccurrencesOf(in, "│");
       int fileStartIndex = in.indexOf("]") + 2;
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

