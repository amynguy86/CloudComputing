package cs6343;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = CassandraDataAutoConfiguration.class)

public class App {

	@Autowired
	IMetaData client;

	@Autowired
	CommandLine commandLine;

	@Autowired
	StatCollector collector;

	@Value("${cloud.command.line}")
	boolean isCommandline;

	private static Logger LOG = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public StatCollector collector() {
		return new StatCollector();
	}

	@Bean
	public IMetaData client(@Value("${cloud.client.type}") String clientType, @Value("${server}") String serverAddress,
			StatCollector collector) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> clientClass = Class.forName(clientType);
		Constructor constructor = clientClass.getConstructor(String.class);
		return new TimedMDS((IMetaData) constructor.newInstance(serverAddress), clientType, collector);
	}

	@PostConstruct
	public void begin() throws Exception {
		if (!isCommandline) {
			// TreeParser p = new TreeParser();
			// FileNode out = p.readFile(args[0]);
			// RandomTest test = new RandomTest(out, new LoggingMDS());
			// test.walk(10, 100);
			// test.destructiveWalk(5, 5, .25);
			// CentralizedMDS mds = new CentralizedMDS("localhost:8080");
			client.mkdir("/test");
			client.mkdir("/test/file");
			System.out.println(client.ls("/"));
			System.out.println(client.ls("/test"));
			System.out.println(collector.getSummaryStatistics(Operation.LS));
			System.out.println(collector.getSummaryStatistics(Operation.MKDIR));
			System.out.println(collector.getSummaryStatistics(Operation.TOUCH));
			System.out.println(collector.getSummaryStatistics(Operation.RM));
			System.out.println(collector.getSummaryStatistics(Operation.RMDIR));
			System.exit(0);
		}
		else
			commandLine.begin();
	}
}