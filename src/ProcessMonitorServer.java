

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ProcessMonitorServer
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/ProcessMonitorServer" })
public class ProcessMonitorServer extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	private String dbUserName = "processTracker";
	private String password = "GLRchSC9NL81UIRc";
	private String address = "jdbc:mysql://localhost:3306";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProcessMonitorServer()
    {
        super();
    }

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		InputStream inStream = request.getInputStream();
		//InputStreamReader myReader = new InputStreamReader(inStream);
		//int numProcesses = myReader.read();
		//response.getWriter().println("Got "+numProcesses);
		ObjectInputStream objInStream = new ObjectInputStream(inStream);
		int numProcesses = 0;
		try
		{
			numProcesses = (Integer) objInStream.readObject();
		}
		catch(ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		response.getWriter().println("Got "+numProcesses);
		int numRead = 0;
		String userName = request.getRemoteAddr().toString();
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection newConnection = DriverManager.getConnection(address, dbUserName, password);
			for(int x=0; x<numProcesses; x++)
			{
				HashMap tmp = null;
				try
				{
					tmp = (HashMap) objInStream.readObject();
					String insertStatement = "INSERT INTO `processTracking`.`process`(`USER`, `PID`, `%CPU`, `%MEM`, `VSZ`, `RSS`, `TTY`, `STAT`, `START`, `TIME`, `COMMAND`, `statusChange`, `highLevelUser`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					PreparedStatement nextStmt = newConnection.prepareStatement(insertStatement);
					nextStmt.setObject(1, tmp.get("USER"));
					nextStmt.setObject(2, tmp.get("PID"));
					nextStmt.setObject(3, tmp.get("%CPU"));
					nextStmt.setObject(4, tmp.get("%MEM"));
					nextStmt.setObject(5, tmp.get("VSZ"));
					nextStmt.setObject(6, tmp.get("RSS"));
					nextStmt.setObject(7, tmp.get("TTY"));
					nextStmt.setObject(8, tmp.get("STAT"));
					nextStmt.setObject(9, tmp.get("START"));
					nextStmt.setObject(10, tmp.get("TIME"));
					nextStmt.setObject(11, tmp.get("COMMAND"));
					nextStmt.setObject(12, tmp.get("statusChange"));
					nextStmt.setObject(13, userName);
					nextStmt.executeUpdate();
					
					numRead++;
					//response.getWriter().println(tmp.toString());
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
					//System.err.println(tmp.get("USER"));
				}
			}
			newConnection.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		response.getWriter().println("Wrote "+numRead+" entries from "+userName);
	}

}
