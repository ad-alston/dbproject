/**
 * ViewThread.java - Displays posts within a given thread.
 */

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.sql.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;

public class ViewThread extends HttpServlet{
	public void doGet ( HttpServletRequest request,
						HttpServletResponse response) 
				throws ServletException, IOException {
			
		response.setContentType("text/html");
		PrintWriter outStream = response.getWriter();
		
		// Get the requested thread.
		String thread_id = request.getParameter("id");
		
		// If no thread was requested, redirect to the board
		// index.
		if(thread_id == null){
			outStream.println(
				HTMLFormatter.formatElement("script", null, 
					HTMLFormatter.INDEX_REDIRECT));
			outStream.close();
			return;
		}
		
		try{
			// Connect, log in to Oracle DB
			Connection connection = DBConnector.connect();
			
			Statement s = connection.createStatement();
			String query = "SELECT T.topic, C.board_name, TO_CHAR(T.time_created,'Mon DD, YYYY HH24:MI') FROM Contains_Thread C, Threads T WHERE "+
							"C.thread_id="+thread_id+" AND T.tid="+thread_id;
			ResultSet r = s.executeQuery(query);
			String topic = ""; 
			String board = "";
			String boardLink = "";
			String date = "";
			
			// Get topic, board, and date
			if(r.next()){
				topic = r.getString(1);
				board = r.getString(2);
				String[] tmpAttrs = {"href", "/ViewBoard?name="+board.replace(" ","%20")};
				boardLink = HTMLFormatter.formatElement("a",
					tmpAttrs,
					"Back");
				date = r.getString(3);
			}  else{
				outStream.println(
				HTMLFormatter.formatElement("script", null, 
					HTMLFormatter.INDEX_REDIRECT));
					
				DBConnector.close(r,s,connection);
				outStream.close();
			}
			
			DBConnector.close(r,null,null);
			
			// Head contents
			String title = HTMLFormatter.formatElement("title",null, board+" - "+topic+" | DB Forum");
			outStream.println(HTMLFormatter.formatElement("head",null,title));
			
			// Format and send response in HTML Format
			outStream.println(HTMLFormatter.formatElement("h1", null, "DB Forum"));
			outStream.println(HTMLFormatter.formatElement("h2", null, board+"["+boardLink+"]"));
			outStream.println(HTMLFormatter.formatElement("h3", null, topic));
			outStream.println(HTMLFormatter.formatElement("h4", null, " - " + "Thread started: " + date));

			query = "SELECT CP.username, U.pid, TO_CHAR(U.post_time,'Mon DD, YYYY HH24:MI') , U.content "+
					"FROM Contains_User_Post C, Composed_Post CP, UserPosts U "+
					"WHERE C.thread_id = "+thread_id+" AND U.pid = C.post_id AND CP.pid = C.post_id "+
					"ORDER BY U.pid ASC";
			r = s.executeQuery(query);
			
			// Create the column headings for post display
			String[] rowAttr = null;
			String[] subRowAttr = {"style","'background-color: #F2F2F2'"};
			String formattedPosts = "";
			
			while(r.next()){
				String post_id = r.getString(2);
				
				String[] htmlAttributes = { "href", "/UserFeed?u="+r.getString(1) };
				String user_link = HTMLFormatter.formatElement(
					"a", htmlAttributes, r.getString(1).replace(" ","%20") );
					
				formattedPosts = formattedPosts + 
					HTMLFormatter.formatElement("tr", subRowAttr,
						HTMLFormatter.formatElement("td",null, user_link) + 
						HTMLFormatter.formatElement("td",null, r.getString(3) + 
							HTMLFormatter.NEWLINE + 
							HTMLFormatter.NEWLINE +
							r.getString(4))
					);
			}
			
			// Send the formatted threads.
			String[] tableAttrs = { "width", "'50%'" };
			outStream.println(
				HTMLFormatter.formatElement("table", tableAttrs, formattedPosts)
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

