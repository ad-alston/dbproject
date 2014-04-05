/**
 * ViewBoard.java - Displays posts within a given forum.
 */

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.sql.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;

public class ViewGroup extends HttpServlet{
	public void doGet ( HttpServletRequest request,
						HttpServletResponse response) 
				throws ServletException, IOException {
			
		response.setContentType("text/html");
		PrintWriter outStream = response.getWriter();
		
		// Get the requested board.
		String group = request.getParameter("group");
		
		// If no group was requested, redirect to the board
		// index.
		if(group == null){
			outStream.println(
				HTMLFormatter.formatElement("script", null, 
					HTMLFormatter.INDEX_REDIRECT));
			outStream.close();
			return;
		}
		
		// Head contents
		String title = HTMLFormatter.formatElement("title",null, group+" | DB Forum");
		outStream.println(HTMLFormatter.formatElement("head",null,title));
		
		try{
			// Connect, log in to Oracle DB
			Connection connection = DBConnector.connect();
			
			// Query the DB
			Statement s = connection.createStatement();
			String query = "SELECT description, TO_CHAR(date_created,'Mon DD, YYYY HH24:MI') "+
							"FROM UserGroups "+
							"WHERE name='"+group+"'";
			ResultSet r = s.executeQuery(query);
			
			String desc = "";
			String date = "";
			
			if(r.next()&&r.getMetaData().getColumnCount() >= 2){
				desc = r.getString(1);
				date = r.getString(2);
			}  else{
				// Redirect to index if not valid group
				outStream.println(HTMLFormatter.INDEX_REDIRECT);
				
				DBConnector.close(r,s,connection);
				outStream.close();
				return;
			}
			
			// Format and send response in HTML Format
			String[] indexAttrs = { "href", "/Index" };
			outStream.println(HTMLFormatter.formatElement("h1", null, "DB Forum["+
				HTMLFormatter.formatElement("a",indexAttrs,"Board Index")+"]"));
			outStream.println(HTMLFormatter.formatElement( "h2", null, "User Group: " + group ));
			outStream.println(HTMLFormatter.formatElement("h3", null, desc));
			
			DBConnector.close(r,null,null);
			
			query = "SELECT COUNT(*) FROM Is_In_Group WHERE group_name='"+group+"'";
			r = s.executeQuery(query);
			
			String memberCt = r.next() ? r.getString(1) : "0";
			String members = "";
			
			DBConnector.close(r,null,null);
			
			query = "SELECT username FROM Is_In_Group WHERE group_name='"+group+"'";
			r = s.executeQuery(query);
			
			while(r.next()){
				String[] membHref = {"href","UserFeed?u="+r.getString(1).replace(" ","%20")};
				members = members + HTMLFormatter.formatElement("a",membHref,
					r.getString(1)) + ", ";
			}
			if(members.length() > 2){
				members = members.substring(0,members.length()-2);
			} else{ members = "None"; }
			
			outStream.println(HTMLFormatter.NEWLINE+
				"Members ("+memberCt+"): "+members);
			// Close everything
			DBConnector.close(r,s,connection);
		} catch (Exception e){
			outStream.println("Unable to access database.");
			e.printStackTrace(outStream);
		}
		
		outStream.close();
	}
}