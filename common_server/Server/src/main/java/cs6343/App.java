package cs6343;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import cs6343.centralized.CentralizedStorage;
import cs6343.iface.Storage;

/**
 * Hello world!
 *
 */

@SpringBootApplication
public class App 
{
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
   
    //Setting the server type to centralized
    @Bean 
    public Storage storage() {
    	return new CentralizedStorage();
    }
}
