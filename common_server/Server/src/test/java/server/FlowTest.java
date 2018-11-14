package server;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cs6343.centralized.CentralizedStorage;
import cs6343.server.Controller;

public class FlowTest {
	public static Logger logger = LoggerFactory.getLogger(FlowTest.class);

	// This should executing command RMDIR
	// @Test
	public void test1() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Controller controller = new Controller();
		controller.setStorageSolution(new CentralizedStorage());
		controller.command("rmdir /something/something1");
	}

	@Test
	public void testLS() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		String path = "/a//b//c";
		String[] directories = path.split("/");
		for (String d : directories) {
			logger.info("dir: " + d);
		}
	}

	//@Test
	public void lockTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InterruptedException {
		ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		// Lock By Thread 1
		readWriteLock.readLock().lock();
		readWriteLock.readLock().lock();
		logger.info("WRITE LOCL");
		readWriteLock.writeLock().lock();
		critical();
		logger.info("Count ReadLock:" + readWriteLock.getReadLockCount());
		Thread.currentThread().sleep(5000);
		readWriteLock.readLock().unlock();
		new Thread(() -> {
			logger.info("Here");
			readWriteLock.writeLock().lock();;
			critical();
			readWriteLock.writeLock().unlock();
		}).start();
	}

	public void critical() {
		logger.info("I Got inside the critical section");

	}

}
