package casstest;
import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.*;
import java.nio.ByteBuffer;

//call configureDB when program first starts to create the keyspace and table.  further calls will destroy all stored data 

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
		//check if keyspace exists, if not create it
		 try {
			 session.execute("USE "+keyspaceName+";");
		 }
		 catch(InvalidQueryException e) {
			session.execute("CREATE KEYSPACE "+keyspaceName+" WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor': 3 };"); 
		 }
		 session.execute("USE "+keyspaceName+";");
		 //create a table for our data called "+tableName+"
		 session.execute("DROP TABLE IF EXISTS "+tableName+";");
		session.execute("CREATE TABLE "+tableName+" (id varchar primary key, data blob);");
	}

	public void insertBytes(String id, byte[] file)  //adds a new entry to the table which stores a byte array
	{
		session.execute("USE "+keyspaceName+";");
		String fileString=null;
		try{fileString = new String(file, "UTF-8");}
		catch(Exception e) {}
		String cmd1="INSERT INTO "+tableName+" (id,data) VALUES ('"+id+"', textasblob('"+fileString+"'));";
		SimpleStatement statement1=new SimpleStatement(cmd1);
		session.execute(statement1);
	}

	public void insertString(String id, String data)  //adds a new entry to the table which stores a string 
	{
		session.execute("USE "+keyspaceName+";");
		String cmd1="INSERT INTO "+tableName+" (id,data) VALUES ('"+id+"', textasblob('"+data+"'));";
		SimpleStatement statement1=new SimpleStatement(cmd1);
		session.execute(statement1);
	}
	
	public String readString(String id)  //returns stored string or null if file isn't in database.  
	{
		session.execute("USE "+keyspaceName+";");
		ResultSet rs = session.execute("select blobastext(data) from "+tableName+" where id='"+id+"'");
		if(!rs.isExhausted())
		{
		return rs.one().getString(0);
		}
		else return null;
	}
	
	public byte[] readBytes(String id) //returns stored byte array or null if file isn't in database
	{
		session.execute("USE "+keyspaceName+";");
		ResultSet rs = session.execute("SELECT data FROM "+tableName+" WHERE id='"+id+"'");
		if(!rs.isExhausted())
		{
			return rs.one().getBytes(0).array();
		}
		else return null;
	}
	
	public void delete(String id)  //deletes an entry from the database
	{
		session.execute("USE "+keyspaceName+";");
		session.execute("DELETE FROM "+tableName+" WHERE id='"+id+"'");

	}
	public void editString(String id, String data)  //replaces data in an existing entry with a new string
	{
		session.execute("USE "+keyspaceName+";");
		session.execute("UPDATE "+tableName+" SET data=textasblob('"+data+"') WHERE id='"+id+"'");
	}
	
	public void editBytes(String id, byte[] file)  //replaces data in an existing entry with a new array of bytes
	{
		session.execute("USE "+keyspaceName+";");
		String fileString=null;
		try{fileString = new String(file, "UTF-8");}
		catch(Exception e) {}
		session.execute("UPDATE "+tableName+" SET data=textasblob('"+fileString+"') WHERE id='"+id+"'");
	}
	
	public void shutdown()
	{
		if (cluster != null) cluster.close();
	}

}
