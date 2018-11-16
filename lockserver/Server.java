import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

public class Server{

    public static void main(String[] args) throws IOException {
        Map<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();
        ServerSocket ss = new ServerSocket(6969);
        while(true){
            Socket s = null;
            s = ss.accept();
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            (new Thread(new Client(lockMap, dis, dos, s))).start();
        }
   }
}