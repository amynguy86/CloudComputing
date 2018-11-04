package cs6343;

import java.util.HashMap;
import java.util.Map;

public class FileNode {
    public String filename;
    private Map<String, FileNode> subFiles;

    public FileNode(String filename){
        this.filename = filename;
    }

    public void addSubFile(FileNode subfile){
        if(subFiles == null){
            subFiles = new HashMap<String, FileNode>();
        }
        subFiles.put(subfile.filename, subfile);
    }

    public void removeSubFile(FileNode subfile){
        if(subFiles != null){
            subFiles.put(subfile.filename, null);
        }
    }

    public String prettyPrint(String prefix){
       String out = prefix + filename + "\n";
       if(subFiles == null) return out;
       for(FileNode subs : subFiles.values()){
           out += subs.prettyPrint(prefix + prefix.charAt(prefix.length()-1));
       }
       return out;
    }

    public String prettyPrint(){
        return prettyPrint(" ");
    }

}

