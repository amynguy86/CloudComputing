package client_simulator;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import cs6343.CephMDS;
import cs6343.CephMDS.CephCache.Node;
import cs6343.util.Result;
import static org.mockito.Mockito.reset;

import org.junit.Assert;

public class CephMDSTest {

	@Test
	public void testRMDIR() {
		Node node = new Node();
		Result<String> result = new Result<>();
		result.setOperationSuccess(true);
		RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
		Mockito.when(restTemplate.postForObject(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
				.thenReturn(result);
		CephMDS.CephCache cache = Mockito.mock(CephMDS.CephCache.class);
		Mockito.when(cache.get(Mockito.anyString())).thenReturn(node);

		CephMDS cephClient = new CephMDS("localhost:8080", restTemplate, cache);

		node.setPath("/a/b/c/d/e");
		node.setVal("localhost:8080");
		cephClient.rmdir("/a/b/c/d/e");

		Mockito.verify(cache, Mockito.times(1)).get("/a/b/c/d/e");
		Mockito.verify(restTemplate, Mockito.times(1)).postForObject("http://localhost:8080/command",
				"rmdir /a/b/c/d/e", Result.class);
		// -------------------------------------------------------------------------------------------------------
		reset(cache);
		reset(restTemplate);
		Mockito.when(restTemplate.postForObject(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
				.thenReturn(result);
		Node node1 = new Node();
		node1.setPath("/a/b/c/d/e%");
		node1.setVal("server1");

		node.setPath("/a/b/c/d");
		node.setVal("localhost:8080");

		Mockito.when(cache.get("/a/b/c/d/e")).thenReturn(node1);
		Mockito.when(cache.get("/a/b/c/d/")).thenReturn(node);

		cephClient.rmdir("/a/b/c/d/e");

		Mockito.verify(cache, Mockito.times(1)).get("/a/b/c/d/e");
		Mockito.verify(cache, Mockito.times(1)).get("/a/b/c/d/");

		Mockito.verify(restTemplate, Mockito.times(1)).postForObject("http://localhost:8080/command",
				"rmdir /a/b/c/d/e", Result.class);
		// -------------------------------------------------------------------------------------------------------
		reset(cache);
		reset(restTemplate);
		Mockito.when(restTemplate.postForObject(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
				.thenReturn(result);
		node1 = new Node();
		node1.setPath("/a/b/c/d/e%f");
		node1.setVal("server1");

		Mockito.when(cache.get("/a/b/c/d/e/f")).thenReturn(node1);

		cephClient.rmdir("/a/b/c/d/e/f");

		Mockito.verify(cache, Mockito.times(1)).get(Mockito.anyString());

		Mockito.verify(restTemplate, Mockito.times(1)).postForObject("http://server1/command",
				"rmdir /a/b/c/d/e%f", Result.class);
	}

	@Test
	public void testCache() {
		CephMDS.CephCache cache = new CephMDS.CephCache("server1");
		Assert.assertEquals("/", cache.get("/a/b/c/d/e").getKey());
		Assert.assertEquals("server1", cache.get("/a/b/c/d/e").getVal());
		Assert.assertEquals("/a/b/c/d/e", cache.get("/a/b/c/d/e").getPath());

		cache.put("/a/b/c", "server2");
		Assert.assertEquals("c", cache.get("/a/b/c/d/e").getKey());
		Assert.assertEquals("server2", cache.get("/a/b/c/d/e").getVal());
		Assert.assertEquals("/a/b/c%d/e", cache.get("/a/b/c/d/e").getPath());

		Assert.assertEquals("/", cache.get("/a/b").getKey());
		Assert.assertEquals("server1", cache.get("/a/b").getVal());
		Assert.assertEquals("/a/b", cache.get("/a/b/").getPath());

		Assert.assertEquals("/", cache.get("/a/b/d").getKey());
		Assert.assertEquals("server1", cache.get("/a/b/d").getVal());
		Assert.assertEquals("/a/b/d", cache.get("/a/b/d").getPath());

		cache.put("/a/b", "server3");
		Assert.assertEquals("b", cache.get("/a/b/d").getKey());
		Assert.assertEquals("server3", cache.get("/a/b/d").getVal());
		Assert.assertEquals("/a/b%d", cache.get("/a/b/d").getPath());

		Assert.assertEquals("c", cache.get("/a/b/c/").getKey());
		Assert.assertEquals("server2", cache.get("/a/b/c").getVal());
		Assert.assertEquals("/a/b/c%", cache.get("/a/b/c").getPath());

		Assert.assertEquals("/", cache.get("/").getKey());
		Assert.assertEquals("server1", cache.get("/").getVal());
		Assert.assertEquals("/", cache.get("/").getPath());
	}
}
