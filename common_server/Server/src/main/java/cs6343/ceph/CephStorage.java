package cs6343.ceph;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import cs6343.centralized.CentralizedStorage;
import cs6343.data.PhysicalInode;
import cs6343.data.VirtualInode;
import cs6343.iface.Inode;
import cs6343.iface.Storage;
import cs6343.iface.Inode.LockOperation;
import cs6343.util.OperationNotSupportedException;
import cs6343.util.RedirectException;
import cs6343.util.Result;

public class CephStorage extends Storage {
	public CentralizedStorage storage;
	CephServer cephServer;

	public boolean isRoot;
	String rootServerAddress;

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
			this.isRoot=true;
		} else {
			logger.info("Initializing Ceph Storage(not root)");
			//Initialize with a fake node
			storage = null; 
			this.isRoot=false;
		}
		this.rootServerAddress = rootServerAddress;
		this.cephServer = cephServer;
	}

	public CephStorage(boolean isRoot, String rootServerAddress) {
		this(isRoot, rootServerAddress, null);
		this.cephServer = new CephServer(this);
	}

	public void init(PhysicalInode rootNode) {
		rootNode.writeLock(LockOperation.LOCK);
		/*
		 * Since parent are all null in this tree(due to circular serialization issues
		 * in Gson), we'd have to parse the tree and fix parent references
		 */
		fixParentReferences(rootNode);
		this.storage = new CentralizedStorage(rootNode);
		rootNode.writeLock(LockOperation.UNLOCK);
	}

	private void fixParentReferences(PhysicalInode node) {
		Stack<Inode> stack = new Stack<>();
		node.setParent(null);
		stack.push(node); // BFS would have been better
		while (stack.size() != 0) {
			PhysicalInode tmp = (PhysicalInode) stack.pop();
			for (Inode child : tmp.getChildren().values()) {
				child.setParent(tmp);
				stack.push(child);
			}
		}
	}

	@Override
	public Result<String> partition(String data) {
		logger.info("Executing Command partition, data: {}", data);
		return partition(data, true);
	}

	public Result<String> partition(String data, boolean unlockAtEnd) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		String[] tokenizer = data.split(" ");

		if (tokenizer.length != 2) {
			result.setOperationReturnMessage("Invalid parameter");
			return result;
		}

		String serverNo = tokenizer[1];
		String path = tokenizer[0];
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
							if (this.cephServer.sendCreatePartition(json, serverNo)) {
								nodeToMoveInode.setParent(parentInode);
								((PhysicalInode) parentInode).getChildren().remove(nodeToMoveInode.getName());
								VirtualInode vInode = new VirtualInode(nodeToMoveInode, serverNo);
								((PhysicalInode) parentInode).addChild(vInode);
								result.setOperationSuccess(true);
							} else {
								nodeToMoveInode.setParent(parentInode);
								result.setOperationSuccess(false);
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
					result.setOperationReturnMessage("REDIRECT TO SERVER:" + parentInode.getServerId());
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
		return (Result<String>) CephServer.class.getMethod(serverRqst.getCommand(), String.class).invoke(cephServer,
				serverRqst.getData());
	}

	public Result<String> sendRedirectRequest(String serverIp) {
		return null;
	}

	/*
	 * Format of path for server containing a partition is "/a/b/c%d/e/f where the
	 * path after % resides on this server For the root server it is just like a
	 * normal path
	 */

	public Result<String[]> validateCephPath(String path) {
		Result<String[]> result = new Result<>();
		result.setOperationSuccess(false);
		String cephPath[] = path.split("%");
	
		if (cephPath.length != 2 && cephPath[0].charAt(cephPath.length-1)!='%') {
			result.setOperationReturnMessage(
					"Invalid Path, CephPath to a server containing a partition is something like /a/b/c%d/e/f");
		} else if (!cephPath[0].equals(this.storage.getRoot().getPath())) {
			result.setOperationReturnMessage("Path " + cephPath[0] + " does not reside on this server");
		} else {
			cephPath[1] = cephPath.length<2 || cephPath[1].length() == 0 ? this.storage.getRoot().getName()
					: this.storage.getRoot().getName() + "/" + cephPath[1];
			result.setOperationSuccess(true);
			result.setOperationReturnVal(cephPath);
		}
		return result;
	}

	@Override
	public Result<String> ls(String path) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.ls(path);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				// todo lock remotely first
				result = this.storage.ls(isPathValid.getOperationReturnVal()[1]);
			}
		}
		return result;
	}

	
	@Override
	public Result<String> mkdir(String path) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.mkdir(path);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				if (isPathValid.getOperationReturnVal()[1].equals(this.storage.getRoot().getName())) {
					result.setOperationReturnMessage("Invalid Path");
				} else {
					// todo lock remotely first
					result = this.storage.mkdir(isPathValid.getOperationReturnVal()[1]);
				}
			}
		}
		return result;
	}

	@Override
	public Result<String> rmdir(String path) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.rmdir(path);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				if (isPathValid.getOperationReturnVal()[1].equals(this.storage.getRoot().getName())) {
					result.setOperationReturnMessage("Invalid Path");
				} else {
					// todo lock remotely first
					result = this.storage.rmdir(isPathValid.getOperationReturnVal()[1]);
				}
			}
		}
		return result;
	}

	@Override
	public Result<String> chmod(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> rm(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> mv(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> touch(String path) {
		Result<String> result = new Result<>();
		result.setOperationSuccess(false);
		if (this.isRoot) {
			return this.storage.touch(path);
		} else {
			Result<String[]> isPathValid = validateCephPath(path);
			if (!isPathValid.isOperationSuccess()) {
				result.setOperationReturnMessage(isPathValid.getOperationReturnMessage());
			} else {
				if (isPathValid.getOperationReturnVal()[1].equals(this.storage.getRoot().getName())) {
					result.setOperationReturnMessage("Invalid Path");
				} else {
					// todo lock remotely first
					result = this.storage.touch(isPathValid.getOperationReturnVal()[1]);
				}
			}
		}
		return result;
	}
}
