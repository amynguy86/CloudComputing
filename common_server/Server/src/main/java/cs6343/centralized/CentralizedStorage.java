package cs6343.centralized;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs6343.data.FileType;
import cs6343.data.PhysicalInode;
import cs6343.data.MetaData;
import cs6343.iface.Inode;
import cs6343.iface.Inode.LockOperation;
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
		List<String> list = new ArrayList<>();
		for (String d : directories) {
			list.add(d);
		}

		if (list.size() == 0)
			list.add(0, "/");
		else if (list.get(0).length() == 0)
			list.set(0, "/"); // empty char

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
			PhysicalInode inode = null;
			if (path.get(0) == "/")
				inode = this.root;
			else {
				// throw error, in ceph case we may look into hash to find the starting of the
				// branch
				logger.error("Must start with / in the centralized version");
			}

			if (numNodesToLock > path.size()) {
				// throw error
			}

			result.setOperationSuccess(false);
			result.setOperationReturnVal(list);

			inode.readLock(LockOperation.LOCK);
			list.add(inode);

			for (int i = 1; i < numNodesToLock; i++) {
				inode = inode.getChild(path.get(i));
				if (inode == null || inode.isDeleted()) {
					result.setOperationReturnMessage("Unable to find DIR:" + path.get(i));
					return result;
				}
				inode.readLock(LockOperation.LOCK);
				list.add(inode);
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
			inode.readLock(LockOperation.UNLOCK);
		}
		return true;
	}

	@Override
	public Result<String> mkdir(String path) {
		logger.info("Executing Command MKDIR, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);

		String[] directories = path.split("/");
		List<String> list = new ArrayList<>();

		for (String d : directories) {
			list.add(d);
		}

		if (list.get(0).length() == 0)
			list.set(0, "/");

		if (list.size() < 2) {
			// throw error
			logger.error("Size of path must be atleast 2");
			return null;
		}
		Result<List<Inode>> result2 = this.lockRead(list, list.size() - 2);
		List<Inode> listInodes = result2.getOperationReturnVal();

		if (result2.isOperationSuccess()) {
			String parentDir = list.get(list.size() - 2);
			String dirToCreate = list.get(list.size() - 1);
			// Lock The Parent
			PhysicalInode parentInode;
			if (listInodes.size() > 0)
				parentInode = ((PhysicalInode) listInodes.get(listInodes.size() - 1)).getChild(parentDir);
			else
				parentInode = this.root;

			if (parentInode == null) {
				result.setOperationReturnMessage("DIR NOT FOUND:" + parentDir);
			} else {
				parentInode.writeLock(LockOperation.LOCK);
				PhysicalInode newInode = new PhysicalInode();
				newInode.setName(dirToCreate);
				newInode.setParent(parentInode);
				MetaData metaData = new MetaData();
				metaData.setType(FileType.DIRECTORY);
				newInode.setMetaData(metaData);
				parentInode.addChild(newInode);
				result.setOperationSuccess(true);
				result.setOperationReturnVal("Directory Created Successfull");
				parentInode.writeLock(LockOperation.UNLOCK);
			}
		} else {
			result.setOperationReturnMessage(result2.getOperationReturnMessage());
		}

		this.unLockRead(listInodes);
		return result;
	}

	@Override
	public Result<String> rmdir(String path) {
		logger.info("Executing Command RMDIR, data: {}", path);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);

		String[] directories = path.split("/");
		List<String> list = new ArrayList<>();

		for (String d : directories) {
			list.add(d);
		}

		if (list.get(0).length() == 0)
			list.set(0, "/");

		if (list.size() < 2) {
			// throw error
			logger.error("Size of path must be atleast 2");
			return null;
		}
		Result<List<Inode>> result2 = this.lockRead(list, list.size() - 2);
		List<Inode> listInodes = result2.getOperationReturnVal();

		if (result2.isOperationSuccess()) {
			String parentDir = list.get(list.size() - 2);
			String dirToDelete = list.get(list.size() - 1);
			// Lock The Parent
			PhysicalInode parentInode;
			PhysicalInode dirToDeleteInode;

			if (listInodes.size() > 0) {
				parentInode = ((PhysicalInode) listInodes.get(listInodes.size() - 1)).getChild(parentDir);
			} else {
				parentInode = this.root;
			}

			if (parentInode == null) {
				result.setOperationReturnMessage("DIR NOT FOUND:" + parentDir);
			} else {
				parentInode.writeLock(LockOperation.LOCK);

				dirToDeleteInode = parentInode.getChild(dirToDelete);
				if (dirToDeleteInode != null) {
					dirToDeleteInode.readLock(LockOperation.LOCK);
					parentInode.getChildren().remove(dirToDeleteInode.getName());
					result.setOperationSuccess(true);
					result.setOperationReturnVal("Directory Deleted Successfull");
					dirToDeleteInode.readLock(LockOperation.UNLOCK);
				} else {
					result.setOperationReturnVal("Directory " + dirToDeleteInode.getName() + " does not exist");
				}

				parentInode.writeLock(LockOperation.UNLOCK);
			}
		} else {
			result.setOperationReturnMessage(result2.getOperationReturnMessage());
		}

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
