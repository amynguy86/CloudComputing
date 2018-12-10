package server;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import cs6343.centralized.CentralizedStorage;
import cs6343.data.FileType;
import cs6343.data.PhysicalInode;
import cs6343.data.VirtualInode;
import cs6343.iface.Inode;
import cs6343.iface.Inode.LockOperation;
import cs6343.util.Result;

public class CentralizedStorageTest {

	@Test
	public void testOperations() {
		// build the tree!
		CentralizedStorage storage = initTree();
		// TestLS
		Result<String> result = storage.ls("/ab", false);
		validateOp(result);
		String resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "Inode [name=cd]\nInode [name=ef]");

		// Test RMDIR
		result = storage.rmdir("/ab/cd", false);
		validateOp(result);

		result = storage.ls("/ab", false);
		validateOp(result);
		resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "Inode [name=ef]");

		result = storage.rmdir("/ab/ef", false);
		validateOp(result);

		result = storage.ls("/ab", false);
		validateOp(result);
		resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "");
		
		//TEST Touch
		result = storage.touch("/ab/file", false);
		validateOp(result);

		//Mkdir FAIL because cant create a something in a file
		result = storage.mkdir("/ab/file/ab", false);
		Assert.assertEquals(result.isOperationSuccess(), false);
	}

	public void validateOp(Result result) {
		Assert.assertEquals(result.isOperationSuccess(), true);
	}

	public CentralizedStorage initTree() {
		CentralizedStorage storage = new CentralizedStorage();
		validateOp(storage.mkdir("/ab", false));
		validateOp(storage.mkdir("/cd", false));
		validateOp(storage.mkdir("/cd/ab", false));
		validateOp(storage.mkdir("/ab/cd", false));
		validateOp(storage.mkdir("/ab/cd/ef", false));
		validateOp(storage.mkdir("/ab/cd/gh", false));
		validateOp(storage.mkdir("/ab/ef", false));
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
			inode = (PhysicalInode) inode.getChild(dir);
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
			inode = (PhysicalInode) inode.getChild(dir);
		}

		PhysicalInode node = getDirInPath("/ab/cd", 2, storage.getRoot());
		System.out.println(node.getName());
		Assert.assertEquals(1, node.getLock().getWriteHoldCount());
		Assert.assertEquals(0, node.getLock().getReadHoldCount());
		Assert.assertNull((getDirInPath("/ab/cd/ef", 3, storage.getRoot())));

		// MKDIR locking
		storage = initTree();
		path = "/ab/cd/ef/i";

		storage.createNode(path, FileType.DIRECTORY, false, false);

		path = "ab/cd";
		p = path.split("/");
		inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(1, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = (PhysicalInode) inode.getChild(dir);
		}

		node = getDirInPath("/ab/cd/ef", 3, storage.getRoot());
		Assert.assertEquals(1, node.getLock().getWriteHoldCount());
		Assert.assertEquals(0, node.getLock().getReadHoldCount());
		Assert.assertNotNull((getDirInPath("/ab/cd/ef/i", 3, storage.getRoot())));
		Assert.assertEquals(FileType.DIRECTORY,(getDirInPath("/ab/cd/ef/i", 4, storage.getRoot())).getMetaData().getType());
		
		//Touch Locking
		storage = initTree();
		path = "/ab/cd/ef/i";

		storage.createNode(path, FileType.FILE, false, false);

		path = "ab/cd";
		p = path.split("/");
		inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(1, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = (PhysicalInode) inode.getChild(dir);
		}

		node = getDirInPath("/ab/cd/ef", 3, storage.getRoot());
		Assert.assertEquals(1, node.getLock().getWriteHoldCount());
		Assert.assertEquals(0, node.getLock().getReadHoldCount());
		Assert.assertNotNull((getDirInPath("/ab/cd/ef/i", 3, storage.getRoot())));
		Assert.assertEquals(FileType.FILE,(getDirInPath("/ab/cd/ef/i", 4, storage.getRoot())).getMetaData().getType());
		
	}

	@Test
	public void testUnlock() {
		// LS Unlocking
		CentralizedStorage storage = initTree();
		String path = "/ab/cd/ef";
		storage.ls(path, false);

		path = "ab/cd/ef";
		String[] p = path.split("/");
		PhysicalInode inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(0, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = (PhysicalInode) inode.getChild(dir);
		}

		// RMDIR locking
		storage = initTree();
		path = "/ab/cd/ef";

		storage.rmdir(path, false);

		path = "ab/cd";
		p = path.split("/");
		inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(0, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = (PhysicalInode) inode.getChild(dir);
		}

		Assert.assertNull((getDirInPath("/ab/cd/ef", 3, storage.getRoot())));

		// MKDIR locking
		storage = initTree();
		path = "/ab/cd/ef/i";

		storage.rmdir(path, false);

		path = "ab/cd/ef/i";
		p = path.split("/");
		Assert.assertNotNull((getDirInPath("/ab/cd/ef/i", 3, storage.getRoot())));
		inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			Assert.assertEquals(0, inode.getLock().getReadHoldCount());
			Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			inode = (PhysicalInode) inode.getChild(dir);
		}

	}

	public static PhysicalInode getDirInPath(String path, int dirIdx, Inode inode) {
		String[] p = path.split("/");
		p[0] = "/";
		PhysicalInode nodeToReturn = (PhysicalInode) inode;
		for (int i = 1; i < dirIdx + 1; i++) {
			nodeToReturn = (PhysicalInode) nodeToReturn.getChild(p[i]);
		}
		return nodeToReturn;

	}
	
	@Test
	public void testRedirectResponse() {
		CentralizedStorage storage1 = initTree();
		PhysicalInode nodeCd = CentralizedStorageTest.getDirInPath("/ab/cd/ef", 2, storage1.getRoot());
		PhysicalInode nodeEf = CentralizedStorageTest.getDirInPath("/ab/cd/ef", 3, storage1.getRoot());
		nodeCd.getChildren().remove("ef");
		VirtualInode vInode = new VirtualInode(nodeEf,"locah:host:4040");
		nodeCd.addChild(vInode);
		
		//Test Get redirect request always
		
		//ls
		Result<String> result=storage1.ls("/ab/cd/ef", false);
		System.out.println(result);
		Assert.assertEquals(result.isOperationSuccess(), false);
		String[] serverToGo =result.getOperationReturnMessage().split("\n")[0].split(":",2);
		String[] path=result.getOperationReturnMessage().split("\n")[1].split(":",2);
		
		Assert.assertEquals(serverToGo[1], "locah:host:4040");
		Assert.assertEquals(path[1], " /ab/cd/ef");
		
		result=storage1.ls("/ab/cd/ef/fg/ge", false);
		
		Assert.assertEquals(result.isOperationSuccess(), false);
		serverToGo =result.getOperationReturnMessage().split("\n")[0].split(":",2);
		path=result.getOperationReturnMessage().split("\n")[1].split(":",2);
		
		Assert.assertEquals(serverToGo[1], "locah:host:4040");
		Assert.assertEquals(path[1], " /ab/cd/ef");
		
		//mkdir
		result=storage1.mkdir("/ab/cd/ef/fg/", false);
		Assert.assertEquals(result.isOperationSuccess(), false);
		System.out.println(result);
		serverToGo =result.getOperationReturnMessage().split("\n")[0].split(":",2);
		path=result.getOperationReturnMessage().split("\n")[1].split(":",2);
		
		
		Assert.assertEquals(serverToGo[1], "locah:host:4040");
		Assert.assertEquals(path[1], " /ab/cd/ef");
		
		result=storage1.mkdir("/ab/cd/hh", false);
		Assert.assertEquals(result.isOperationSuccess(), true);
		
		result=storage1.mkdir("/ab/cd/ef/fg/hh", false);
		Assert.assertEquals(result.isOperationSuccess(), false);
		serverToGo =result.getOperationReturnMessage().split("\n")[0].split(":",2);
		path=result.getOperationReturnMessage().split("\n")[1].split(":",2);
		Assert.assertEquals(serverToGo[1], "locah:host:4040");
		Assert.assertEquals(path[1], " /ab/cd/ef");
		
		
		result=storage1.mkdir("/ab/cd/ef/", false);
		Assert.assertEquals(result.isOperationSuccess(), false);
		Assert.assertEquals(result.getOperationReturnMessage(), "Directory already exists");
		
		
		//rmdir
		result=storage1.rmdir("/ab/cd/ef", false);
		Assert.assertEquals(result.isOperationSuccess(), false);
		serverToGo =result.getOperationReturnMessage().split("\n")[0].split(":",2);
		path=result.getOperationReturnMessage().split("\n")[1].split(":",2);
		Assert.assertEquals(serverToGo[1], "locah:host:4040");
		Assert.assertEquals(path[1], " /ab/cd/ef");
	}
	
}
