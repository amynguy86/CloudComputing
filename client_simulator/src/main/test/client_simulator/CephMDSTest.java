package client_simulator;

import org.junit.Test;

import cs6343.CephMDS;

public class CephMDSTest {

	@Test
	public void testCache() {
		CephMDS.CephCache cephCache= new CephMDS.CephCache("localhost:8080");
		cephCache.treeMap.put("/a/b/c", "localhost2");
		cephCache.treeMap.put("/a/b/c/d/e", "localhost3");
		cephCache.treeMap.put("/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "localhost3");
		cephCache.treeMap.keySet().stream().forEach((k)->System.out.println(k));
		System.out.println(cephCache.treeMap.ceilingKey("/aaaaaaaaaaaaaaaa/aaaaaaaaa"));
		
		System.out.println("/".compareTo("a"));
	}
}
