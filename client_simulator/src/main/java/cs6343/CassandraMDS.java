package cs6343;
import com.google.gson.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.util.Set;
public class CassandraMDS implements IMetaData {
    CassConnector cc=null;
    public CassandraMDS(String IPAddress)
    {
        cc=new CassConnector(IPAddress);
        cc.configureDB();

    }
    //what happens if called to make directory in root?
    public boolean mkdir(String dirName)
    {
        //add directory to cassandra
        FileNode newFile= new FileNode(dirName, true);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        boolean fileAdded=cc.insert(dirName, gson.toJson(newFile));
        if(!fileAdded)  //file already exists in system
        {
            return false;
        }
        //find parent and add directory to its subfiles list
        int lastIndex=dirName.lastIndexOf("/");
        String parentDir=dirName.substring(0,lastIndex);
        String parentString=cc.read(parentDir);
        if(parentString!=null) //parent exists
        {
            FileNode parentNode=gson.fromJson(parentString,FileNode.class);
            parentNode.addSubFile(newFile);
            cc.edit(parentDir, gson.toJson(parentNode));
        }
        return true;

    }
    public List<String> ls(String dirName)
    {
        String dirString=cc.read(dirName);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        FileNode fileNode=gson.fromJson(dirString, FileNode.class);
        Map<String, FileNode> subFiles=fileNode.getSubFiles();
        Set<String> keys=subFiles.keySet();
        List<String> subfileList = new ArrayList<String>();
        subfileList.addAll(keys);
        return subfileList;
    }
    public boolean touch(String filePath)
    {
        FileNode newFile= new FileNode(filePath, false);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        boolean fileAdded=cc.insert(filePath, gson.toJson(newFile));
        return fileAdded;
    }
    public boolean rm(String filePath)
    {
        //remove file
        FileNode newFile= new FileNode(filePath, true);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        boolean fileDeleted=cc.delete(filePath);
        //find parent and add directory to its subfiles list
        int lastIndex=filePath.lastIndexOf("/");
        String parentDir=filePath.substring(0,lastIndex);
        String parentString=cc.read(parentDir);
        if(parentString!=null) //parent exists
        {
            FileNode parentNode=gson.fromJson(parentString,FileNode.class);
            parentNode.removeSubFileByName(filePath);
            cc.edit(parentDir, gson.toJson(parentNode));
        }
        return fileDeleted;
    }
    //needs to recursively delete subdirectories?
    public boolean rmdir(String dirname)
    {
        return false;
    }
}
