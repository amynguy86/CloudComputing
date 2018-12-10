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
import cs6343.util.OperationNotSupportedException;
import cs6343.util.RedirectException;
import cs6343.util.Result;

public class CentralizedStorage extends Storage {
	public static Logger logger = LoggerFactory.getLogger(CentralizedStorage.class);
	private PhysicalInode root;

	public CentralizedStorage() {
		this("/");
	}

	public CentralizedStorage(String rootdir) {
		logger.info("Initializing Centralized Storage");
		root = new PhysicalInode();
		root.setName(rootdir);
		root.setParent(null);
		root.setPath(rootdir);
		MetaData metaData = new MetaData();
		metaData.setType(FileType.DIRECTORY);
		root.setMetaData(metaData);
	}
	
	public CentralizedStorage(PhysicalInode rootdir) {
		root = rootdir;
	}

	/*
	 * Need some path validation here as well
	 */
	public List<String> getPathAsList(String path) {

		String[] directories = path.split("/");
		List<String> list = new ArrayList<>();
		for (String d : directories) {
			list.add(d);
		}

		if (path.equals("/"))
			list.add(0, "/");
		else if (list.size() > 0) {
			if (list.get(0).length() == 0)
				list.set(0, "/"); // empty char
		} else {
			logger.error("Invalid Path: " + path);
			return null;
		}

		return list;
	}

	@Override
	public Result<String> ls(String path, boolean delay) {
		return ls(path, true, delay);
	}

	public PhysicalInode getRoot() {
		return this.root;
	}

	public void setRoot(PhysicalInode root) {
		this.root = root;
	}

	public Result<String> ls(String path, boolean unlockAtEnd, boolean delay) {
		logger.info("Executing Command ls, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		List<String> list = getPathAsList(path);

		if (list == null) {
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
		if (unlockAtEnd){
			if(delay){
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e){
					e.printStackTrace();;
				}
			}
			this.unLockRead(listInodes);
		}
		return result;
	}

	@Override
	public Result<String> rm(String path, boolean delay) {
		// TODO Auto-generated method stub
		return null;
	}

	public static boolean isPhysicalNode(Inode inode) {
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

					if (inode == null || (this.isPhysicalNode(inode) && ((PhysicalInode)inode).getMetaData().getType() == FileType.FILE)) {
						result.setOperationReturnMessage("Unable to find DIR:" + path.get(i));
						return result;
					}
					inode.readLock(LockOperation.LOCK);
					list.add(inode);
				}
			} catch (RedirectException ex) {
				result.setOperationReturnMessage(createRedirectMsg(inode));
				return result;

			}
		}
		result.setOperationSuccess(true);
		return result;
	}
	
	public static String createRedirectMsg(Inode inode) {
		String msg="REDIRECT TO SERVER:" + inode.getServerId() + "\n FOR PATH: " + inode.getPath();
		return msg;
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
	public Result<String> mkdir(String path, boolean delay) {
		logger.info("Executing Command MKDIR, data: {}", path);
		return createNode(path, FileType.DIRECTORY, true, delay);
	}

	@Override
	public Result<String> touch(String path, boolean delay) {
		logger.info("Executing Command touch, data: {}", path);
		return createNode(path, FileType.FILE, true, delay);
	}


	public Result<String> createNode(String path, FileType fileType, boolean unlockAtEnd, boolean delay) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);

		List<String> list = getPathAsList(path);
		if (list == null || list.size() < 2) {
			if (list != null)
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

			if (parentInode == null || (isPhysicalNode(parentInode) && ((PhysicalInode)parentInode).getMetaData().getType() == FileType.FILE))
				result.setOperationReturnMessage("DIR NOT FOUND:" + parentDir);
			else {
				try {
					parentInode.writeLock(LockOperation.LOCK);
					if (((PhysicalInode) parentInode).getChild(dirToCreate) == null) {
						PhysicalInode newInode = new PhysicalInode();
						newInode.setName(dirToCreate);
						newInode.setParent(parentInode);
						MetaData metaData = new MetaData();
						metaData.setType(fileType);
						newInode.setMetaData(metaData);
						((PhysicalInode) parentInode).addChild(newInode);
						newInode.setPath(normalizePath(parentInode.getPath())+"/"+dirToCreate);
						result.setOperationSuccess(true);
						result.setOperationReturnVal("Directory Created Successfull");
					}
					else {
						result.setOperationReturnMessage("Directory already exists");
					}
					if (unlockAtEnd)
						parentInode.writeLock(LockOperation.UNLOCK);
				} catch (RedirectException ex) {
					result.setOperationReturnMessage(createRedirectMsg(parentInode));
				}
			}
		} else

		{
			result.setOperationReturnMessage(result2.getOperationReturnMessage());
		}
		if (unlockAtEnd) {
			if (delay) {
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					;
				}
			}
			this.unLockRead(listInodes);
		}
		return result;
	}

	@Override
	public Result<String> rmdir(String path, boolean delay) {
		return rmdir(path, true, delay);
	}

	public static String normalizePath(String path) {
		String returnPath = path;
		if (path.length() > 0)
			returnPath = path.charAt(path.length() - 1) == '/' ? path.substring(0, path.length() - 1) : path;

		return returnPath;
	}

	public Result<String> rmdir(String path, boolean unlockAtEnd, boolean delay) {
		logger.info("Executing Command RMDIR, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);

		List<String> list = getPathAsList(path);
		if (list == null || list.size() < 2) {
			if (list != null)
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
					dirToDeleteInode = ((PhysicalInode) parentInode).getChild(dirToDelete);
					if (dirToDeleteInode != null) {
						if (!isPhysicalNode(dirToDeleteInode)) {
							//Todo send to other server
							result.setOperationReturnMessage("NOT IMPLEMENTED YET");
							
						} else {
							((PhysicalInode) parentInode).getChildren().remove(dirToDeleteInode.getName());
							result.setOperationSuccess(true);
							result.setOperationReturnVal("Directory Deleted Successfull");
						}
					} else {
						result.setOperationReturnMessage("Directory " + dirToDelete + " does not exist"); }
					if (unlockAtEnd)
						parentInode.writeLock(LockOperation.UNLOCK);
				} catch (RedirectException ex) {
					result.setOperationReturnMessage(createRedirectMsg(parentInode));
				} catch (OperationNotSupportedException ex) {
					result.setOperationReturnMessage(ex.getMessage());
					if (unlockAtEnd)
						parentInode.writeLock(LockOperation.UNLOCK);
				}
			}
		} else {
			result.setOperationReturnMessage(result2.getOperationReturnMessage());
		}
		if (unlockAtEnd) {
			if (delay) {
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					;
				}
			}
			this.unLockRead(listInodes);
		}
		return result;
	}

	@Override
	public Result<String> mv(String path, boolean delay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> chmod(String path, boolean delay) {
		return null;
	}

}
