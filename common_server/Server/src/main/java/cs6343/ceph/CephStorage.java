package cs6343.ceph;

import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import cs6343.RemoteLock;
import cs6343.centralized.CentralizedStorage;
import cs6343.data.PhysicalInode;
import cs6343.data.VirtualInode;
import cs6343.iface.Inode;
import cs6343.iface.Storage;
import cs6343.iface.Inode.LockOperation;
import cs6343.util.OperationNotSupportedException;
import cs6343.util.RedirectException;
import cs6343.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;

public class CephStorage extends Storage {
	public CentralizedStorage storage;
	CephServer cephServer;

	public boolean isRoot;
	String rootServerAddress;
	String parentPath;
	public ReentrantReadWriteLock cephReadWriteLock;

	@Override
	public Result<?> executeCommand(String command) {
		cephReadWriteLock.readLock().lock();
		Result<?> result;
		if (this.storage != null)
			result = super.executeCommand(command);
		else {
			result = new Result<>();
			result.setOperationSuccess(false);
			result.setOperationReturnMessage("There is no partition on this server");
		}
		cephReadWriteLock.readLock().unlock();
		return result;
	}

	public Result<?> executeCommandWithDelay(String command)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		cephReadWriteLock.readLock().lock();
		Result<?> result;
		if (this.storage != null)
			result = super.executeCommandWithDelay(command);
		else {
			result = new Result<>();
			result.setOperationSuccess(false);
			result.setOperationReturnMessage("There is no partition on this server");
		}
		cephReadWriteLock.readLock().unlock();
		return result;
	}

	public String getRootServerAddress() {
		return rootServerAddress;
	}

	public void setRootServerAddress(String rootServerAddress) {
		this.rootServerAddress = rootServerAddress;
	}

	public CephStorage(boolean isRoot, String rootServerAddress, CephServer cephServer) {
		if (isRoot) {
			logger.info("Initializing root Ceph Storage");
			storage = new CentralizedStorage("/");
			this.isRoot = true;
		} else {
			logger.info("Initializing Ceph Storage(not root)");
			// Initialize with a fake node
			storage = null;
			this.isRoot = false;
		}
		this.rootServerAddress = rootServerAddress;
		this.cephServer = cephServer;
		this.cephReadWriteLock = new ReentrantReadWriteLock();
	}

	public CephStorage(boolean isRoot, String rootServerAddress, int port) {
		this(isRoot, rootServerAddress, null);
		this.cephServer = new CephServer(this, port);
	}

	public void init(PhysicalInode rootNode) {
		rootNode.writeLock(LockOperation.LOCK);
		/*
		 * Since parent are all null in this tree(due to circular serialization issues
		 * in Gson), we'd have to parse the tree and fix parent references
		 */
		fixParentReferences(rootNode);
		this.storage = new CentralizedStorage(rootNode);
		String[] parts = rootNode.getPath().split("/");
		String parentPath = "";
		for (int i = 1; i < parts.length - 1; i++) {
			parentPath += "/" + parts[i];
		}
		this.parentPath = parentPath.length() == 0 ? "/" : parentPath;
		logger.info("Parent Path:" + this.parentPath);
		rootNode.writeLock(LockOperation.UNLOCK);
	}

	private void fixParentReferences(PhysicalInode node) {
		Stack<Inode> stack = new Stack<>();
		node.setParent(null);
		stack.push(node); // BFS would have been better
		while (stack.size() != 0) {
			Inode inode = stack.pop();
			if (CentralizedStorage.isPhysicalNode(inode)) {
				PhysicalInode tmp = (PhysicalInode) inode;
				for (Inode child : tmp.getChildren().values()) {
					child.setParent(tmp);
					stack.push(child);
				}
			}
		}
	}

	@Override
	public Result<String> partition(String data, boolean delay) {
		// TODO delay work
		logger.info("Executing Command partition, data: {}", data);
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		String[] tokenizer = data.split(" ");

		if (tokenizer.length != 2) {
			result.setOperationReturnMessage("Invalid parameter");
			return result;
		}

		String serverNo = tokenizer[1];
		String path = tokenizer[0];

		if (this.isRoot) {
			return this.partition(path, serverNo, true);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				RemoteLock lock = readLockParent();
				result = this.partition(isPathValid.getOperationReturnVal()[1], serverNo, true);
				lock.unlock();
			}
		}
		return result;
	}

	public Result<String> partition(String path, String serverToMove, boolean unlockAtEnd) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		String serverNo = serverToMove;
		List<String> list = storage.getPathAsList(path);

		if (list == null || list.size() < 2) {
			if (list != null)
				logger.error("Size of path must be atleast 2");

			result.setOperationReturnMessage("Invalid Path");
			return result;
		}

		Result<List<Inode>> result2 = this.storage.lockRead(list, list.size() - 2);
		List<Inode> listInodes = result2.getOperationReturnVal();
		if (result2.isOperationSuccess()) {
			String parentDir = list.get(list.size() - 2);
			String nodeToMove = list.get(list.size() - 1);
			// Lock The Parent
			Inode parentInode;
			Inode nodeToMoveInode;

			if (listInodes.size() > 0) {
				parentInode = ((PhysicalInode) listInodes.get(listInodes.size() - 1)).getChild(parentDir);
			} else {
				parentInode = this.storage.getRoot();
			}

			if (parentInode == null) {
				result.setOperationReturnMessage("DIR NOT FOUND:" + parentDir);
			} else {
				try {
					parentInode.writeLock(LockOperation.LOCK);
					nodeToMoveInode = ((PhysicalInode) parentInode).getChild(nodeToMove);
					if (nodeToMoveInode != null) {
						if (CentralizedStorage.isPhysicalNode(nodeToMoveInode)) {
							String json = PhysicalInode.toJson((PhysicalInode) nodeToMoveInode);
							nodeToMoveInode.setParent(parentInode);
							String prevName = nodeToMoveInode.getName();
							nodeToMoveInode.setName(nodeToMoveInode.getName());
							Result<String> rsltPartitionCmd;
							if ((rsltPartitionCmd = this.cephServer.sendCreatePartition(json, serverNo))
									.isOperationSuccess()) {
								nodeToMoveInode.setParent(parentInode);
								((PhysicalInode) parentInode).getChildren().remove(prevName);
								VirtualInode vInode = new VirtualInode(nodeToMoveInode, serverNo);
								((PhysicalInode) parentInode).addChild(vInode);
								result.setOperationSuccess(true);
							} else {
								nodeToMoveInode.setParent(parentInode);
								result = rsltPartitionCmd;
							}
						} else {
							result.setOperationReturnMessage("There is already a partiton here, its resides on server"
									+ nodeToMoveInode.getServerId());
						}
					} else {
						result.setOperationReturnMessage("Directory " + nodeToMove + " does not exist");
					}
					if (unlockAtEnd)
						parentInode.writeLock(LockOperation.UNLOCK);
				} catch (RedirectException ex) {
					result.setOperationReturnMessage(CentralizedStorage.createRedirectMsg(parentInode));
				}
			}
		} else {
			result.setOperationReturnMessage(result2.getOperationReturnMessage());
		}
		if (unlockAtEnd)
			this.storage.unLockRead(listInodes);
		return result;
	}

	public Result<String> cephServerRqst(CephServer.ServerRequest serverRqst) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Result<String> result;
		this.cephReadWriteLock.writeLock().lock();
		try {
			result = (Result<String>) CephServer.class.getMethod(serverRqst.getCommand(), String.class)
					.invoke(cephServer, serverRqst.getData());

		} catch (Exception ex) {
			ex.printStackTrace();
			result = new Result<>();
			result.setOperationSuccess(false);
			result.setOperationReturnMessage(ex.getMessage());
		}
		this.cephReadWriteLock.writeLock().unlock();
		return result;
	}

	/*
	 * Format of path for server containing a partition is "/a/b/c%d/e/f where the
	 * path after % resides on this server For the root server it is just like a
	 * normal path
	 */

	public Result<String[]> validateCephPath(String path) {
		Result<String[]> result = new Result<>();
		result.setOperationSuccess(false);
		String cephPath[] = path.split("%", 2);

		if (cephPath.length != 2 && path.charAt(path.length() - 1) != '%') {
			result.setOperationReturnMessage(
					"Invalid Path, CephPath to a server containing a partition is something like /a/b/c%d/e/f");
		} else if (!cephPath[0].equals(this.storage.getRoot().getPath())) {
			logger.error(this.storage.getRoot().getPath());
			result.setOperationReturnMessage("Path " + cephPath[0] + " does not reside on this server");
		} else {
			cephPath[1] = cephPath.length < 2 || cephPath[1].length() == 0 ? this.storage.getRoot().getName()
					: this.storage.getRoot().getName() + "/" + cephPath[1];
			result.setOperationSuccess(true);
			result.setOperationReturnVal(cephPath);
		}
		return result;
	}

	@Override
	public Result<String> ls(String path, boolean delay) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.ls(path, delay);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				// todo lock remotely first
				RemoteLock lock = readLockParent();
				result = this.storage.ls(isPathValid.getOperationReturnVal()[1], delay);
				lock.unlock();
			}
		}
		return result;
	}

	@Override
	public Result<String> mkdir(String path, boolean delay) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.mkdir(path, delay);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				if (isPathValid.getOperationReturnVal()[1].equals(this.storage.getRoot().getName())) {
					result.setOperationReturnMessage("Invalid Path");
				} else {
					RemoteLock lock = readLockParent();
					result = this.storage.mkdir(isPathValid.getOperationReturnVal()[1], delay);
					lock.unlock();
				}
			}
		}
		return result;
	}

	@Override
	public Result<String> rmdir(String path, boolean delay) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.rmdir(path, this.cephServer, delay);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				if (isPathValid.getOperationReturnVal()[1].equals(this.storage.getRoot().getName())) {
					result.setOperationReturnMessage("Invalid Path");
				} else {
					RemoteLock lock = readLockParent();
					result = this.storage.rmdir(isPathValid.getOperationReturnVal()[1], this.cephServer, delay);
					lock.unlock();
				}
			}
		}
		return result;
	}

	@Override
	public Result<String> chmod(String path, boolean delay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> mv(String path, boolean delay) {
		// TODO Auto-generated method stub
		return null;
	}

	public RemoteLock readLockParent() {
		RemoteLock lock = new RemoteLock(this.rootServerAddress.split(":")[0],
				Integer.parseInt(this.rootServerAddress.split(":")[1]) + 1);
		lock.lock(this.parentPath);
		return lock;
	}

	@Override
	public Result<String> touch(String path, boolean delay) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.touch(path, delay);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				if (isPathValid.getOperationReturnVal()[1].equals(this.storage.getRoot().getName())) {
					result.setOperationReturnMessage("Invalid Path");
				} else {
					RemoteLock lock = readLockParent();
					result = this.storage.touch(isPathValid.getOperationReturnVal()[1], delay);
					lock.unlock();
				}
			}
		}
		return result;
	}

	@Override
	public Result<String> rm(String path, boolean delay) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.rm(path, delay);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				if (isPathValid.getOperationReturnVal()[1].equals(this.storage.getRoot().getName())) {
					result.setOperationReturnMessage("Invalid Path");
				} else {
					RemoteLock lock = readLockParent();
					result = this.storage.rm(isPathValid.getOperationReturnVal()[1], delay);
					lock.unlock();
				}
			}
		}
		return result;
	}

	public void print() {
		this.storage.print();
		
	}
	
	
}
