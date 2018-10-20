package cs6343.data;

public enum FileType {
	FILE("FILE"), DIRECTORY("DIRECTORY");
	
	String type;
	
	FileType(String type) {
		this.type=type;
	}
	
	public String toString() {
		return type;
	}
}
