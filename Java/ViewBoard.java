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
			String query = "SELECT T2.tid, T2.topic, C2.username as author, T2.num_posts, TO_CHAR(U2.post_time,'Mon DD, YYYY HH24:MI') as last_post, C3.username as last_poster "+
							"FROM "+
								"(SELECT T1.tid, T1.topic, MAX(post_id) as last_pid, MIN(post_id) as auth_pid, COUNT(C1.post_id) as num_posts "+
								"FROM "+
									"(SELECT T.tid, T.topic "+
									"FROM Threads T, Contains_Thread C "+
									"WHERE T.tid = C.thread_id and C.board_name='"+board+"') T1, "+
									"Contains_User_Post C1 "+
								"WHERE C1.thread_id = T1.tid "+
								"GROUP BY T1.tid, T1.topic) T2, "+
								"UserPosts U1, UserPosts U2, Composed_Post C2, Composed_Post C3 "+
							"WHERE U1.pid = T2.auth_pid and U1.pid = C2.pid and U2.pid = last_pid AND U2.pid = C3.pid "+
							"ORDER BY U2.post_time DESC";
			ResultSet r = s.executeQuery(query);
			
			// Format and send response in HTML Format
			String[] indexAttrs = { "href", "/Index" };
			outStream.println(HTMLFormatter.formatElement("h1", null, "DB Forum"));
			outStream.println(HTMLFormatter.formatElement("h2", null, board + "["+
								HTMLFormatter.formatElement("a", indexAttrs, "Index") +  "]"));
			outStream.println(HTMLFormatter.formatElement("h3", null, "Active Threads"));
			
			// Create the column headings for post display
			String[] rowAttr = null;
			String[] subRowAttr = null;
			String formattedThreads = 
				HTMLFormatter.formatElement("tr",rowAttr,
					HTMLFormatter.formatElement("td",null,"Topic") + 
					HTMLFormatter.formatElement("td",null,"Author") + 
					HTMLFormatter.formatElement("td",null,"Posts") + 
					HTMLFormatter.formatElement("td",null,"Last Post")
				);
			
			while(r.next()){
				String thread_id = r.getString(1);
				String[] htmlAttributes = { "href", "'/ViewThread?id="+thread_id+"'" };
			
				String thread_link = HTMLFormatter.formatElement(
					"a", htmlAttributes, r.getString(2) );
					
				formattedThreads = formattedThreads + 
					HTMLFormatter.formatElement("tr", subRowAttr,
						HTMLFormatter.formatElement("td",null, thread_link) + 
						HTMLFormatter.formatElement("td",null, r.getString(3)) + 
						HTMLFormatter.formatElement("td",null, r.getString(4)) +
						HTMLFormatter.formatElement("td",null, r.getString(6) + 
							" on " +r.getString(5))
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

