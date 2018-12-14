package cs6343.data;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs6343.iface.Inode;

@JsonIgnoreProperties(value = { "lock" })
public class PhysicalInode implements Inode {
	MetaData metaData;
	String serverId;
	
	@JsonIgnore
	ReentrantReadWriteLock readWriteLock;

	public MetaData getMetaData() {
		return metaData;
	}
	
	public ReentrantReadWriteLock getLock() {
		return readWriteLock;
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
		return "Inode [name=" + name + "] "+readWriteLock +" "+path;
	}

	public void addChild(Inode inode) {
		this.children.put(inode.getName(), inode);
	}

	public Inode getChild(PhysicalInode inode) {
		return this.children.get(inode.getName());
	}

	public Inode getChild(String name) {
		return this.children.get(name);
	}

	public Map<String, Inode> getChildren() {
		return this.children;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@JsonIgnore
	PhysicalInode parent;
	Map<String, Inode> children; // PATH,INODE
	String name;
	boolean isDeleted;
	String path;
	
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
		if (lockOperation == LockOperation.LOCK)
			this.readWriteLock.writeLock().lock();
		else
			this.readWriteLock.writeLock().unlock();
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return this.path;
	}

	@Override
	public void setPath(String path) {
		this.path=path;
	}

	@Override
	public void setParent(Inode parent) {
		this.parent=(PhysicalInode)parent;
	}
	
	public static String toJson(PhysicalInode nodeToMoveInode) {
		try {
		ObjectMapper mapper = new ObjectMapper();
		String json=mapper.writeValueAsString(nodeToMoveInode);
		return json;
		}
		catch(JsonProcessingException ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public static PhysicalInode fromJson(String json) {
		try {
		ObjectMapper mapper = new ObjectMapper();
		PhysicalInode node=(PhysicalInode) mapper.readValue(json, Inode.class);
		return node;
		}
		catch(JsonProcessingException ex){
			ex.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
