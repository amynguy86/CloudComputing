package cs6343;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket; import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;


public class Server{

    public static void main(String[] args) throws IOException {
        ConcurrentHashMap<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();
        ServerSocket ss = new ServerSocket(6969);
        while(true){
            Socket s = null;
            s = ss.accept();
            InputStreamReader isr = new InputStreamReader(s.getInputStream());
            OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
            (new Thread(new RequestHandler(lockMap, isr, osw, s))).start();
        }
   }
}