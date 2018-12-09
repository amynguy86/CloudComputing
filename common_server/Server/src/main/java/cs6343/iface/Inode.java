package cs6343.iface;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import cs6343.data.MetaData;
import cs6343.data.PhysicalInode;
import cs6343.data.VirtualInode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = PhysicalInode.class, name = "PhysicalInode"), @Type(value = VirtualInode.class, name = "virtualInode") })
public interface Inode {

	public String getName();

	public void setName(String name);

	public String getPath();

	public void setPath(String path);

	public String getServerId();

	public void setServerId(String serverId);

	public void readLock(LockOperation lockOperation);

	public void writeLock(LockOperation lockOperation);

	public Inode getParent();

	public void setParent(Inode parent);

	public enum LockOperation {
		LOCK, UNLOCK
	}

}
