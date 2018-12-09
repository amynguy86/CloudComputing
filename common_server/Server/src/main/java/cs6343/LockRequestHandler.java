package cs6343;

import cs6343.ceph.CephServer;
import cs6343.data.PhysicalInode;
import cs6343.data.VirtualInode;
import cs6343.iface.Inode;
import cs6343.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockRequestHandler implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LockRequestHandler.class);
	CephServer cephServer;
	String rootPath;
	BufferedReader isr;
	OutputStreamWriter osw;
	Socket s;

	public LockRequestHandler(CephServer cephServer, InputStreamReader isr, OutputStreamWriter osw, Socket s) {
		this.cephServer = cephServer;
		this.isr = new BufferedReader(isr);
		this.osw = osw;
		this.s = s;
	}

	public void handleRequest() throws IOException {
		String path = isr.readLine();
		System.out.println("Locking: " + path);
		PhysicalInode rootNode = cephServer.cephStorage.storage.getRoot();
		if (!path.startsWith(rootNode.getPath())) {
			osw.write("Path doesn't start with root! " + path + " root: " + rootNode.getName());
			flushAndClose();
			return;
		}

		List<String> parts = Arrays.asList(path.substring(rootNode.getPath().length()).split("/"));
		List<Inode> locks = new ArrayList<>();
		PhysicalInode curr = rootNode;
		RemoteLock remoteLock = null;
		curr.readLock(Inode.LockOperation.LOCK);
		locks.add(curr);
		System.out.println("Parts: " + parts);
		for (String part : parts) {
			if (part.length() > 0) {
				Inode next = curr.getChild(part);
				if (next == null) {
					osw.write("Could not find part: " + part + "\n");
					flushAndClose();
					for (Inode lock : locks) {
						lock.readLock(Inode.LockOperation.UNLOCK);
					}
					return;
				} else if (next.getClass().equals(VirtualInode.class)) {
					VirtualInode vnext = (VirtualInode) next;
					String lockServerId = getLockServer(vnext);
					remoteLock = new RemoteLock(lockServerId.split(":")[0],
							Integer.parseInt(lockServerId.split(":")[1]));
					remoteLock.lock(path);
					break;
				} else {
					curr = (PhysicalInode) next;
				}
				curr.readLock(Inode.LockOperation.LOCK);
				locks.add(curr);
			}
		}
		osw.write("locked" + "\n");
		osw.flush();
		isr.readLine();
		for (Inode i : locks) {
			i.readLock(Inode.LockOperation.UNLOCK);
		}
		if (remoteLock != null)
			remoteLock.unlock();
		osw.write("unlocked");
		flushAndClose();
	}

	private String getLockServer(VirtualInode vnext) {
		String serverId = vnext.getServerId();
		String port = serverId.split(":")[1];
		int lockserverPort = Integer.parseInt(port) + 1;
		String lockServerId = serverId.split(":")[0] +":"+ lockserverPort;
		return lockServerId;
	}

	// public void cephHandleRequest() throws IOException {
	// String stuff = isr.readLine();
	// System.out.println(stuff);
	// if (stuff.startsWith("readlock")) {
	// String path = stuff.split(" ")[1];
	// List<String> pathParts = Arrays.asList(path.split("/"));
	// pathParts.set(0, "/");
	// if(cephServer.cephStorage.isRoot){
	// LOGGER.info(pathParts.toString());
	// Result<List<Inode>> lockResult
	// =cephServer.cephStorage.storage.lockRead(pathParts, pathParts.size());
	// if(!lockResult.isOperationSuccess()){
	// osw.write("Can't lock because " + lockResult.getOperationReturnMessage());
	// flushAndClose();
	// return;
	// }
	// osw.write("locked\n");
	// osw.flush();
	// isr.readLine();
	// for(Inode i :
	// lockResult.getOperationReturnVal()){i.readLock(Inode.LockOperation.UNLOCK);};
	// osw.write("unlocked\n");
	// flushAndClose();
	// return;
	// } else {
	// Result<String[]> result = cephServer.cephStorage.validateCephPath(path);
	// if(!result.isOperationSuccess()){
	// osw.write("Can't lock because " + result.getOperationReturnMessage());
	// flushAndClose();
	// return;
	// }
	// Result<List<Inode>> lockResult =
	// cephServer.cephStorage.storage.lockRead(pathParts, pathParts.size());
	// if(!lockResult.isOperationSuccess()){
	// osw.write("Can't lock because " + lockResult.getOperationReturnMessage());
	// flushAndClose();
	// return;
	// }
	// osw.write("locked\n");
	// osw.flush();
	// isr.readLine();
	// for(Inode i : lockResult.getOperationReturnVal()){
	// i.readLock(Inode.LockOperation.UNLOCK);};
	// osw.write("unlocked\n");
	// flushAndClose();
	// }
	// } else if (stuff.startsWith("writelock")) {
	// String path = stuff.split(" ")[1];
	// List<String> pathParts = Arrays.asList(path.split("/"));
	// pathParts.set(0, "/");
	// if(cephServer.cephStorage.isRoot){
	// Result<List<Inode>> lockResult =
	// cephServer.cephStorage.storage.lockRead(pathParts, pathParts.size()-1);
	// if(!lockResult.isOperationSuccess()){
	// osw.write("Can't lock because " + lockResult.getOperationReturnMessage());
	// flushAndClose();
	// return;
	// }
	// List<Inode> locks = lockResult.getOperationReturnVal();
	// PhysicalInode last = (PhysicalInode) locks.get(locks.size()-1);
	// Inode toBeWriteLocked = last.getChild(pathParts.get(pathParts.size()-1));
	// if(toBeWriteLocked == null || toBeWriteLocked.getClass() ==
	// VirtualInode.class){
	// for(Inode i : lockResult.getOperationReturnVal()){
	// i.readLock(Inode.LockOperation.UNLOCK);};
	// osw.write("REDIRECT TO SERVER: " + ((VirtualInode)
	// toBeWriteLocked).getServerId());
	// flushAndClose();
	// return;
	// }
	// toBeWriteLocked = (PhysicalInode) toBeWriteLocked;
	// toBeWriteLocked.writeLock(Inode.LockOperation.LOCK);
	// osw.write("locked\n");
	// osw.flush();
	// stuff = isr.readLine();
	// toBeWriteLocked.writeLock(Inode.LockOperation.UNLOCK);
	// for(Inode i : lockResult.getOperationReturnVal()){
	// i.readLock(Inode.LockOperation.UNLOCK);};
	// osw.write("unlocked\n");
	// flushAndClose();
	// } else {
	// Result<String[]> result = cephServer.cephStorage.validateCephPath(path);
	// if(!result.isOperationSuccess()){
	// osw.write("Can't lock because " + result.getOperationReturnMessage());
	// flushAndClose();
	// return;
	// }
	// Result<List<Inode>> lockResult =
	// cephServer.cephStorage.storage.lockRead(pathParts, pathParts.size()-2);
	// if(!lockResult.isOperationSuccess()){
	// osw.write("Can't lock because " + lockResult.getOperationReturnMessage());
	// flushAndClose();
	// return;
	// }
	// List<Inode> locks = lockResult.getOperationReturnVal();
	// PhysicalInode last = (PhysicalInode) locks.get(locks.size()-1);
	// Inode toBeWriteLocked = last.getChild(pathParts.get(pathParts.size()-1));
	// if(toBeWriteLocked == null || toBeWriteLocked.getClass() ==
	// VirtualInode.class){
	// for(Inode i : lockResult.getOperationReturnVal()){
	// i.readLock(Inode.LockOperation.UNLOCK);};
	// osw.write("REDIRECT TO SERVER: " + ((VirtualInode)
	// toBeWriteLocked).getServerId());
	// flushAndClose();
	// return;
	// }
	// toBeWriteLocked = (PhysicalInode) toBeWriteLocked;
	// toBeWriteLocked.writeLock(Inode.LockOperation.LOCK);
	// osw.write("locked\n");
	// osw.flush();
	// isr.readLine();
	// toBeWriteLocked.writeLock(Inode.LockOperation.UNLOCK);
	// for(Inode i : lockResult.getOperationReturnVal()){
	// i.readLock(Inode.LockOperation.UNLOCK);};
	// osw.write("unlocked\n");
	// flushAndClose();
	// }
	// }
	// osw.flush();
	// isr.close();
	// osw.close();
	// s.close();
	// }

	private void flushAndClose() throws IOException {
		osw.flush();
		osw.close();
		isr.close();
		s.close();
	}

	@Override
	public void run() {
		try {
			handleRequest();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
