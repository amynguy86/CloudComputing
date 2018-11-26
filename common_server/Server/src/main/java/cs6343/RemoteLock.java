package cs6343;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;

public class RemoteLock {
    private String hostname;
    private int port;
    private boolean locked;
    private Socket s;
    private OutputStreamWriter osw;
    private BufferedReader br;


    public RemoteLock(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }



    public boolean unlock(){
        if(!locked){
            throw new IllegalArgumentException("Unlock called and nothing is locked");
        }

        try {
            osw.write("unlock\n");
            osw.flush();
            String response = br.readLine();
            if(!"unlocked".equals(response)){
                return false;
            }
            osw.close();
            br.close();
            s.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean lock(String filename){
        if(locked){
            throw new IllegalArgumentException("RemoteLock already called lock");
        }
        try {
            System.out.println("Filename: " + filename + " hostname: " + hostname + " port: " + port);
            s = new Socket(hostname, port);
            osw = new OutputStreamWriter(s.getOutputStream());
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            osw.write( filename + "\n");
            osw.flush();
            String response = br.readLine();
            System.out.println(response);
            if(!"locked".equals(response)){
                return false;
            }else {
                locked = true;
                return true;
            }
        } catch (IOException ex){
            ex.printStackTrace();
            return false;
        }
    };

}
