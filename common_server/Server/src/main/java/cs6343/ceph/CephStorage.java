package cs6343.ceph;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cs6343.centralized.CentralizedStorage;
import cs6343.iface.Storage;
import cs6343.util.Result;

public class CephStorage extends Storage {
	private Map<String, CentralizedStorage> map; //path mapping to the directory tree
	
	//map= /a/b/c=> node C   /a/c/a => node A previous path /a/c/a = > a/c/a||/d/e/f
	
	///a/b/d/(e)  a/b/d/e/f/(e)
	public CephStorage() {
		map = new ConcurrentHashMap<>(); 
		map.put("/", new CentralizedStorage());
	}
	
	public Result<String> sendRedirectRequest(String serverIp){
		return null;
	}
	
	@Override
	public Result<String> ls(String path) {
		CentralizedStorage storage = map.get(path);
		storage=storage==null? map.get("/") : storage;
		Result resultFromStorage = storage.ls(path);
		
		Result<String> result=null; 
		return result;
	}
	

	@Override
	public Result<String> mkdir(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> rmdir(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> add(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> chmod(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> rm(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<String> mv(String path) {
		// TODO Auto-generated method stub
		return null;
	}
}
