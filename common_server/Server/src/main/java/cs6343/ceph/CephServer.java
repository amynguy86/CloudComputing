package cs6343.ceph;


import cs6343.LockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import cs6343.RemoteLock;
import cs6343.data.PhysicalInode;
import cs6343.util.Result;

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
		restTemplate=new RestTemplate();
		this.cephStorage = cephStorage;
		this.lockServer = new LockServer(this, port+1);
		new Thread(lockServer).start();
	}

	/*
	 * Before this the node preceding this partition must be writelocked 
	 */
	public Result<String> createPartition(String data) {
		Result<String> rslt = new Result<String>();
		rslt.setOperationSuccess(false);
		if (this.cephStorage.storage!=null) {
			logger.error("There is already a partiton {} on this server", this.cephStorage.storage.getRoot().getPath());
			rslt.setOperationReturnMessage("There is already a partiton on this server:"+ this.cephStorage.storage.getRoot().getPath());
			return rslt;
		}
		PhysicalInode rootInode = PhysicalInode.fromJson(data);
		this.cephStorage.init(rootInode);
		rslt.setOperationSuccess(true);
		return rslt;
	}

	public void removePartition(String path) {
		if (this.cephStorage.storage == null || !this.cephStorage.storage.getRoot().getPath().equals(path)) {
			logger.error("There is no partiton on this server with path {}", path);
		}
	}

	public boolean sendCreatePartition(String data, String serverName) {
		Result<String> result=restTemplate.postForObject("http://"+serverName+"/ceph",new ServerRequest("createPartition",data) , Result.class);
		return result.isOperationSuccess();
	}
	
	/*
	 * Before this the node preceding this partition must be writelocked 
	 */
	public boolean sendRemovePartition(String data, String serverName) {
		Result<String> result=restTemplate.postForObject("http://"+serverName+"/ceph",new ServerRequest("removePartition",data) , Result.class);
		return result.isOperationSuccess();
	}
}
