package cs6343;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import cs6343.CephMDS.CephCache.Node;
import cs6343.util.Result;

public class CephMDS implements IMetaData {
	private RestTemplate restClient;
	private String rootServer;
	private CephCache cache;
	private int numTries = 10;
	public static Logger logger = LoggerFactory.getLogger(CephMDS.class);

	public static class CephCache {

		public class Node {
			String key;
			String val;
			HashMap<String, Node> map;

			Node(String key, String val) {
				this.key = key;
				this.val = val;
				map = new HashMap<>();
			}
		}

		private Node root;

		CephCache(String rootServer) {
			root = new Node("/", rootServer);
		}

		public Node get(String path) {
			String[] vals = path.split("/");
			vals[0] = "/";

			Node node = root;
			Node resultNode = root;
			int i;
			for (i = 1; i < vals.length; i++) {
				node = node.map.get(vals[i]);
				if (node == null)
					break;

				if (node.val != null) {
					resultNode = node;
				}
			}

			String newPath = null;
			if (!resultNode.key.equals("/")) {
				String[] newPathArr = Arrays.copyOfRange(vals, i, vals.length);
				newPath = resultNode.key + "%" + String.join("/", newPathArr);
			} else {
				String[] newPathArr = Arrays.copyOfRange(vals, 1, vals.length);
				newPath = resultNode.key + String.join("/", newPathArr);
			}

			return new Node(newPath, resultNode.val);
		}

		public void remove(String path) {
			String[] vals = path.split("/");
			vals[0] = "/";
			Node node = root;
			int i;
			for (i = 1; i < vals.length - 1; i++) {
				node = node.map.get(vals[i]);
				if (node == null) {
					node.map.put(vals[i], new Node(vals[i], null));
					node = node.map.get(vals[i]);
				}
			}

			node.map.remove(vals[i]);

		}

		public void put(String path, String val) {
			String[] vals = path.split("/");
			vals[0] = "/";
			Node node = root;
			int i;
			for (i = 1; i < vals.length - 1; i++) {
				node = node.map.get(vals[i]);
				if (node == null) {
					node.map.put(vals[i], new Node(vals[i], null));
					node = node.map.get(vals[i]);
				}
			}
			node.map.put(vals[i], new Node(vals[i], val));
		}

	}

	public CephMDS(String rootServer) {
		this.rootServer = rootServer;
		this.cache = new CephCache(rootServer);
		this.restClient = new RestTemplate();
	}

	public CephMDS(String rootServer, RestTemplate restTemplate) {
		this.rootServer = rootServer;
		this.cache = new CephCache(rootServer);
		this.restClient = restTemplate;
	}

	public boolean isRedirect(String msg) {
		if (msg.length() == 0)
			return false;

		return msg.startsWith("REDIRECT TO SERVER");

	}

	private void updateCache(Result<String> result) {
		String serverToGo = result.getOperationReturnMessage().split("\n")[0].split(":", 2)[1];
		String path = result.getOperationReturnMessage().split("\n")[1].split(":", 2)[1];
		this.cache.put(path, serverToGo);
	}

	private boolean isWrongServerErr(String msg) {
		if (msg.length() == 0)
			return false;

		return msg.endsWith("does not reside on this server");

	}

	@Override
	public boolean mkdir(String dirName) {

		Node node = this.cache.get(dirName);
		for (int i = 0; i < this.numTries; i++) {
			Result<String> result = restClient.postForObject("http://" + node.val + "/command", "mkdir " + node.key,
					Result.class);

			if (result.isOperationSuccess())
				return true;

			if (isRedirect(result.getOperationReturnMessage())) {
				this.updateCache(result);
				node = this.cache.get(dirName);
			} else if (isWrongServerErr(result.getOperationReturnMessage())) {
				// CacheMiss
				logger.info("CacheMiss: Server: {} Path: {}", node.val, node.key);
				node.key = dirName;
				node.val = this.rootServer;
			}

		}
		return false;
	}

	@Override
	public List<String> ls(String dirName) {
		Node node = this.cache.get(dirName);
		for (int i = 0; i < this.numTries; i++) {
			Result<String> result = restClient.postForObject("http://" + node.val + "/command", "mkdir " + node.key,
					Result.class);

			if (result.isOperationSuccess())
				return Arrays.stream(result.getOperationReturnVal().split("/n")).map(x -> x.substring(x.indexOf('=')+1, x.indexOf(']'))).collect(Collectors.toList());


			if (isRedirect(result.getOperationReturnMessage())) {
				this.updateCache(result);
				node = this.cache.get(dirName);
			} else if (isWrongServerErr(result.getOperationReturnMessage())) {
				// CacheMiss
				logger.info("CacheMiss: Server: {} Path: {}", node.val, node.key);
				node.key = dirName;
				node.val = this.rootServer;
			}

		}
		return null;
	}

	@Override
	public boolean touch(String filePath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rm(String filePath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rmdir(String dirname) {
		// TODO Auto-generated method stub
		return false;
	}

}
