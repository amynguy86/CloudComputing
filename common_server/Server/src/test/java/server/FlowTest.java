package server;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs6343.centralized.CentralizedStorage;
import cs6343.server.Controller;

public class FlowTest {
	public static Logger logger = LoggerFactory.getLogger(FlowTest.class);

	//This should executing command RMDIR
	//@Test
	public void test1() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Controller controller = new Controller();
		controller.setStorageSolution(new CentralizedStorage());
		controller.command("rmdir /something/something1");
	}
	
	@Test
	public void testLS() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String path= "/a//c";
		String[] directories = path.split("/");
		for(String d:directories) {
			logger.info(d);
		}
		/*
		Controller controller = new Controller();
		controller.setStorageSolution(new CentralizedStorage());
		logger.info(controller.command("mkdir /amin"));
		logger.info(controller.command("mkdir /amin/ali"));
		logger.info(controller.command("mkdir /ali"));
		logger.info(controller.command("ls /amin"));
		logger.info(controller.command("ls /"));
		logger.info(controller.command("rmdir /amin/ali"));
		logger.info(controller.command("ls /amin"));
		logger.info(controller.command("rmdir /amin"));
		logger.info(controller.command("ls /"));
		*/
	}
}
