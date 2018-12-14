package cs6343;

import java.util.List;

public interface IMetaData {
    public boolean mkdir(String dirName);
    public List<String> ls(String dirName);
    public boolean touch(String filePath);
    public boolean rm(String filePath);
    public boolean rmdir(String dirname);
    public boolean partition(String data);
    public String printTree(String path);
    public boolean deleteChildren(String path);
    public void delayMe();
}
