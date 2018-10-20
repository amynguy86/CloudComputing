package cs6343.iface;

public interface Inode {

	public String getName();
	public void setName(String name);
	public String getServerId();
	public void  setServerId(String serverId);
	public void readLock(LockOperation lockOperation);
	public void writeLock(LockOperation lockOperation);
	public Inode getParent();
	
	public enum LockOperation{
		LOCK,UNLOCK
	}
}
