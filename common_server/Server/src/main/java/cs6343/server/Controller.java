package cs6343.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cs6343.ceph.CephServer;
import cs6343.ceph.CephStorage;
import cs6343.iface.Storage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import cs6343.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	@Autowired
	Storage storageSolution;

	public Storage getStorageSolution() {
		return storageSolution;
	}

	public void setStorageSolution(Storage storageSolution) {
		this.storageSolution = storageSolution;
	}

	@RequestMapping("/command")
	public String command(@RequestBody String command) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Result<String> result = (Result<String>) storageSolution.executeCommand(command);
		// Typee stringType = new TypeToken<Result<String>>(){}.getType();
		Type stringType = new TypeToken<Result<String>>() {
		}.getType();
		Gson gson = new Gson();
		return gson.toJsonTree(result, stringType).toString();
	}

	@RequestMapping("/commandWithDelay")
	public String commandWithDelay(@RequestBody String command) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Result<String> result = (Result<String>) storageSolution.executeCommandWithDelay(command);
		// Typee stringType = new TypeToken<Result<String>>(){}.getType();
		Type stringType = new TypeToken<Result<String>>() {
		}.getType();
		Gson gson = new Gson();
		return gson.toJsonTree(result, stringType).toString();
	}

	/*
	 * For Ceph LockServer to LockServer communication
	 */
	@ConditionalOnProperty(name = "cloud.centralized", havingValue = "false")
	@RequestMapping("/ceph")
	public Result<String> cephCommand(@RequestBody CephServer.ServerRequest command) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Result<String> result = (Result<String>) ((CephStorage) storageSolution).cephServerRqst(command);
		return result;
	}
	
	@RequestMapping("/print")
	public String print() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		storageSolution.print();
		return "Success";
	}
	
}
