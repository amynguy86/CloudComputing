package cs6343;

public class CassUnixApp {
    public static void main(String[] args){

CassandraMDS cmds = new CassandraMDS("127.0.0.1");
cmds.configureDB();


cmds.mkdir("/test1dir");
cmds.mkdir("/test1dir/test2dir");
cmds.mkdir("/test3dir");
cmds.touch("/test1dir/file1");
cmds.touch("/test1dir/file2");

System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
System.out.println(cmds.ls("/test1dir"));
cmds.rm("/test1dir/file1");
System.out.println(cmds.ls("/test1dir"));
cmds.disconnect();
    }
}
