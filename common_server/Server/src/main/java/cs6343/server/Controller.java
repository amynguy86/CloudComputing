package cs6343.server;

import cs6343.iface.Storage;

import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller{
	
	@Autowired
    Storage storageSolution;

    public Storage getStorageSolution() {
		return storageSolution;
	}

	public void setStorageSolution(Storage storageSolution) {
		this.storageSolution = storageSolution;
	}

	@RequestMapping("/command")
    public String command(@RequestBody String command) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	return storageSolution.executeCommand(command).toString();
    }
}
