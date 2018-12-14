package cs6343;

import java.util.Scanner;
public class CassUnixApp {
    public static void main(String[] args){

CassandraMDS cmds = new CassandraMDS("127.0.0.1");  //IP address for a node on the cassandra server


if(args.length!=0 && args[0].equals("commandline"))
{
Scanner scanner=new Scanner(System.in);
String input;
    while (!(input = scanner.nextLine()).equals("exit"))
     {
         String cmd=input.split(" ")[0];
         String arg="";
         if(input.split(" ").length>=2) {
            int spaceIndex=input.indexOf(" ");
            arg=input.substring(spaceIndex+1);
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

            cmds.mkdir(arg);
         }
         else if(cmd.equals("touch"))
         {
             cmds.touch(arg);
         }
         else if(cmd.equals("rm"))
         {
             cmds.rm(arg);
         }
         else if(cmd.equals("ls"))
         {
             System.out.println(cmds.ls(arg));
         }
         else if(cmd.equals("rmdir"))
         {
             cmds.rmdir(arg);
         }

    }
    scanner.close();

}
else {



    cmds.configureDB();
    cmds.disableMessages();
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
    System.out.println(statCollector.getSummaryStatistics(Operation.LS));
    System.out.println(statCollector.getSummaryStatistics(Operation.MKDIR));
    System.out.println(statCollector.getSummaryStatistics(Operation.TOUCH));
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
