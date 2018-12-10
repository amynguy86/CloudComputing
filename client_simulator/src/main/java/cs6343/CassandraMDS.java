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
        System.out.println("Emptying database.");
        cc.configureDB();
        FileNode root=new FileNode("/",true);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        cc.insert("/",gson.toJson(root));
        System.out.println("Database configured.");
    }

    public void disconnect()
    {
        cc.shutdown();
    }

    public boolean mkdir(String dirName)
    {
        return addFile(dirName,true);
    }

    public List<String> ls(String dirName)
    {
        RemoteLock lock1 = new RemoteLock(lockHost,lockPort);
        lock1.readlock(dirName);
        String dirString=cc.read(dirName);
        lock1.unlock(dirName);
        if(dirString==null)//directory not found
        {
            return null;
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        FileNode fileNode=gson.fromJson(dirString, FileNode.class);
        if(fileNode.getSubFiles()==null)
        {
            List<String> emptyList = new ArrayList<>();
            return emptyList;
        }
        Map<String, FileNode> subFiles=fileNode.getSubFiles();
        Set<String> keys=subFiles.keySet();
        List<String> subfileList = new ArrayList<String>();
        subfileList.addAll(keys);
        return subfileList;
    }


    public boolean touch(String filePath)
    {
        return addFile(filePath,false);
    }

    public boolean rm(String filePath)
    {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        boolean fileDeleted=true;
        //find parent and remove file from its subfiles list
        int lastIndex=filePath.lastIndexOf("/");
        String parentDir=filePath.substring(0,lastIndex);
        if(parentDir.equals("")) //parent directory is root
        {
            parentDir="/";
        }
        RemoteLock lock1 = new RemoteLock(lockHost,lockPort);
        lock1.writeLock(parentDir);
        String parentString=cc.read(parentDir);
        if(parentString!=null) //parent exists
        {
            FileNode parentNode=gson.fromJson(parentString,FileNode.class);
            parentNode.removeSubFileByName(filePath);
            cc.edit(parentDir, gson.toJson(parentNode));
        }
        else
        {
            lock1.unlock(parentDir);
            return false;  //parent did not exist
        }
        lock1.unlock(parentDir);
        RemoteLock lock2 = new RemoteLock(lockHost,lockPort);
        lock2.writeLock(filePath);
        fileDeleted=cc.delete(filePath);  //set to false if file didn't exist, true if exists and was deleted
        lock2.unlock(filePath);
        return fileDeleted;
    }

    private boolean addFile(String path, boolean isDirectory)
    {
        FileNode newFile= new FileNode(path, isDirectory);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        //find parent directory
        int lastIndex=path.lastIndexOf("/");
        String parentDir=path.substring(0,lastIndex);  //if parent is root using this sets parentDir to an empty string
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
        lock1.writeLock(path);
        boolean fileAdded=cc.insert(path, gson.toJson(newFile));
        lock1.unlock(path);
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
            lock3.writeLock(path);
            cc.delete(path);
            lock3.unlock(path);
            lock2.unlock(parentDir);
            return false;
        }
        FileNode parentNode=gson.fromJson(parentString,FileNode.class);
        parentNode.addSubFile(newFile);
        cc.edit(parentDir, gson.toJson(parentNode));
        lock2.unlock(parentDir);
        return true;

    }

    //returns true if directory was successfully removed
    public boolean rmdir(String dirName)
    {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        boolean fileDeleted=true;
        //find parent and remove directory from its subfiles list
        int lastIndex=dirName.lastIndexOf("/");
        String parentDir=dirName.substring(0,lastIndex);
        if(parentDir.equals("")) //parent directory is root
        {
            parentDir="/";
        }
        RemoteLock lock1 = new RemoteLock(lockHost,lockPort);
        lock1.writeLock(parentDir);
        String parentString=cc.read(parentDir);
        if(parentString!=null) //parent exists
        {
            FileNode parentNode=gson.fromJson(parentString,FileNode.class);
            parentNode.removeSubFileByName(dirName);
            cc.edit(parentDir, gson.toJson(parentNode));
        }
        else
        {
            lock1.unlock(parentDir);
            return false;  //parent did not exist
        }
        lock1.unlock(parentDir);
        RemoteLock lock2 = new RemoteLock(lockHost,lockPort);
        lock2.writeLock(dirName);
        String fileString=cc.read(dirName);
        fileDeleted=cc.delete(dirName);  //set to false if file didn't exist, true if exists and was deleted
        lock2.unlock(dirName);
        if(fileString!=null)
        {
            //get list of subdirectories/subfiles from the deleted directory
            FileNode fileNode=gson.fromJson(fileString,FileNode.class);
            Map<String, FileNode> subFiles=fileNode.getSubFiles();
            if(subFiles!=null) {
                Set<String> keys = subFiles.keySet();
                List<String> subfileList = new ArrayList<String>();
                subfileList.addAll(keys);
                for (int i = 0; i < subfileList.size(); i++) {
                    recursiveDelete(subfileList.get(i));
                }
            }
        }

        return fileDeleted;
    }

    //only called when file has already been disconnected from system by deleting parent, so doesn't need to worry about locks
    private void recursiveDelete(String path)
    {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String fileString=cc.read(path);
        FileNode fileNode=gson.fromJson(fileString,FileNode.class);
        if(fileNode.isDirectory)
        {
            Map<String, FileNode> subFiles=fileNode.getSubFiles();
            if(subFiles!=null) {
                Set<String> keys = subFiles.keySet();
                List<String> subfileList = new ArrayList<String>();
                subfileList.addAll(keys);
                for (int i = 0; i < subfileList.size(); i++) {
                    recursiveDelete(subfileList.get(i));
                }
            }
        }
        cc.delete(path);
    }
}
