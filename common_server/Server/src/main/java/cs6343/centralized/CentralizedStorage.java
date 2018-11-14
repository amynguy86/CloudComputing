package cs6343.centralized;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs6343.data.FileType;
import cs6343.data.PhysicalInode;
import cs6343.data.MetaData;
import cs6343.iface.Inode;
import cs6343.iface.Inode.LockOperation;
import cs6343.iface.Storage;
import cs6343.util.RedirectException;
import cs6343.util.Result;

public class CentralizedStorage extends Storage {
	public static Logger logger = LoggerFactory.getLogger(CentralizedStorage.class);

	public CentralizedStorage() {
		super();
	}

	public CentralizedStorage(String rootdir) {
		root = new PhysicalInode();
		root.setName(rootdir);
		root.setParent(null);
		root.setPath(rootdir);
		MetaData metaData = new MetaData();
		metaData.setType(FileType.DIRECTORY);
		root.setMetaData(metaData);
	}
	
	/*
	 * Need some path validation here as well
	 */
	private List<String> getPathAsList(String path) {
		
		String[] directories = path.split("/");
		List<String> list = new ArrayList<>();
		for (String d : directories) {
			list.add(d);
		}
		
		if (path.equals("/"))
			list.add(0, "/");
		else if (list.size() > 0 && list.get(0).length() == 0)
			list.set(0, "/"); // empty char
		else {
			logger.error("Invalid Path: " + path);
			return null;
		}
		
		return list;
	}
	@Override
	public Result<String> ls(String path) {
		return ls(path,true);
	}
	
	public Inode getRoot() {
		return this.root;
	}
	
	public Result<String> ls(String path,boolean unlockAtEnd) {
		logger.info("Executing Command ls, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		List<String> list  = getPathAsList(path);
		
		if(list==null) {
			result.setOperationReturnMessage("Invalid Path");
			return result;
		}
		
		Result<List<Inode>> result2 = this.lockRead(list, list.size());
		List<Inode> listInodes = result2.getOperationReturnVal();
		if (result2.isOperationSuccess()) {
			PhysicalInode nodeToRead = (PhysicalInode) listInodes.get(listInodes.size() - 1);
			String resultStr = nodeToRead.getChildren().values().stream().map(node -> node.toString())
					.collect(Collectors.joining("\n"));
			result.setOperationReturnVal(resultStr);
			result.setOperationSuccess(true);
		} else {
			result.setOperationReturnMessage(result2.getOperationReturnMessage());
		}
		if(unlockAtEnd)
			this.unLockRead(listInodes);
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

	private boolean isPhysicalNode(Inode inode) {
		if (inode.getClass().equals(PhysicalInode.class)) {
			return true;
		} else
			return false;
	}

	/*
	 * Returns List of Inodes that were Locked including the startingInode. /a/b/c =
	 * {/,a,b,c} Path: a list of directories /a/b/c => {/,a,b,c} numNodesToLock: how
	 * many nodes to lock starting the from the first element in the path
	 */
	public Result<List<Inode>> lockRead(List<String> path, int numNodesToLock) {
		List<Inode> list = new LinkedList<>();
		Result<List<Inode>> result = new Result<>();
		result.setOperationReturnVal(list);
		if (numNodesToLock > 0) {
			Inode inode = this.root;

			if (numNodesToLock > path.size()) {
				result.setOperationReturnMessage(
						"numNodesToLock: " + numNodesToLock + "is greater than path size: " + path.size());
				return result;
			}

			result.setOperationSuccess(false);
			result.setOperationReturnVal(list);

			try {
				inode.readLock(LockOperation.LOCK);
				list.add(inode);

				if (!inode.getName().equals(path.get(0))) {
					result.setOperationReturnMessage("First directory in path: " + path.get(0)
							+ " does not match the starting node of this server: " + this.root.getName());
					return result;

				}

				for (int i = 1; i < numNodesToLock; i++) {

					inode = ((PhysicalInode) inode).getChild(path.get(i));

					if (inode == null) {
						result.setOperationReturnMessage("Unable to find DIR:" + path.get(i));
						return result;
					}
					inode.readLock(LockOperation.LOCK);
					list.add(inode);

				}
			} catch (RedirectException ex) {
				result.setOperationReturnMessage("REDIRECT TO SERVER:" + inode.getServerId());
				return result;

			}
		}
		result.setOperationSuccess(true);
		return result;
	}	

	/*
	 * Return list of unlockedNodes, these nodes MUST have been locked!
	 */
	public boolean unLockRead(List<Inode> listParam) {
		for (Inode inode : listParam) {
			try {
				inode.readLock(LockOperation.UNLOCK);
			} catch (RedirectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public Result<String> mkdir(String path){
		return mkdir(path,true);
	}
	
	public Result<String> mkdir(String path,boolean unlockAtEnd) {
		logger.info("Executing Command MKDIR, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		
		List<String> list = getPathAsList(path); 
		if (list==null ||  list.size() < 2) {
			if(list!=null)
				logger.error("Size of path must be atleast 2");
			
			result.setOperationReturnMessage("Invalid Path");
			return result;
		}
		Result<List<Inode>> result2 = this.lockRead(list, list.size() - 2);
		List<Inode> listInodes = result2.getOperationReturnVal();

		if (result2.isOperationSuccess()) {
			String parentDir = list.get(list.size() - 2);
			String dirToCreate = list.get(list.size() - 1);
			// Lock The Parent
			Inode parentInode;
			if (listInodes.size() > 0)
				parentInode = ((PhysicalInode) listInodes.get(listInodes.size() - 1)).getChild(parentDir);
			else
				parentInode = this.root;

			if (parentInode == null) {
				result.setOperationReturnMessage("DIR NOT FOUND:" + parentDir);
			} else {
				try {
				parentInode.writeLock(LockOperation.LOCK);
				PhysicalInode newInode = new PhysicalInode();
				newInode.setName(dirToCreate);
				newInode.setParent(parentInode);
				MetaData metaData = new MetaData();
				metaData.setType(FileType.DIRECTORY);
				newInode.setMetaData(metaData);
				((PhysicalInode)parentInode).addChild(newInode);
				newInode.setPath(path);
				result.setOperationSuccess(true);
				result.setOperationReturnVal("Directory Created Successfull");
				if(unlockAtEnd)
					parentInode.writeLock(LockOperation.UNLOCK);
				}
				catch(RedirectException ex) {
					result.setOperationReturnMessage("REDIRECT TO SERVER:" + parentInode.getServerId());
				}
			}
		} else {
			result.setOperationReturnMessage(result2.getOperationReturnMessage());
		}
		if(unlockAtEnd)
			this.unLockRead(listInodes);
		return result;
	}

	@Override
	public Result<String> rmdir(String path) {
		return rmdir(path,true);
	}
	
	public Result<String> rmdir(String path,boolean unlockAtEnd) {
		logger.info("Executing Command RMDIR, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		
		List<String> list = getPathAsList(path); 
		if (list==null ||  list.size() < 2) {
			if(list!=null)
				logger.error("Size of path must be atleast 2");
			
			result.setOperationReturnMessage("Invalid Path");
			return result;
		}
		
		Result<List<Inode>> result2 = this.lockRead(list, list.size() - 2);
		List<Inode> listInodes = result2.getOperationReturnVal();

		if (result2.isOperationSuccess()) {
			String parentDir = list.get(list.size() - 2);
			String dirToDelete = list.get(list.size() - 1);
			// Lock The Parent
			Inode parentInode;
			Inode dirToDeleteInode;

			if (listInodes.size() > 0) {
				parentInode = ((PhysicalInode) listInodes.get(listInodes.size() - 1)).getChild(parentDir);
			} else {
				parentInode = this.root;
			}

			if (parentInode == null) {
				result.setOperationReturnMessage("DIR NOT FOUND:" + parentDir);
			} else {
				try {
				parentInode.writeLock(LockOperation.LOCK);
				dirToDeleteInode = ((PhysicalInode)parentInode).getChild(dirToDelete);
				if (dirToDeleteInode != null) {
					if(!isPhysicalNode(dirToDeleteInode)) {
						//todo make a remote request to delete the partiion
					}
					((PhysicalInode)parentInode).getChildren().remove(dirToDeleteInode.getName());
					result.setOperationSuccess(true);
					result.setOperationReturnVal("Directory Deleted Successfull");
				} else {
					result.setOperationReturnVal("Directory " + dirToDelete + " does not exist");
				}
				if(unlockAtEnd)
					parentInode.writeLock(LockOperation.UNLOCK);
				}
				catch(RedirectException ex) {
					result.setOperationReturnMessage("REDIRECT TO SERVER:" + parentInode.getServerId());
				}
			}
		} else {
			result.setOperationReturnMessage(result2.getOperationReturnMessage());
		}
		if(unlockAtEnd)
			this.unLockRead(listInodes);
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
