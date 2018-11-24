package cs6343;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.*;

public class FileNode {
    public String filename;
    public boolean isDirectory;
    private Map<String, FileNode> subFiles;

    public FileNode(String filename, boolean isDirectory){
        this.filename = filename;
        this.isDirectory = isDirectory;
    }

    public Map<String, FileNode> getSubFiles(){
        return subFiles;
    }

    public FileNode getSubFile(String filename){
        if(subFiles == null){
            subFiles = new HashMap<String, FileNode>();
        }
        return subFiles.get(filename);
    }

    public void addSubFile(FileNode subfile){
        if(subFiles == null){
            subFiles = new HashMap<String, FileNode>();
        }
        subFiles.put(subfile.filename, subfile);
    }

    public String getFileName()
    {
        return filename;
    }

    //pretty sure this doesn't completely remove the entry

    public void removeSubFile(FileNode subfile){
        if(subFiles != null){
            subFiles.put(subfile.filename, null);
        }
    }

    public void removeSubFileByName(String name)
    {
        if(subFiles!=null)
        {
            subFiles.remove(this.getSubFile(name));
        }
    }

    public String prettyPrint(String prefix){
       String out = prefix + filename + "\n";
       if(subFiles == null) return out;
       for(FileNode subs : subFiles.values()){
           if(subs != null){
               out += subs.prettyPrint(prefix + prefix.charAt(prefix.length()-1));
           }
       }
       return out;
    }

    public String prettyPrint(){
        return prettyPrint(" ");
    }

}

