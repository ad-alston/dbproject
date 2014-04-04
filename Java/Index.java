/**
 * Index.java - Splash:  displays index of available forums.
 */

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.sql.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;

public class Index extends HttpServlet{
	public void doGet ( HttpServletRequest request,
						HttpServletResponse response) 
				throws ServletException, IOException {
			
		response.setContentType("text/html");
		PrintWriter outStream = response.getWriter();
		
		// Head contents
		String title = HTMLFormatter.formatElement("title",null,"Board Index | DB Forum");
		outStream.println(HTMLFormatter.formatElement("head",null,title));
		
		try{
			// Connect, log in to Oracle DB
			Connection connection = DBConnector.connect();
			
			// Query the DB
			Statement s = connection.createStatement();
			String query = "SELECT * FROM Boards";
			ResultSet r = s.executeQuery(query);
			
			// Format and send response in HTML Format
			outStream.println(HTMLFormatter.formatElement("h1", null, "DB Forum"));
			outStream.println(HTMLFormatter.formatElement("h2", null, "Board Index"));
			
			while(r.next()){
				String boardName = r.getString(1);
				String[] htmlAttributes = { "href", "'/ViewBoard?name="+boardName+"'" };
				outStream.println(
					HTMLFormatter.formatElement("a",htmlAttributes, boardName));
				outStream.println(HTMLFormatter.NEWLINE);
			}
			
			// Close everything
			DBConnector.close(r,s,connection);
		} catch (Exception e){
			outStream.println("Unable to access database.");
			e.printStackTrace(outStream);
		}
		
		outStream.close();
	}
}

