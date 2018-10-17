package server;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import cs6343.centralized.CentralizedStorage;
import cs6343.server.Controller;

public class FlowTest {
	//This should executing command RMDIR
	@Test
	public void test1() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Controller controller = new Controller();
		controller.setStorageSolution(new CentralizedStorage());
		controller.command("rmdir /something/something1");
	}
}
