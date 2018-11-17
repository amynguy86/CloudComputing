package cs6343.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cs6343.iface.Storage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import cs6343.util.Result;
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
    	Result<String> result = (Result<String>) storageSolution.executeCommand(command);
//    	Typee stringType = new TypeToken<Result<String>>(){}.getType();
        Type stringType = new TypeToken<Result<String>>(){}.getType();
        Gson gson = new Gson();
        return gson.toJsonTree(result, stringType).toString();
    }
}
