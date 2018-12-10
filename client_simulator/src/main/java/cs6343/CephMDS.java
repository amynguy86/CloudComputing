package cs6343;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	public static Logger logger = LoggerFactory.getLogger(CephMDS.class);

	public static class CephCache {

		public static class Node {
			@Override
			public String toString() {
				return "Node [key=" + key + ", val=" + val + ", map=" + map + ", path=" + path + "]";
			}

			String key;

			public Node() {

			}

			public String getKey() {
				return key;
			}

			public void setKey(String key) {
				this.key = key;
			}

			public String getVal() {
				return val;
			}

			public void setVal(String val) {
				this.val = val;
			}

			String val;
			Map<String, Node> map;
			String path;

			public String getPath() {
				return path;
			}

			public void setPath(String path) {
				this.path = path;
			}

			Node(String key, String val) {
				this.key = key;
				this.val = val;
				map = new HashMap<>();
			}

			Node(String key, String val, String path) {
				this.key = key;
				this.val = val;
				this.path = path;
			}
		}

		private Node root;

		public CephCache(String rootServer) {
			root = new Node("/", rootServer);
		}

		public Node get(String path) {
			String[] vals = path.split("/");

			if (vals.length == 0) {
				if (!path.equals("/")) {
					throw new RuntimeException("Path:" + path + "is in wrong format");
				} else {
					return new Node(root.key, root.val, root.key);
				}
			}
			vals[0] = "/";

			Node node = root;
			Node resultNode = root;
			int j = 0;
			for (int i = 1; i < vals.length; i++) {
				node = node.map.get(vals[i]);
				if (node == null)
					break;

				if (node.val != null) {
					resultNode = node;
					j = i;
				}
			}

			String newPath = null;
			if (!resultNode.key.equals("/")) {
				String[] newPathArrAfter = Arrays.copyOfRange(vals, j + 1, vals.length);
				String[] newPathArrBefore = Arrays.copyOfRange(vals, 1, j + 1);
				newPath = "/" + String.join("/", newPathArrBefore) + "%" + String.join("/", newPathArrAfter);
			} else {
				String[] newPathArr = Arrays.copyOfRange(vals, 1, vals.length);
				newPath = resultNode.key + String.join("/", newPathArr);
			}

			Node returnVal = new Node(resultNode.key, resultNode.val, newPath);
			return returnVal;
		}

		public void remove(String pathParam) {
			if (pathParam == null)
				return;

			int idx = pathParam.indexOf('%');
			String path = pathParam;

			if (idx != -1)
				path = path.substring(0, idx);

			String[] vals = path.split("/");
			vals[0] = "/";
			Node node = root;
			int i;
			for (i = 1; i < vals.length - 1; i++) {
				if (node.map.get(vals[i]) == null) {
					node.map.put(vals[i], new Node(vals[i], null));
				}
				node = node.map.get(vals[i]);
			}

			node.map.remove(vals[i]);

		}

		public void put(String path, String val) {
			String[] vals = path.split("/");
			vals[0] = "/";
			Node node = root;
			int i;
			for (i = 1; i < vals.length - 1; i++) {
				if (node.map.get(vals[i]) == null) {
					node.map.put(vals[i], new Node(vals[i], null));
				}
				node = node.map.get(vals[i]);
			}
			Node updateNode = node.map.get(vals[i]);
			if (updateNode == null) {
				node.map.put(vals[i], new Node(vals[i], val));
			} else {
				updateNode.setVal(val);
			}
		}

	}

	public CephMDS(String rootServer) {
		this.rootServer = rootServer;
		this.cache = new CephCache(rootServer);
		this.restClient = new RestTemplate();
	}

	public CephMDS(String rootServer, RestTemplate restTemplate, CephCache cache) {
		this.rootServer = rootServer;
		this.cache = cache;
		this.restClient = restTemplate;
	}

	public boolean isRedirect(String msg) {
		if (msg == null || msg.length() == 0)
			return false;

		return msg.startsWith("REDIRECT TO SERVER");

	}

	private void updateCache(Result<String> result) {
		logger.info(result.toString());
		String serverToGo = result.getOperationReturnMessage().split("\n")[0].split(":", 2)[1];
		String path = result.getOperationReturnMessage().split("\n")[1].split(":", 2)[1];
		this.cache.put(path, serverToGo);
	}

	private boolean isWrongServerErr(String msg) {
		if (msg == null || msg.length() == 0)
			return false;

		return msg.endsWith("does not reside on this server") || msg.startsWith("There is no partition on this server");
	}

	@Override
	public boolean mkdir(String dirName) {
		return createNode(dirName, "mkdir");
	}

	public boolean createNode(String dirName, String cmd) {

		Node node = this.cache.get(dirName);
		while (true) {
			Result<String> result = restClient.postForObject("http://" + node.val + "/command", cmd + " " + node.path,
					Result.class);

			if (result.isOperationSuccess())
				return true;

			if (isRedirect(result.getOperationReturnMessage())) {
				this.updateCache(result);
				node = this.cache.get(dirName);
			} else if (isWrongServerErr(result.getOperationReturnMessage())) {
				// CacheMiss
				logger.info("CacheMiss: Server: {} Path: {}", node.val, node.path);
				this.cache.remove(node.path);
				node.path = dirName;
				node.val = this.rootServer;
			} else {
				logger.error(result.getOperationReturnMessage());
				return false;
			}
		}
	}

	@Override
	public List<String> ls(String dirName) {
		Node node = this.cache.get(dirName);
		while (true) {
			Result<String> result = restClient.postForObject("http://" + node.val + "/command", "ls " + node.path,
					Result.class);

			if (result.isOperationSuccess()) {
				return result.getOperationReturnVal().length() != 0
						? Arrays.stream(result.getOperationReturnVal().split("\n"))
								.map(x -> x.substring(x.indexOf('=') + 1, x.indexOf(']'))).collect(Collectors.toList())
						: Collections.EMPTY_LIST;
			}

			if (isRedirect(result.getOperationReturnMessage())) {
				this.updateCache(result);
				node = this.cache.get(dirName);
			} else if (isWrongServerErr(result.getOperationReturnMessage())) {
				// CacheMiss
				logger.info("CacheMiss: Server: {} Path: {}", node.val, node.path);
				this.cache.remove(node.path);
				node.path = dirName;
				node.val = this.rootServer;
			} else {
				logger.error(result.getOperationReturnMessage());
				return null;
			}

		}
	}

	@Override
	public boolean touch(String filePath) {
		return createNode(filePath, "touch");
	}

	@Override
	public boolean rm(String filePath) {
		return nodeDel(filePath, "rm");
	}

	@Override
	public boolean rmdir(String dirName) {
		return nodeDel(dirName, "rmdir");
	}

	private boolean nodeDel(String dirName, String cmd) {
		Node node = this.cache.get(dirName);
		while (true) {

			if (node.getPath().endsWith("%")) {
				int indx = node.getPath().lastIndexOf('/');
				String tmp2 = node.getPath().substring(0, indx);
				String dirToDelete = node.getPath().substring(indx + 1, node.getPath().length() - 1);
				Node previousServer = this.cache.get(node.getPath().substring(0, indx));
				String tmpPath;
				if (previousServer.getPath().endsWith("/") || previousServer.getPath().endsWith("%")) {
					tmpPath = previousServer.getPath() + dirToDelete;
				} else {
					tmpPath = previousServer.getPath() + "/" + dirToDelete;
				}

				node.setPath(tmpPath);
				node.setVal(previousServer.getVal());
			}

			Result<String> result = restClient.postForObject("http://" + node.val + "/command", cmd + " " + node.path,
					Result.class);

			if (result.isOperationSuccess())
				return true;

			if (isRedirect(result.getOperationReturnMessage())) {
				this.updateCache(result);
				node = this.cache.get(dirName);
			} else if (isWrongServerErr(result.getOperationReturnMessage())) {
				// CacheMiss
				logger.info("CacheMiss: Server: {} Path: {}", node.val, node.path);
				this.cache.remove(node.path);
				node.path = dirName;
				node.val = this.rootServer;
			} else {
				logger.error(result.getOperationReturnMessage());
				return false;
			}
		}
	}
}
