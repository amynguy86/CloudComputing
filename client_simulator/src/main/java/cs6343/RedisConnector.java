package cs6343;
import com.lambdaworks.redis.*;


public class RedisConnector {

	

		  public static void main(String[] args) {
		    RedisClient redisClient = new RedisClient(
		      RedisURI.create("redis://@192.168.29.135:9004"));
		    RedisConnection<String, String> connection = redisClient.connect();

		    System.out.println("Connected to Redis");

		    connection.close();
		    redisClient.shutdown();
		  }
	
	
	
}
