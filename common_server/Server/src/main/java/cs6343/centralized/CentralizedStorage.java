package cs6343.centralized;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs6343.data.FileType;
import cs6343.data.Inode;
import cs6343.data.MetaData;
import cs6343.iface.Storage;
import cs6343.util.Result;

public class CentralizedStorage extends Storage {
	public static Logger logger = LoggerFactory.getLogger(CentralizedStorage.class);

	public CentralizedStorage() {
		super();
	}

	@Override
	public Result<String> ls(String path) {
		logger.info("Executing Command ls, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		
		String[] directories = path.split("/");
		Inode inode=this.root;
		
		for(int i=1;i<directories.length;i++) {
			String dir=directories[i];
			
			inode=inode.getChild(dir);
			if(inode==null) {
				result.setOperationReturnMessage("Unable to find DIR:"+dir);
				return result;
			}
		}
		
		String resultStr=inode.getChildren().values().stream().map(node->node.toString()).collect(Collectors.joining("\n"));
		result.setOperationSuccess(true);
    	result.setOperationReturnMessage(resultStr);
		return result;
	}

	public void parentDirectory(String path) {
		/*
		 * String[] components = path.split("/"); Directory current = rootDir; int i =
		 * 0; while(current != null && i < components.length-1){ current =
		 * current.subdirectories.get(components[i]); } return current;
		 */
	}

	@Override
	public Result<String> add(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> rm(String path) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Result<String> mkdir(String path) {
		logger.info("Executing Command MKDIR, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		
		String[] directories = path.split("/");
		Inode inode=this.root;
		
		for(int i=1;i<directories.length-1;i++) {
			String dir=directories[i];
			
			inode=inode.getChild(dir);
			if(inode==null) {
				result.setOperationReturnMessage("Unable to find DIR:"+dir);
				return result;
			}
		}
		
		if(inode.getChild(directories[directories.length-1])!=null) {
			result.setOperationReturnMessage("Directory already Exists");
			return result;
		}
		
		Inode newInode = new Inode();
    	newInode.setName(directories[directories.length-1]);
    	newInode.setParent(inode);
    	MetaData metaData = new MetaData();
    	metaData.setType(FileType.DIRECTORY);
    	newInode.setMetaData(metaData);
    	inode.addChild(newInode);
    	result.setOperationSuccess(true);
    	result.setOperationReturnMessage("Directory Created Successfull");
		return result;
	}
	

	@Override
	public Result<String> rmdir(String path) {
		logger.info("Executing Command RMDIR, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		
		String[] directories = path.split("/");
		Inode inode=this.root; 
		for(int i=1;i<directories.length-1;i++) {
			String dir=directories[i];
			
			inode=inode.getChild(dir);
			if(inode==null) {
				result.setOperationReturnMessage("Unable to find DIR:"+dir);
				return result;
			}
			
			//todo else write lock!
		}
		
		inode=inode.getChild(directories[directories.length-1]);
		if(inode==null) {
			result.setOperationReturnMessage("Directory does not Exists");
			return result;
		}
		//todo else write lock
		
		inode.getParent().getChildren().remove(inode.getName());
    	result.setOperationSuccess(true);
    	result.setOperationReturnMessage("Directory Deleted Successfull");
    	
    	//todo loop second time and unlock everything
		return result;
	}

	@Override
	public Result<String> mv(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> chmod(String path) {
		// TODO Auto-generated method stub
		return null;
	}
}
