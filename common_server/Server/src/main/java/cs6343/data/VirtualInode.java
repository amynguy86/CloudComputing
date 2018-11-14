package cs6343.data;

import cs6343.iface.Inode;
import cs6343.util.RedirectException;

/*
 * This Inode represents an inode that may not reside on the same server
 */
public class VirtualInode implements Inode {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getServerId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setServerId(String serverId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Inode getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readLock(LockOperation lockOperation) {
		// TODO Auto-generated method stub
		throw new RedirectException();
	}

	@Override
	public void writeLock(LockOperation lockOperation){
		// TODO Auto-generated method stub
		throw new RedirectException();
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPath(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(Inode parent) {
		// TODO Auto-generated method stub
		
	}
}
