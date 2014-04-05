/**
 * ViewBoard.java - Displays posts within a given forum.
 */

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.sql.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;

public class UserFeed extends HttpServlet{
	public void doGet ( HttpServletRequest request,
						HttpServletResponse response) 
				throws ServletException, IOException {
			
		response.setContentType("text/html");
		PrintWriter outStream = response.getWriter();
		
		// Get the requested user.
		String user = request.getParameter("u");
		
		// If no user was requested, redirect to the board
		// index.
		if(user == null){
			outStream.println(
				HTMLFormatter.formatElement("script", null, 
					HTMLFormatter.INDEX_REDIRECT));
			outStream.close();
			return;
		}
		
		// Head contents
		String title = HTMLFormatter.formatElement("title",null, user+" | DB Forum");
		outStream.println(HTMLFormatter.formatElement("head",null,title));
		
		String tableContent = "";
		
		try{
			// Connect, log in to Oracle DB
			Connection connection = DBConnector.connect();
			
			// Query the DB
			Statement s = connection.createStatement();
			String query = "SELECT TO_CHAR(time_created,'Mon DD, YYYY HH24:MI') FROM Users WHERE username='"+user+"'";
			ResultSet r = s.executeQuery(query);
			
			String joinDate = r.next() ? r.getString(1) : "x";
			
			// If non-extant user, redirect to index
			if(joinDate.equals("x")){
				outStream.println(
				HTMLFormatter.formatElement("script", null, 
					HTMLFormatter.INDEX_REDIRECT));
				DBConnector.close(r,s,connection);
				outStream.close();
				return;
			}
			DBConnector.close(r,null,null);
			
			query = "SELECT COUNT(*) "+
					"FROM Composed_Post "+
					"WHERE username='"+user+"'";
			r = s.executeQuery(query);
			
			String numPosts = r.next() ? r.getString(1) : "x";
			
			DBConnector.close(r,null,null);
			
			query = "SELECT group_name FROM Is_In_Group WHERE "+
					"username='"+user+"'";
			r = s.executeQuery(query);
			
			String groups = "";
			while(r.next()){
				String[] groupHref = {"href","/ViewGroup?group="+
					r.getString(1).replace(" ","%20") };
				groups = groups + ", " + 
					HTMLFormatter.formatElement("a",groupHref, r.getString(1));
			}
			if(groups.length() >= 1){ groups = groups.substring(1); }
			else{ groups = "None"; }
			
			String[] summaryAttrs = { "width", "'20%'", "valign", "\"top\""};
			tableContent = tableContent + HTMLFormatter.formatElement(
							"td", summaryAttrs, HTMLFormatter.formatElement("h4", null, user) +
										"Date Joined: " + joinDate + 
										HTMLFormatter.NEWLINE + 
										"Posts: " + numPosts + HTMLFormatter.NEWLINE + 
										"Groups: " + groups);
			
			DBConnector.close(r,null,null);
			
			query = "SELECT U.fid, U.name, U.is_anonymous "+
					"FROM UserFeed U, Is_User_Feed_Of I "+
					"WHERE U.fid = I.fid AND I.username = '"+user+"'";
			r = s.executeQuery(query);
			
			String feedId = "";
			String feedName = "";
			String feedAnon = "";
			String formattedFeed = "";
			
			if(r.next()){
				feedId = r.getString(1);
				feedName = r.getString(2);
				feedAnon = r.getString(3);
			}
			
			// Fetch feed posts
			if(feedAnon.equals("0")){
				DBConnector.close(r,null,null);
				formattedFeed = HTMLFormatter.formatElement(
					"h2", null, feedName );
					
				String[] feedPostAttrs = {"bgcolor","'#E6E6E6'","style","'width:100%'"};
				String[] divCellAttrs = { "style","'width:400px; padding: 3px'" };
				query = "SELECT fpid, content, TO_CHAR(time_created,'Mon DD, YYYY HH24:MI')"+
						"FROM Contained_User_Feed_Post "+
						"WHERE fid="+feedId+" "+
						"ORDER BY fpid DESC";
				r = s.executeQuery(query);
				
				String feedPosts = "";
				while(r.next() && r.getMetaData().getColumnCount() >= 3){
					feedPosts = feedPosts + HTMLFormatter.formatElement(
						"tr", feedPostAttrs, HTMLFormatter.formatElement(
							"td", feedPostAttrs, 
							HTMLFormatter.formatElement("div",divCellAttrs,
								"Feed Post No. "+r.getString(1)+ HTMLFormatter.NEWLINE+
								r.getString(3) + HTMLFormatter.NEWLINE +
								HTMLFormatter.NEWLINE + r.getString(2))));
				}
				
				formattedFeed = formattedFeed + HTMLFormatter.formatElement(
					"table",null,feedPosts);
			}  else{
				formattedFeed = HTMLFormatter.formatElement(
					"h2", null, feedName ) + HTMLFormatter.formatElement(
					"h3", null, "This user's feed is private.");
			}
			
			String[] feedAttrs = {"style","'width:80%'"};
			tableContent = tableContent + HTMLFormatter.formatElement(
				"td", feedAttrs, formattedFeed );
			// Close everything
			DBConnector.close(r,s,connection);
		} catch (Exception e){
			outStream.println("Unable to access database.");
			e.printStackTrace(outStream);
		}
		String[] indHref = { "href", "/Index" };
		String index = HTMLFormatter.formatElement("h2", null,
			HTMLFormatter.formatElement("a",indHref,"Board Index"));
		
		String[] profAttrs = {"style","'width:100%'"};
		outStream.println(index+HTMLFormatter.formatElement("table",profAttrs,
			HTMLFormatter.formatElement("tr", null, tableContent)));
		outStream.close();
	}
}