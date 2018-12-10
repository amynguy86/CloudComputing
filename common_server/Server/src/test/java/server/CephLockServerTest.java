package server;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CephLockServerTest {

	//@Test
	public void test() {
		String rootNode="/";
		String path= "c/";
		List<String> parts = Arrays.asList(path.substring(rootNode.length()).split("/"));
		System.out.println(parts.get(0));
	}
	
	@Test
	public void test2() {
		String[] parts = "/c".split("/");
		String ff= "fff"+null;
		System.out.println(ff);
		System.out.println(parts.length);
	}
}
