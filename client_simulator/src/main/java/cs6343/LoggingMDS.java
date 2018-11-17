package cs6343;

import java.util.List;

public class LoggingMDS implements IMetaData {
    @Override
    public boolean mkdir(String dirName) {
        System.out.println("MKDIR: " + dirName);
        return false;
    }

    @Override
    public List<FileNode> ls(String dirName) {
        return null;
    }

    @Override
    public boolean touch(String filePath) {
        System.out.println("TOUCH: " + filePath);
        return false;
    }

    @Override
    public boolean rm(String filePath) {
        System.out.println("RM: " + filePath);
        return false;
    }

    @Override
    public boolean rmdir(String dirname) {
        System.out.println("RMDIR: " + dirname);
        return false;
    }
}
