/**
 * ViewBoard.java - Displays posts within a given forum.
 */

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.sql.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;

public class ViewBoard extends HttpServlet{
	public void doGet ( HttpServletRequest request,
						HttpServletResponse response) 
				throws ServletException, IOException {
			
		response.setContentType("text/html");
		PrintWriter outStream = response.getWriter();
		
		// Get the requested board.
		String board = request.getParameter("name");
		
		// If no board was requested, redirect to the board
		// index.
		if(board == null){
			outStream.println(
				HTMLFormatter.formatElement("script", null, 
					HTMLFormatter.INDEX_REDIRECT));
			outStream.close();
			return;
		}
		
		// Head contents
		String title = HTMLFormatter.formatElement("title",null, board+" | DB Forum");
		outStream.println(HTMLFormatter.formatElement("head",null,title));
		
		try{
			// Connect, log in to Oracle DB
			Connection connection = DBConnector.connect();
			
			// Query the DB
			Statement s = connection.createStatement();
			String query = "SELECT * FROM " + 
						   "(SELECT A.tid, A.topic, A.time_created, COUNT(B.post_id) " +
								"FROM (SELECT T.tid, T.topic, T.time_created "+
										"FROM Threads T, Contains_Thread C "+
										"WHERE T.tid = C.thread_id AND C.board_name='"+board+"') A, "+
										"Contains_User_Post B " + 
							"WHERE A.tid = B.thread_id "+
							"GROUP BY A.tid, A.topic, A.time_created) Z " + 
							"ORDER BY Z.time_created";
			ResultSet r = s.executeQuery(query);
			
			// Format and send response in HTML Format
			outStream.println(HTMLFormatter.formatElement("h1", null, "DB Forum"));
			outStream.println(HTMLFormatter.formatElement("h2", null, board));
			outStream.println(HTMLFormatter.formatElement("h3", null, "Active Threads"));
			
			// Create the column headings for post display
			String[] rowAttr = null;
			String[] subRowAttr = null;
			String formattedThreads = 
				HTMLFormatter.formatElement("tr",rowAttr,
					HTMLFormatter.formatElement("td",null,"Thread") + 
					HTMLFormatter.formatElement("td",null,"Posts") + 
					HTMLFormatter.formatElement("td",null,"Date Posted")
				);
			
			while(r.next()){
				String thread_id = r.getString(1);
				String[] htmlAttributes = { "href", "'/ViewThread?id="+thread_id+"'" };
			
				String thread_link = HTMLFormatter.formatElement(
					"a", htmlAttributes, r.getString(2) );
					
				formattedThreads = formattedThreads + 
					HTMLFormatter.formatElement("tr", subRowAttr,
						HTMLFormatter.formatElement("td",null, thread_link) + 
						HTMLFormatter.formatElement("td",null, r.getString(4)) + 
						HTMLFormatter.formatElement("td",null, r.getString(3))
					);
			}
			
			// Send the formatted threads.
			String[] tableAttrs = { "width", "'50%'" };
			outStream.println(
				HTMLFormatter.formatElement("table", tableAttrs, formattedThreads)
			);
			
			// Close everything
			DBConnector.close(r,s,connection);
		} catch (Exception e){
			outStream.println("Unable to access database.");
			e.printStackTrace(outStream);
		}
		
		outStream.close();
	}
}

