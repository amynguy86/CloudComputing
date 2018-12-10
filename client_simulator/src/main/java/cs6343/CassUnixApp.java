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
         String arg=input.split(" ")[1];
         System.out.println("Received command "+cmd+" "+arg);
         if(cmd.equals("configure"))
         {
             cmds.configureDB();
         }
         if(cmd.equals("mkdir"))
         {
            System.out.println(cmds.mkdir(arg));
         }
         if(cmd.equals("touch"))
         {
             System.out.println(cmds.touch(arg));
         }
         if(cmd.equals("rm"))
         {
             System.out.println(cmds.rm(arg));
         }
         if(cmd.equals("ls"))
         {
             System.out.println(cmds.ls(arg));
         }
         if(cmd.equals("rmdir"))
         {
             System.out.println(cmds.rmdir(arg));
         }



    }
    scanner.close();
}
else {
    cmds.configureDB();
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
