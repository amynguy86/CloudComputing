package cs6343;

import java.io.IOException;

public class ClientTestMain {

    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("Give me a locktype and a file, yo!");
            System.exit(1);
        }
        RemoteLock lock = new RemoteLock("127.0.0.1", 6969);
        if("readlock".equals(args[0])){
            lock.readlock(args[1]);
        } else {
            lock.writeLock(args[1]);
        }
        System.out.println("Looks like I got a lock!");
        System.in.read();
        lock.unlock(args[1]);
        System.out.println("Looks like I unlocked the file");
    }

}