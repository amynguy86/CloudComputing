package cs6343.iface;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cs6343.util.Result;

public abstract class Storage {
	public static Logger logger = LoggerFactory.getLogger(Storage.class);

    public abstract Result<String> ls(String path, boolean delay);
    public abstract Result<String> touch(String path, boolean delay);
    //Leave to the implementation how this should be parsed
    public abstract Result<String> chmod(String path, boolean delay);
    public abstract Result<String> rm(String path, boolean delay);
    public abstract Result<String> mkdir(String path, boolean delay);
    public abstract Result<String> rmdir(String path, boolean delay);
    public abstract Result<String> mv(String path, boolean delay);
    public abstract Result<String> print(String path, boolean delay);
    public Result<String> partition(String path,boolean delay) throws Exception{
    	throw new Exception("Not Implemented");
    }
   
    public Result<?> executeCommand(String command) {
    	try {
    		String[] cmd = command.trim().toLowerCase().split(" ", 2);
    		return (Result<?>) this.getClass().getMethod(cmd[0], String.class,boolean.class).invoke(this, cmd[1],false);
    	} catch (Exception ex) {
    		Result<String> result = new Result<>();
    		result.setOperationSuccess(false);
    		result.setOperationReturnMessage("Exception:"+ ex.getMessage());
    		logger.error("ex:",ex);
    		return result;
    	}
    	
    }

    public Result<?> executeCommandWithDelay(String command) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    	try {
    		String[] cmd = command.trim().toLowerCase().split(" ", 2);
    		return (Result<?>) this.getClass().getMethod(cmd[0], String.class,boolean.class).invoke(this, cmd[1],true);
    	} catch (Exception ex) {
    		Result<String> result = new Result<>();
    		result.setOperationSuccess(false);
    		result.setOperationReturnMessage("Exception:"+ ex.getMessage());
    		logger.error("ex:",ex);
    		return result;
    	}    	
    }
}