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
        TreeParser p = new TreeParser();
        FileNode out = p.readFile(args[0]);
        RandomTest test = new RandomTest(out, new LoggingMDS());
        test.walk(10, 100);
    }
}