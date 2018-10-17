package cs6343.centralized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs6343.data.Directory;
import cs6343.iface.Storage;
import cs6343.util.Result;

public class CentralizedStorage extends Storage {
	public static Logger logger = LoggerFactory.getLogger(CentralizedStorage.class);
    public Directory rootDir = new Directory();

    @Override
    public Result<String> ls(String path) {
        String[] components = path.split("/");
        Directory d = parentDirectory(path);
        Result<String> result = new Result<>();
        result.setOperationSuccess(true);
        if(d == null){
        	result.setOperationSuccess(false);
        	result.setOperationReturnMessage("Not found: " + path);
        }
        
        if(d.subdirectories.get(components[components.length-1])!=null){
        	result.setOperationReturnMessage(d.subdirectories.get(components[components.length-1]).toString());
        } else if(d.files.get(components[components.length-1])!= null){
        	result.setOperationReturnMessage(d.files.get(components[components.length-1]).toString());
        }
        else {
        	result.setOperationSuccess(false);
        	result.setOperationReturnMessage("Not found: " + path);
        }
        
    	return result;
        
    }

   

    public Directory parentDirectory(String path){
        String[] components = path.split("/");
        Directory current = rootDir;
        int i = 0;
        while(current != null && i < components.length-1){
            current = current.subdirectories.get(components[i]);
        }
        return current;
    }



	@Override
	public Result<Boolean> add(String path) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Result<Boolean> rm(String path) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Result<Boolean> mkdir(String path) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Result<Boolean> rmdir(String path) {
		// TODO Auto-generated method stub
		logger.info("Executing Command RMDIR, data: {}",path);
		return null;
	}



	@Override
	public Result<Boolean> mv(String path) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Result<Boolean> chmod(String path) {
		// TODO Auto-generated method stub
		return null;
	}
}
