package cs6343;

import java.util.List;

public class LoggingMDS implements IMetaData {
    @Override
    public boolean mkdir(String dirName) {
        System.out.println("MKDIR: " + dirName);
        return false;
    }

    @Override
    public List<String> ls(String dirName) {
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

	@Override
	public boolean partition(String data) {
		// TODO Auto-generated method stub
	      System.out.println("Partition: " + data);
		return false;
	}

	@Override
	public String printTree(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteChildren(String path) {
		// TODO Auto-generated method stub
		return false;
	}
}
