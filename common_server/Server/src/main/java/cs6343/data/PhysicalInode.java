package cs6343.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cs6343.iface.Inode;

public class PhysicalInode implements Inode {
	MetaData metaData;
	String serverId;
	ReentrantReadWriteLock readWriteLock;
	private Lock lock;

	public MetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	public PhysicalInode getParent() {
		return parent;
	}

	public void setParent(PhysicalInode parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "Inode [name=" + name + "]";
	}

	public void addChild(PhysicalInode inode) {
		this.children.put(inode.getName(), inode);
	}

	public PhysicalInode getChild(PhysicalInode inode) {
		return this.children.get(inode.getName());
	}

	public PhysicalInode getChild(String name) {
		return this.children.get(name);
	}

	public Map<String, PhysicalInode> getChildren() {
		return this.children;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	PhysicalInode parent;
	Map<String, PhysicalInode> children; // PATH,INODE
	String name;
	boolean isDeleted;
	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public PhysicalInode() {
		this.metaData = null;
		this.parent = null;
		children = new ConcurrentHashMap<>();
		name = "";
		this.serverId = "1"; // getCurrentServerID();
		this.readWriteLock = new ReentrantReadWriteLock();
		isDeleted=false;
	}

	@Override
	public String getServerId() {
		// TODO Auto-generated method stub
		return serverId;
	}

	@Override
	public void setServerId(String serverId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readLock(LockOperation lockOperation) {
		if (lockOperation == LockOperation.LOCK)
			this.readWriteLock.readLock().lock();
		else
			this.readWriteLock.readLock().unlock();
	}

	@Override
	public void writeLock(LockOperation lockOperation) {
		// TODO Auto-generated method stub
		if (lockOperation == LockOperation.LOCK)
			this.readWriteLock.writeLock().lock();
		else
			this.readWriteLock.writeLock().unlock();
	}
}
