package cs6343.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cs6343.iface.Inode;
import cs6343.util.RedirectException;

/*
 * This Inode represents an inode that may not reside on the same server
 */
public class VirtualInode implements Inode {
	private String name;
	private String serverId;
	private String path;
	@JsonIgnore
	private Inode parent;

	public VirtualInode() {

	}

	public VirtualInode(Inode inode, String serverId) {
		this.setName(inode.getName());
		this.setPath(inode.getPath());
		this.setParent(inode.getParent());
		this.setServerId(serverId);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getServerId() {
		return this.serverId;
	}

	@Override
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	@Override
	public Inode getParent() {
		return this.parent;
	}

	@Override
	public void readLock(LockOperation lockOperation) {
		// TODO Auto-generated method stub
		throw new RedirectException();
	}

	@Override
	public void writeLock(LockOperation lockOperation) {
		// TODO Auto-generated method stub
		throw new RedirectException();
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public void setPath(String path) {
		this.path=path;
	}

	@Override
	public void setParent(Inode parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "VirtualInode{" +
				"name='" + name + '\'' +
				", serverId='" + serverId + '\'' +
				", path='" + path + '\'' +
				", parent=" + parent +
				'}';
	}
}
