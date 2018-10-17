package cs6343.iface;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs6343.util.Result;

public abstract class Storage{
	public static Logger logger = LoggerFactory.getLogger(Storage.class);
	
    public abstract Result<String> ls(String path);
    public abstract Result<Boolean> add(String path);
    //Leave to the implementation how this should be parsed
    public abstract Result<Boolean> chmod(String path);
    public abstract Result<Boolean> rm(String path);
    public abstract Result<Boolean> mkdir(String path);
    public abstract Result<Boolean> rmdir(String path);
    public abstract Result<Boolean> mv(String path);
    
    public Result<?> executeCommand(String command) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		logger.info("Command Recieved: "+command);
    	String[] cmd=command.trim().toLowerCase().split(" ",2);
		logger.info(cmd[0]);
		return (Result<?>) this.getClass().getMethod(cmd[0], String.class).invoke(this, cmd[1]);
		 
    }
}