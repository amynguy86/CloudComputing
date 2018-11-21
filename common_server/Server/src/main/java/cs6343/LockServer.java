package cs6343;

import cs6343.ceph.CephServer;
import cs6343.iface.Storage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class LockServer implements Runnable{
    private CephServer server;

    public LockServer(CephServer server){
        this.server = server;
    }

    @Override
    public void run() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(6969);
            while(true){
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