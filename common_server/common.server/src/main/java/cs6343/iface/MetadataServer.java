package cs6343.iface;
import cs6343.data.Directory;

public interface MetadataServer{
    public Directory list(String path);
    public boolean add(String path, String filename);
    public boolean change_mode(String path, String filename, String permission);
    public boolean remove(String path);
    public boolean make_directory(String path, String directory);
    public boolean move(String oldPath, String newPath);
}