package server;

import org.junit.Assert;
import static org.hamcrest.CoreMatchers.instanceOf;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;

import cs6343.centralized.CentralizedStorage;
import cs6343.ceph.CephServer;
import cs6343.ceph.CephServer.ServerRequest;
import cs6343.ceph.CephStorage;
import cs6343.data.PhysicalInode;
import cs6343.data.VirtualInode;
import cs6343.iface.Inode;
import cs6343.util.Result;

public class CephStorageTest {

	public CentralizedStorage initTree() {
		CentralizedStorage storage = new CentralizedStorage();
		validateOp(storage.mkdir("/ab"));
		validateOp(storage.mkdir("/cd"));
		validateOp(storage.mkdir("/cd/ab"));
		validateOp(storage.mkdir("/ab/cd"));
		validateOp(storage.mkdir("/ab/cd/ef"));
		validateOp(storage.mkdir("/ab/cd/ef/zh"));
		validateOp(storage.mkdir("/ab/cd/ef/zh/dh"));
		validateOp(storage.mkdir("/ab/cd/gh"));
		validateOp(storage.mkdir("/ab/ef"));
		return storage;
	}

	public CentralizedStorage initTree(String rootDir,String rootDirParent) {
		CentralizedStorage storage = new CentralizedStorage(rootDir);
		storage.getRoot().setPath(rootDirParent+rootDir);
		validateOp(storage.mkdir(rootDir+"/ab"));
		validateOp(storage.mkdir(rootDir+"/cd"));
		validateOp(storage.mkdir(rootDir+"/cd/ab"));
		validateOp(storage.mkdir(rootDir+"/ab/cd"));
		validateOp(storage.mkdir(rootDir+"/ab/cd/ef"));
		validateOp(storage.mkdir(rootDir+"/ab/cd/gh"));
		validateOp(storage.mkdir(rootDir+"/ab/ef"));
		
		
		return storage;
	}

	public void validateOp(Result result) {
		Assert.assertEquals(result.isOperationSuccess(), true);
	}

	//@Test
	public void testJson() {
		CentralizedStorage storage = initTree();
		PhysicalInode p =new PhysicalInode();
		p.addChild(new PhysicalInode());
		storage.setRoot(p);
		String inode = PhysicalInode.toJson(storage.getRoot());
		System.out.println(inode);
		PhysicalInode d = PhysicalInode.fromJson(inode);
	}

	@Test
	public void testPartition() {
		CentralizedStorage storage = initTree();
		CephServer cephServer = Mockito.mock(CephServer.class);
		Mockito.when(cephServer.sendCreatePartition(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

		CephStorage cephStorage = new CephStorage(false, "someurl", cephServer);
		cephStorage.init(storage.getRoot());

		PhysicalInode transferNode = CentralizedStorageTest.getDirInPath("/ab/cd/ef/zh", 4, storage.getRoot());
		Result<String> result = cephStorage.partition("/ab/cd/ef/zh " + "someurlsend", false);
		Assert.assertEquals(result.isOperationSuccess(), true);

		// Test Locking
		String path = "/ab/cd/ef/zh";
		path = "ab/cd/ef";

		String p[] = path.split("/");
		PhysicalInode inode = (PhysicalInode) storage.getRoot();
		for (String dir : p) {
			if (!inode.getName().equals("ef")) {
				Assert.assertEquals(1, inode.getLock().getReadHoldCount());
				Assert.assertEquals(0, inode.getLock().getWriteHoldCount());
			} else {
				Assert.assertEquals(0, inode.getLock().getReadHoldCount());
				Assert.assertEquals(1, inode.getLock().getWriteHoldCount());
			}
			inode = (PhysicalInode) inode.getChild(dir);
		}

		PhysicalInode node = CentralizedStorageTest.getDirInPath("/ab/cd/ef", 3, storage.getRoot());
		Inode childNode = node.getChild("zh");
		Assert.assertThat(childNode, instanceOf(VirtualInode.class));
		Assert.assertEquals(childNode.getName(), "zh");
		Assert.assertEquals(childNode.getParent().getName(), "ef");
		Assert.assertEquals(childNode.getParent().getPath(), "/ab/cd/ef");
		Mockito.verify(cephServer, Mockito.times(1)).sendCreatePartition(PhysicalInode.toJson(transferNode),
				"someurlsend");
	}

	@Test
	public void testCreatePartition() {
		CephStorage storage = new CephStorage(false,"localhost:8080");
		String rootDirName="/somedir/on/parent/server/";
		CentralizedStorage CentralStorage = this.initTree("SomeRoot",rootDirName);
		CentralStorage.getRoot().setPath(rootDirName+CentralStorage.getRoot().getName());
		String json = PhysicalInode.toJson(CentralStorage.getRoot());
		System.out.println(json);
		storage.init(PhysicalInode.fromJson(json));
		
		PhysicalInode test= PhysicalInode.fromJson(json);
		//Test Basic Operation, Mock the Locking
		// TestLS
		rootDirName="/somedir/on/parent/server/SomeRoot%";
		Result<String> result = storage.ls(rootDirName+"ab");
		validateOp(result);
		String resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "Inode [name=cd]\nInode [name=ef]");

		// Test RMDIR
		result = storage.rmdir(rootDirName+"ab/cd");
		validateOp(result);

		result = storage.ls(rootDirName+"ab");
		validateOp(result);
		resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "Inode [name=ef]");

		result = storage.rmdir(rootDirName+"ab/ef");
		validateOp(result);

		result = storage.ls(rootDirName+"ab");
		validateOp(result);
		resultStr = result.getOperationReturnVal();
		Assert.assertEquals(resultStr, "");

		// TEST Touch
		result = storage.touch(rootDirName+"ab/file");
		validateOp(result);

		// Mkdir FAIL because cant create a something in a file
		result = storage.mkdir(rootDirName+"ab/file/ab");
		Assert.assertEquals(result.isOperationSuccess(), false);
	}
}
