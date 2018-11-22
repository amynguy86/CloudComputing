package cs6343;
import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.*;
import java.nio.ByteBuffer;

//call configureDB when program first starts to create the keyspace and table.  further calls will destroy all stored data
//call shutdown when done
public class CassConnector {
	Cluster cluster = null;
	Session session=null;
	String tableName="table1";
	String keyspaceName="keyspace1";
	public CassConnector(String ipAddress) //the IP address of a node in the cassandra system, assumes default port 9042
	{
		try {
			cluster = Cluster.builder().addContactPoint(ipAddress).build();
			session = cluster.connect();
		}
		catch(Exception e) {}
	}

	public void configureDB()
	{
		session.execute("DROP KEYSPACE IF EXISTS "+keyspaceName+";");
		//replication factor must be <= number of nodes active or some calls will not work
		session.execute("CREATE KEYSPACE "+keyspaceName+" WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor': 1 };");
		session.execute("USE "+keyspaceName+";");
		//create a table for our data called [tableName]
		session.execute("DROP TABLE IF EXISTS "+tableName+";");
		session.execute("CREATE TABLE "+tableName+" (id varchar primary key, data blob);");
	}

	public boolean insert(String id, String data)  //adds a new entry to the table which stores a string
	{
		session.execute("USE "+keyspaceName+";");
		String cmd="INSERT INTO "+tableName+" (id,data) VALUES ('"+id+"', textasblob('"+data+"')) IF NOT EXISTS;";
		ResultSet rs=session.execute(cmd);
		return rs.wasApplied();
	}

	public String read(String id)  //returns stored string or null if file isn't in database.
	{
		session.execute("USE "+keyspaceName+";");
		ResultSet rs = session.execute("select blobastext(data) from "+tableName+" where id='"+id+"'");
		if(!rs.isExhausted())
		{
			return rs.one().getString(0);
		}
		else return null;
	}


	public boolean delete(String id)  //deletes an entry from the database
	{
		session.execute("USE "+keyspaceName+";");
		ResultSet rs=session.execute("DELETE FROM "+tableName+" WHERE id='"+id+"' IF EXISTS");
		return rs.wasApplied();
	}

	public void edit(String id, String data)  //replaces data in an existing entry with a new string
	{
		session.execute("USE "+keyspaceName+";");
		session.execute("UPDATE "+tableName+" SET data=textasblob('"+data+"') WHERE id='"+id+"'");
	}

	public void shutdown()
	{
		if (cluster != null) cluster.close();
	}

}
