import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import java.sql.*;
import java.io.*;

public class DBConnector {
	private static final String user = "ada2145";
	private static final String password = "aapg";
	
	/**
	 * Returns a connection to the database.
	 */
	public static Connection connect() throws SQLException{
		OracleDataSource ods = new oracle.jdbc.pool.OracleDataSource();
		ods.setURL("jdbc:oracle:thin:@//w4111b.cs.columbia.edu:1521/ADB");
		ods.setUser(user);
		ods.setPassword(password);
		
		return ods.getConnection();
	}
	
	public static void close(ResultSet r, Statement s, Connection c)
			throws SQLException
	{
		if(s != null)  s.close();
		if(r != null)  r.close();
		if(c != null)  c.close();
	}
}