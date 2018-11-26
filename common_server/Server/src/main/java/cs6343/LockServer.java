package cs6343;

import cs6343.ceph.CephServer;
import cs6343.iface.Storage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class LockServer implements Runnable{
    private CephServer server;
    private int port;
    public LockServer(CephServer server, int port){
        this.server = server;
        this.port = port;
        System.out.println("Created Lockserver");
    }

    @Override
    public void run() {
        ServerSocket ss = null;
        System.out.println("Running Lockserver");
        try {
            ss = new ServerSocket(port);
            System.out.println("PORT: " + port);
            while(true){
                System.out.println("Reading Lockserver");
                Socket s = null;
                s = ss.accept();
                InputStreamReader isr = new InputStreamReader(s.getInputStream());
                OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
                (new Thread(new LockRequestHandler(server, isr, osw, s))).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}