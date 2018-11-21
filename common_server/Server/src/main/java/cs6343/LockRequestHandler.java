package cs6343;

import cs6343.ceph.CephServer;
import cs6343.data.PhysicalInode;
import cs6343.data.VirtualInode;
import cs6343.iface.Inode;
import cs6343.util.Result;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockRequestHandler implements Runnable {
    CephServer cephServer;
    String rootPath;
	BufferedReader isr;
	OutputStreamWriter osw;
	Socket s;

	public LockRequestHandler(CephServer cephServer, InputStreamReader isr,
							  OutputStreamWriter osw, Socket s) {
	    this.cephServer = cephServer;
		this.isr = new BufferedReader(isr);
		this.osw = osw;
		this.s = s;
		this.rootPath = rootPath;
	}

	
	private static interface Lock {
		public void doLock(String path);
	    public void doUnlock(String path);
	}

	
	public static interface ReadLock extends Lock {
	}

	public static interface WriteLock extends Lock {
	
	}
	public void cephHandleRequest() throws IOException {
		String stuff = isr.readLine();
		System.out.println(stuff);
		if (stuff.startsWith("readlock")) {
		    String path = stuff.split(" ")[1];
		    if(cephServer.cephStorage.isRoot){
		        List<String> pathParts = Arrays.asList(path.split("/"));
		    	Result<List<Inode>> lockResult =cephServer.cephStorage.storage.lockRead(pathParts, pathParts.size());
		    	if(!lockResult.isOperationSuccess()){
		    		osw.write("Can't lock because " + lockResult.getOperationReturnMessage());
                    flushAndClose();
                    return;
				}

                osw.write("locked\n");
                osw.flush();
                isr.readLine();
                for(Inode i : lockResult.getOperationReturnVal()){i.readLock(Inode.LockOperation.UNLOCK);};
                osw.write("unlocked\n");
                flushAndClose();
                return;
			} else {
				Result<String[]> result = cephServer.cephStorage.validateCephPath(path);
				if(!result.isOperationSuccess()){
					osw.write("Can't lock because " + result.getOperationReturnMessage());
                    flushAndClose();
                    return;
				}
                List<String> pathParts = Arrays.asList(result.getOperationReturnVal()[1].split("/"));
                Result<List<Inode>> lockResult = cephServer.cephStorage.storage.lockRead(pathParts, pathParts.size());
                if(!lockResult.isOperationSuccess()){
                    osw.write("Can't lock because " + lockResult.getOperationReturnMessage());
                    flushAndClose();
                    return;
                }
                osw.write("locked\n");
                osw.flush();
                isr.readLine();
                for(Inode i : lockResult.getOperationReturnVal()){ i.readLock(Inode.LockOperation.UNLOCK);};
                osw.write("unlocked\n");
                flushAndClose();
            }
		} else if (stuff.startsWith("writelock")) {
            String path = stuff.split(" ")[1];
			if(cephServer.cephStorage.isRoot){
                List<String> pathParts = Arrays.asList(path.split("/"));
                Result<List<Inode>> lockResult = cephServer.cephStorage.storage.lockRead(pathParts, pathParts.size()-2);
                if(!lockResult.isOperationSuccess()){
                    osw.write("Can't lock because " + lockResult.getOperationReturnMessage());
                    flushAndClose();
                    return;
                }
                List<Inode> locks = lockResult.getOperationReturnVal();
                PhysicalInode last = (PhysicalInode) locks.get(locks.size()-1);
                Inode toBeWriteLocked = last.getChild(pathParts.get(pathParts.size()-1));
                if(toBeWriteLocked == null || toBeWriteLocked.getClass() == VirtualInode.class){
                    for(Inode i : lockResult.getOperationReturnVal()){ i.readLock(Inode.LockOperation.UNLOCK);};
                    osw.write("Can't lock because last node is virtual " + path + " # Virtual Host: " + ((VirtualInode) toBeWriteLocked).getServerId());
                    flushAndClose();
                    return;
                }
                toBeWriteLocked = (PhysicalInode) toBeWriteLocked;
                toBeWriteLocked.writeLock(Inode.LockOperation.LOCK);
                osw.write("locked\n");
                osw.flush();
                stuff = isr.readLine();
                toBeWriteLocked.writeLock(Inode.LockOperation.UNLOCK);
                for(Inode i : lockResult.getOperationReturnVal()){ i.readLock(Inode.LockOperation.UNLOCK);};
                osw.write("unlocked\n");
                flushAndClose();
            } else {
                Result<String[]> result = cephServer.cephStorage.validateCephPath(path);
                if(!result.isOperationSuccess()){
                    osw.write("Can't lock because " + result.getOperationReturnMessage());
                    flushAndClose();
                    return;
                }
                List<String> pathParts = Arrays.asList(result.getOperationReturnVal()[1].split("/"));
                Result<List<Inode>> lockResult = cephServer.cephStorage.storage.lockRead(pathParts, pathParts.size()-2);
                if(!lockResult.isOperationSuccess()){
                    osw.write("Can't lock because " + lockResult.getOperationReturnMessage());
                    flushAndClose();
                    return;
                }
                List<Inode> locks = lockResult.getOperationReturnVal();
                PhysicalInode last = (PhysicalInode) locks.get(locks.size()-1);
                Inode toBeWriteLocked = last.getChild(pathParts.get(pathParts.size()-1));
                if(toBeWriteLocked == null || toBeWriteLocked.getClass() == VirtualInode.class){
                    for(Inode i : lockResult.getOperationReturnVal()){ i.readLock(Inode.LockOperation.UNLOCK);};
                    osw.write("Can't lock because last node is virtual " + path + " # Virtual Host: " + ((VirtualInode) toBeWriteLocked).getServerId());
                    flushAndClose();
                    return;
                }
                toBeWriteLocked = (PhysicalInode) toBeWriteLocked;
                toBeWriteLocked.writeLock(Inode.LockOperation.LOCK);
                osw.write("locked\n");
                osw.flush();
                isr.readLine();
                toBeWriteLocked.writeLock(Inode.LockOperation.UNLOCK);
                for(Inode i : lockResult.getOperationReturnVal()){ i.readLock(Inode.LockOperation.UNLOCK);};
                osw.write("unlocked\n");
                flushAndClose();
            }
		}
		osw.flush();
		isr.close();
		osw.close();
		s.close();
	}

    private void flushAndClose() throws IOException {
        osw.flush();
        osw.close();
        isr.close();
        s.close();
    }

	@Override
	public void run() {
		try {
			cephHandleRequest();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
