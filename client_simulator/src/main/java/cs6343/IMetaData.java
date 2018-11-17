package cs6343;

import java.util.List;

public interface IMetaData {
    public boolean mkdir();
    public List<FileNode> ls();
    public boolean touch();
    public boolean rm();
    public boolean rmdir();
}
