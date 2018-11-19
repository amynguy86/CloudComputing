package cs6343;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RequestHandler implements Runnable {
	ConcurrentHashMap<String, ReadWriteLock> lockMap;
	BufferedReader isr;
	OutputStreamWriter osw;
	Socket s;

	public RequestHandler(ConcurrentHashMap<String, ReadWriteLock> lockMap, InputStreamReader isr,
			OutputStreamWriter osw, Socket s) {
		this.lockMap = lockMap;
		this.isr = new BufferedReader(isr);
		this.osw = osw;
		this.s = s;
	}

	
	private static interface Lock {
		public void doLock(String path);
	    public void doUnlock(String path);
	}

	
	public static interface ReadLock extends Lock {
	}

	public static interface WriteLock extends Lock {
	
	}

	/*
	 * This is for generalized for use with Ceph or anything else
	 */
	public void handleRequest2(ReadLock readLock,WriteLock writeLock) throws IOException {
		String stuff = isr.readLine();
		System.out.println(stuff);
		Lock lock;
		if (stuff.startsWith("readlock")) {
			lock=readLock;
		} else if (stuff.startsWith("writelock")) {
			lock=writeLock;
		}
		else {
			throw new IOException("Not Supported");
		}
		
		if(lock==null) {
			throw new IOException("Invalid Parameters");
		}
		
		String path=stuff.split(" ")[1];
		lock.doLock(path);
		osw.write("locked\n");
		osw.flush();
		stuff = isr.readLine();
		lock.doUnlock(path);
		osw.write("unlocked\n");
		osw.flush();
		
		osw.flush();
		isr.close();
		osw.close();
		s.close();
	}

	public void handleRequest() throws IOException {
		String stuff = isr.readLine();
		System.out.println(stuff);
		if (stuff.startsWith("readlock")) {
			String[] fileParts = stuff.split("/");
			String partials = "";
			for (int i = 1; i < fileParts.length; i++) {
				partials = partials + fileParts[i] + '/';
				ReadWriteLock lock = new ReentrantReadWriteLock();
				lockMap.putIfAbsent(partials, lock);
				lock = lockMap.get(partials);
				lock.readLock().lock();
			}
			osw.write("locked\n");
			osw.flush();
			stuff = isr.readLine();
			partials = "";
			for (int i = 1; i < fileParts.length; i++) {
				partials = partials + fileParts[i] + '/';
				ReadWriteLock lock = lockMap.get(partials);
				lock.readLock().unlock();
			}
			osw.write("unlocked\n");
			osw.flush();

		} else if (stuff.startsWith("writelock")) {
			String[] fileParts = stuff.split("/");
			String partials = "";
			for (int i = 1; i < fileParts.length - 1; i++) {
				partials = partials + fileParts[i] + '/';
				ReadWriteLock lock = new ReentrantReadWriteLock();
				lockMap.putIfAbsent(partials, lock);
				lock = lockMap.get(partials);
				lock.readLock().lock();
			}
			partials = partials + fileParts[fileParts.length - 1] + '/';
			ReadWriteLock writelock = new ReentrantReadWriteLock();
			lockMap.putIfAbsent(partials, writelock);
			writelock = lockMap.get(partials);
			writelock.writeLock().lock();
			osw.write("locked\n");
			osw.flush();
			stuff = isr.readLine();
			partials = "";
			for (int i = 1; i < fileParts.length - 1; i++) {
				partials = partials + fileParts[i] + '/';
				ReadWriteLock lock = new ReentrantReadWriteLock();
				lockMap.putIfAbsent(partials, lock);
				lock = lockMap.get(partials);
				lock.readLock().unlock();
			}
			partials = partials + fileParts[fileParts.length - 1] + '/';
			writelock = lockMap.get(partials);
			writelock.writeLock().unlock();
			osw.write("unlocked\n");
			osw.flush();
		}
		osw.flush();
		isr.close();
		osw.close();
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
