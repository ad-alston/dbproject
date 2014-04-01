/**
 * Index.java - Splash:  displays index of available forums
 * plus a link to log in.
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
			
			outStream.println("Placeholder string.");
			
			outStream.close();
	}
}

