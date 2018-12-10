package cs6343;

public class CassUnixApp {
    public static void main(String[] args){

CassandraMDS cmds = new CassandraMDS("127.0.0.1");
cmds.configureDB();

if(args.length!=0 && args[0].equals("commandline"))
{

}
else {

    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    cmds.mkdir("/a");
  //  cmds.mkdir("/b");
    cmds.mkdir("/a/aa");
    cmds.mkdir("/a/ab");
    cmds.mkdir("/a/aa/aaa");
    cmds.mkdir("/a/ab/aaa");
    cmds.mkdir("/a/aa/aaa/aaaa");
    System.out.println(cmds.ls("/a/aa/aaa"));
    cmds.rmdir("/a");
    System.out.println(cmds.ls("/a/aa/aaa"));
    System.out.println(cmds.ls("/"));


}
cmds.disconnect();
    }
}
