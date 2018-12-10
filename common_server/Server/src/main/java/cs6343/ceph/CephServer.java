package cs6343.ceph;

import cs6343.LockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import cs6343.RemoteLock;
import cs6343.centralized.CentralizedStorage;
import cs6343.data.PhysicalInode;
import cs6343.iface.Inode;
import cs6343.util.Result;
import java.util.LinkedList;
import java.util.Queue;

public class CephServer {
	public static Logger logger = LoggerFactory.getLogger(CephServer.class);
	public CephStorage cephStorage;
	RemoteLock remoteLock;
	RestTemplate restTemplate;
	LockServer lockServer;

	public static class ServerRequest {
		private String command;
		private String data;

		public ServerRequest() {

		}

		public ServerRequest(String command, String data) {
			this.command = command;
			this.data = data;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}
	}

	public CephServer(CephStorage cephStorage, int port) {
		restTemplate = new RestTemplate();
		this.cephStorage = cephStorage;
		this.lockServer = new LockServer(this, port);
		new Thread(lockServer).start();
	}

	public Result<String> createPartition(String data) {
		Result<String> rslt = new Result<String>();
		rslt.setOperationSuccess(false);
		if (this.cephStorage.storage != null) {
			logger.error("There is already a partiton {} on this server", this.cephStorage.storage.getRoot().getPath());
			rslt.setOperationReturnMessage(
					"There is already a partiton on this server:" + this.cephStorage.storage.getRoot().getPath());
			return rslt;
		}
		PhysicalInode rootInode = PhysicalInode.fromJson(data);
		this.cephStorage.init(rootInode);
		rslt.setOperationSuccess(true);
		return rslt;
	}

	public Result<String> removePartition(String path) {
		logger.info("removeParition: path: "+path);
		Result<String> result;
		if (this.cephStorage.storage == null || !this.cephStorage.storage.getRoot().getPath().equals(path)) {
			logger.error("There is no partiton on this server with root path {}", path);
			result = new Result<String>();
			result.setOperationSuccess(false);
			result.setOperationReturnMessage("There is no partiton on this server with root path " + path);
		} else {
			result = findAndDeleteVirtualNodes();
			this.cephStorage.storage = null;
		}
		return result;
	}

	public Result<String> findAndDeleteVirtualNodes(){
		return findAndDeleteVirtualNodes(this.cephStorage.storage.getRoot());
	}
	
	public Result<String> findAndDeleteVirtualNodes(PhysicalInode startingNode) {
		Result<String> result= new Result<>();
		result.setOperationSuccess(true);
		result.setOperationReturnMessage("");
		Queue<Inode> queue = new LinkedList<>();
		queue.add(startingNode);
		while (!queue.isEmpty()) {
			Inode inode = queue.poll();
			if (CentralizedStorage.isPhysicalNode(inode)) {
				for (Inode tmp : ((PhysicalInode) inode).getChildren().values()) {
					queue.add(tmp);
				}
			}
			else {
				Result<String> resultDelPart=this.sendRemovePartition(inode.getPath(), inode.getServerId());
				if(!resultDelPart.isOperationSuccess()) {
					result.setOperationSuccess(false);
					result.setOperationReturnMessage(result.getOperationReturnMessage()+"|"+resultDelPart.getOperationReturnMessage());
				}
			}
		}
		
		return result;
	}

	public Result<String> sendCreatePartition(String data, String serverName) {
		Result<String> result;
		try {
			result = restTemplate.postForObject("http://" + serverName + "/ceph",
					new ServerRequest("createPartition", data), Result.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			result = new Result<>();
			result.setOperationSuccess(false);
			result.setOperationReturnMessage(ex.getMessage());
		}

		return result;
	}
	
	public Result<String> sendRemovePartition(String data, String serverName) {
		Result<String> result;
		try {
			result = restTemplate.postForObject("http://" + serverName + "/ceph",
					new ServerRequest("removePartition", data), Result.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			result = new Result<>();
			result.setOperationSuccess(false);
			result.setOperationReturnMessage(ex.getMessage());
		}
		return result;
	}
}
