import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class Ciient implements Runnable{
    Map<String, ReadWriteLock> lockMap;
    DataInputStream dis;
    DataOutputStream dos;
    Socket s;

    public Ciient(Map<String, ReadWriteLock> lockMap, DataInputStream dis, DataOutputStream dos, Socket s) {
        this.lockMap = lockMap;
        this.dis = dis;
        this.dos = dos;
        this.s = s;
    }

    public void handleRequest() throws IOException {
        String stuff = dis.readUTF();
        dos.writeUTF(stuff);
        dos.flush();
        dis.close();
        dos.close();
        s.close();
    }

    @Override
    public void run() {
        try{
            handleRequest();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
