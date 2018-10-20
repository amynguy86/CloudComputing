package cs6343.iface;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs6343.data.FileType;
import cs6343.data.PhysicalInode;
import cs6343.data.MetaData;
import cs6343.util.Result;

public abstract class Storage{
	protected PhysicalInode root;
	public static Logger logger = LoggerFactory.getLogger(Storage.class);
    public abstract Result<String> ls(String path);
    public abstract Result<String> add(String path);
    //Leave to the implementation how this should be parsed
    public abstract Result<String> chmod(String path);
    public abstract Result<String> rm(String path);
    public abstract Result<String> mkdir(String path);
    public abstract Result<String> rmdir(String path);
    public abstract Result<String> mv(String path);
    
    public Storage() {
    	root= new PhysicalInode();
    	root.setName("/");
    	root.setParent(null);
    	MetaData metaData = new MetaData();
    	metaData.setType(FileType.DIRECTORY);
    	root.setMetaData(metaData);
    }
    
    public Result<?> executeCommand(String command) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	String[] cmd=command.trim().toLowerCase().split(" ",2);
		return (Result<?>) this.getClass().getMethod(cmd[0], String.class).invoke(this, cmd[1]);
		 
    }
}