package cs6343;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

import cs6343.centralized.CentralizedStorage;
import cs6343.ceph.CephStorage;
import cs6343.iface.Storage;

/**
 * Hello world!
 *
 */

@SpringBootApplication
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	// Setting the server type to centralized
	@ConditionalOnProperty(name = "cloud.centralized", havingValue = "true")
	@Bean
	public Storage Centralizedstorage() {
		return new CentralizedStorage();
	}

	public void createRandomStructure(Storage storage) {
		storage.mkdir("/a");
		storage.mkdir("/a/c");
		storage.mkdir("/a/d");
		storage.mkdir("/b");
		storage.mkdir("/b/e");
		storage.mkdir("/b/f");
		
		storage.mkdir("/a/c/g");
		
	}
	
	// Setting the server type to ceph
	@ConditionalOnProperty(name = "cloud.centralized", havingValue = "false")
	@Bean
	public Storage cephStorage(@Value("${cloud.ceph.root}") boolean isRoot,
			@Value("${cloud.ceph.root.server.address}") String rootServer,
			@Value("${cloud.demo}") boolean isDemo) {
		Storage st=new CephStorage(isRoot, rootServer);
		if(isDemo)
			createRandomStructure(st);
		return st ;
	}
}
