package cs6343;
import com.google.gson.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.util.Set;
public class CassandraMDS implements IMetaData {
    CassConnector cc;
    String lockHost="127.0.0.1";
    int lockPort=6969;
    public CassandraMDS(String IPAddress)
    {
        cc=new CassConnector(IPAddress);
        RemoteLock lock = new RemoteLock(lockHost,lockPort);
        lock.writeLock("/root/file1");

    }

    public void configureDB()
    {
        cc.configureDB();
        FileNode root=new FileNode("/",true);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        cc.insert("/",gson.toJson(root));
    }
    public void disconnect()
    {
        cc.shutdown();
    }
    public boolean mkdir(String dirName)
    {
        FileNode newFile= new FileNode(dirName, true);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        //find parent directory
        int lastIndex=dirName.lastIndexOf("/");
        String parentDir=dirName.substring(0,lastIndex);  //if parent is root using this sets parentDir to an empty string
        if(parentDir.equals("")) //parent directory is root
        {
            parentDir="/";
        }
        String parentString=cc.read(parentDir); //check that parent exists

        if(parentString==null) //parent directory does not exist
        {
            return false;
        }
        //add directory to the system
        RemoteLock lock1 = new RemoteLock(lockHost,lockPort);
        lock1.writeLock(dirName);
        boolean fileAdded=cc.insert(dirName, gson.toJson(newFile));//don't need read lock here, just checking that file exists
        lock1.unlock(dirName);
        if(!fileAdded)  //file already exists in system
        {
            return false;
        }
        //add directory to parent's list of subfiles
        RemoteLock lock2 = new RemoteLock(lockHost,lockPort);
        lock2.writeLock(parentDir);
        parentString=cc.read(parentDir);  //check that parent still exists and store it to add to subfiles list
        if(parentString==null)//parent was deleted since we last checked, need to undo file addition
        {
            RemoteLock lock3 = new RemoteLock(lockHost,lockPort);
            lock3.writeLock(dirName);
            cc.delete(dirName);
            lock3.unlock(dirName);
            lock2.unlock(parentDir);
            return false;
        }

        FileNode parentNode=gson.fromJson(parentString,FileNode.class);
        parentNode.addSubFile(newFile);
        cc.edit(parentDir, gson.toJson(parentNode));
        lock2.unlock(parentDir);
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
    //needs to add to parent
    public boolean touch(String filePath)
    {
        FileNode newFile= new FileNode(filePath, false);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        //find parent directory
        int lastIndex=filePath.lastIndexOf("/");
        String parentDir=filePath.substring(0,lastIndex);  //if parent is root using this sets parentDir to an empty string
        if(parentDir.equals("")) //parent directory is root
        {
            parentDir="/";
        }
        String parentString=cc.read(parentDir);
        if(parentString==null) //parent directory does not exist
        {
            return false;
        }
        //add directory to the system
        boolean fileAdded=cc.insert(filePath, gson.toJson(newFile));
        if(!fileAdded)  //file already exists in system
        {
            return false;
        }
        //add directory to parent's list of subfiles
        FileNode parentNode=gson.fromJson(parentString,FileNode.class);
        parentNode.addSubFile(newFile);
        cc.edit(parentDir, gson.toJson(parentNode));
        return true;
    }
    public boolean rm(String filePath)
    {
        //remove file
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        boolean fileDeleted=cc.delete(filePath);
        //find parent and remove directory from its subfiles list
        int lastIndex=filePath.lastIndexOf("/");
        String parentDir=filePath.substring(0,lastIndex);
        if(parentDir.equals("")) //parent directory is root
        {
            parentDir="/";
        }
        String parentString=cc.read(parentDir);
        if(parentString!=null) //parent exists
        {
            FileNode parentNode=gson.fromJson(parentString,FileNode.class);
            parentNode.removeSubFileByName(filePath);
            cc.edit(parentDir, gson.toJson(parentNode));
        }
        else
        {
            fileDeleted=false;
        }
        return fileDeleted;
    }
    //needs to recursively delete subdirectories
    public boolean rmdir(String dirname)
    {

        return false;
    }

}
