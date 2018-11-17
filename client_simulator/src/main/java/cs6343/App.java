package cs6343;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(App.class);

    public static void main(String[] args) throws Exception{
        App app = new App();
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
//        TreeParser p = new TreeParser();
//        FileNode out = p.readFile(args[0]);
//        RandomTest test = new RandomTest(out, new LoggingMDS());
//        test.walk(10, 100);
//        test.destructiveWalk(5, 5, .25);
        CentralizedMDS mds = new CentralizedMDS("http://localhost:8080/command");
        mds.mkdir("/test");
        mds.mkdir("/test/file");
        System.out.println(mds.ls("/"));
        System.out.println(mds.ls("/test"));
    }
}