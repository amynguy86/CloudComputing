package server;

import org.junit.Assert;
import org.junit.Test;

import cs6343.centralized.CentralizedStorage;
import cs6343.data.PhysicalInode;
import cs6343.iface.Inode;
import cs6343.util.Result;

public class CentralizedStorageTest {

	@Test
	public void testOperations() {
		// build the tree!
		CentralizedStorage storage = initTree();
		// TestLS
		Result<String> result = storage.ls("/ab");
		validateOp(result);
		String resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "Inode [name=cd]\nInode [name=ef]");

		// Test RMDIR
		result = storage.rmdir("/ab/cd");
		validateOp(result);

		result = storage.ls("/ab");
		validateOp(result);
		resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "Inode [name=ef]");

		result = storage.rmdir("/ab/ef");
		validateOp(result);

		result = storage.ls("/ab");
		validateOp(result);
		resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "");
	}

	public void validateOp(Result result) {
		Assert.assertEquals(result.isOperationSuccess(), true);
	}

	public CentralizedStorage initTree() {
		CentralizedStorage storage = new CentralizedStorage();
		validateOp(storage.mkdir("/ab"));
		validateOp(storage.mkdir("/cd"));
		validateOp(storage.mkdir("/cd/ab"));
		validateOp(storage.mkdir("/ab/cd"));
		validateOp(storage.mkdir("/ab/cd/ef"));
		validateOp(storage.mkdir("/ab/cd/gh"));
		validateOp(storage.mkdir("/ab/ef"));
		return storage;
	}

	@Test
	public void testLocking() {
		// LS locking
		CentralizedStorage storage = initTree();
		String path = "/ab/cd/ef";
		storage.ls(path, false);

		path = "ab/cd/ef";
		String[] p = path.split("/");
		PhysicalInode inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(1, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = inode.getChild(dir);
		}

		// RMDIR locking
		storage = initTree();
		path = "/ab/cd/ef";

		storage.rmdir(path, false);

		path = "ab";
		p = path.split("/");
		inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(1, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = inode.getChild(dir);
		}

		PhysicalInode node = getDirInPath("/ab/cd", 2, storage.getRoot());
		System.out.println(node.getName());
		Assert.assertEquals(1, node.getLock().getWriteHoldCount());
		Assert.assertEquals(0, node.getLock().getReadHoldCount());
		Assert.assertNull((getDirInPath("/ab/cd/ef", 3, storage.getRoot())));

		// MKDIR locking
		storage = initTree();
		path = "/ab/cd/ef/i";

		storage.rmdir(path, false);

		path = "ab/cd";
		p = path.split("/");
		inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(1, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = inode.getChild(dir);
		}

		node = getDirInPath("/ab/cd/ef", 3, storage.getRoot());
		Assert.assertEquals(1, node.getLock().getWriteHoldCount());
		Assert.assertEquals(0, node.getLock().getReadHoldCount());
		Assert.assertNotNull((getDirInPath("/ab/cd/ef/i", 3, storage.getRoot())));
	}

	@Test
	public void testUnlock() {
		// LS Unlocking
		CentralizedStorage storage = initTree();
		String path = "/ab/cd/ef";
		storage.ls(path);

		path = "ab/cd/ef";
		String[] p = path.split("/");
		PhysicalInode inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(0, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = inode.getChild(dir);
		}

		// RMDIR locking
		storage = initTree();
		path = "/ab/cd/ef";

		storage.rmdir(path);

		path = "ab/cd";
		p = path.split("/");
		inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(0, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = inode.getChild(dir);
		}

		Assert.assertNull((getDirInPath("/ab/cd/ef", 3, storage.getRoot())));

		// MKDIR locking
		storage = initTree();
		path = "/ab/cd/ef/i";

		storage.rmdir(path);

		path = "ab/cd/ef/i";
		p = path.split("/");
		Assert.assertNotNull((getDirInPath("/ab/cd/ef/i", 3, storage.getRoot())));
		inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(0, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = inode.getChild(dir);
		}

	}

	public PhysicalInode getDirInPath(String path, int dirIdx, Inode inode) {
		String[] p = path.split("/");
		p[0] = "/";
		PhysicalInode nodeToReturn = (PhysicalInode) inode;
		for (int i = 1; i < dirIdx + 1; i++) {
			nodeToReturn = nodeToReturn.getChild(p[i]);
		}
		return nodeToReturn;

	}

}
