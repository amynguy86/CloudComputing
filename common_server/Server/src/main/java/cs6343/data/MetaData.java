package cs6343.data;

public class MetaData {

	@Override
	public String toString() {
		return "MetaData [type=" + type + ", size=" + size + ", owner=" + owner + ", creationTime=" + creationTime
				+ ", privilege=" + privilege + ", lastaccessed=" + lastaccessed + ", lastModified=" + lastModified
				+ "]";
	}

	public FileType type;

	public Integer size;

	public String owner;

	public Long creationTime;

	public String privilege;

	public Long lastaccessed;

	public Long lastModified;

	public FileType getType() {
		return type;
	}

	public void setType(FileType type) {
		this.type = type;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}

	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	public Long getLastaccessed() {
		return lastaccessed;
	}

	public void setLastaccessed(Long lastaccessed) {
		this.lastaccessed = lastaccessed;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

}