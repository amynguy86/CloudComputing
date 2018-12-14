package cs6343;

import java.util.Scanner;
public class CassUnixApp {
    public static void main(String[] args){

CassandraMDS cmds = new CassandraMDS("127.0.0.1");


if(args.length!=0 && args[0].equals("commandline"))
{
Scanner scanner=new Scanner(System.in);
String input;
    while (!(input = scanner.nextLine()).equals("exit"))
     {
         String cmd=input.split(" ")[0];
         String arg="";
         if(input.split(" ").length>=2) {
             arg = input.split(" ")[1];
         }
         if(cmd.equals("configure"))
         {
             cmds.configureDB();
         }
         else if(arg.equals(""))
         {
             System.out.println("Invalid argument");
         }
         else if(cmd.equals("lock"))
         {
            System.out.println("Locking file "+arg);
             cmds.lock(arg);
             System.out.println(arg+" unlocked");
         }
         else if(cmd.equals("mkdir"))
         {

            System.out.println(cmds.mkdir(arg));
         }
         else if(cmd.equals("touch"))
         {
             System.out.println(cmds.touch(arg));
         }
         else if(cmd.equals("rm"))
         {
             System.out.println(cmds.rm(arg));
         }
         else if(cmd.equals("ls"))
         {
             System.out.println(cmds.ls(arg));
         }
         else if(cmd.equals("rmdir"))
         {
             System.out.println(cmds.rmdir(arg));
         }



    }
    scanner.close();
}
else {



    cmds.configureDB();
    cmds.mkdir("/a");
    cmds.mkdir("/a/a");
    cmds.mkdir("/a/b");
    cmds.mkdir("/a/a/a");
    cmds.mkdir("/a/a/b");
    cmds.mkdir("/a/b/a");
    cmds.mkdir("/a/b/b");
    cmds.mkdir("/b");
    cmds.mkdir("/b/a");
    cmds.mkdir("/b/b");
    cmds.mkdir("/b/a/a");
    cmds.mkdir("/b/a/b");
    cmds.mkdir("/b/b/a");
    cmds.mkdir("/b/b/b");






    FileNode rootNode=cmds.getRootNode();
    StatCollector statCollector= new StatCollector();
    TimedMDS timedMDS=new TimedMDS(cmds,"Unix", statCollector);

    RandomTest tester= new RandomTest(rootNode, timedMDS);
    tester.walk(10,10);
    System.out.println("Random test finished.  ");
    /*
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
*/

}
cmds.disconnect();
    }
}
