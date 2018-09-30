package cs6343.mds;

import cs6343.data.Directory;
import cs6343.iface.MetadataServer;

public class SingleUserMDS implements MetadataServer {
    public Directory rootDir = new Directory();

    @Override
    public String list(String path) {
        String[] components = path.split("/");
        Directory d = parentDirectory(path);
        if(d == null){
            return "Not found: " + path;
        }
        if(d.subdirectories.get(components[components.length-1])!=null){
            return d.subdirectories.get(components[components.length-1]).toString();
        } else if(d.files.get(components[components.length-1])!= null){
            return d.files.get(components[components.length-1]).toString();
        }
        return "Not found: " + path;
    }

    @Override
    public boolean add(String path, String filename) {
        return false;
    }

    @Override
    public boolean change_mode(String path, String filename, String permission) {
        return false;
    }

    @Override
    public boolean remove(String path) {
        return false;
    }

    @Override
    public boolean make_directory(String path, String directory) {
        return false;
    }

    @Override
    public boolean move(String oldPath, String newPath) {
        return false;
    }

    public Directory parentDirectory(String path){
        String[] components = path.split("/");
        Directory current = rootDir;
        int i = 0;
        while(current != null && i < components.length-1){
            current = current.subdirectories.get(components[i]);
        }
        return current;
    }

}
