package edu.columbia.watson.project3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCInterface {
	static Connection con = null;
	static Statement st = null;
	static ResultSet rs = null;
	static ResultSet rs1 = null;
	static String url = "jdbc:mysql://localhost:3306/twitter_trigram";
	static String user = "root";
	static String password = "9999813800appu";
	private static String DB_END_POINT ="localhost";
	private final static String DB_USER_NAME = "root";
	private final static String DB_PWD = "9999813800appu";
	private final static String DB_NAME = "twitter_trigram";
	private final static int DB_PORT = 3306;

	public static void createConnectionAndStatement()
	{
		try
		{
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			// Setup the connection with the DB
			String connUrl = "jdbc:mysql://"+DB_END_POINT+":"+DB_PORT+"/"+DB_NAME ;
			System.out.println(connUrl);
			con = DriverManager.getConnection(connUrl,DB_USER_NAME,DB_PWD);

			// Statements allow to issue SQL queries to the database
			st = con.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

	// You need to close the resultSet
	public static void close() {
		try {
			if (rs != null) {
				rs.close();
			}

			if (st != null) {
				st.close();
			}

			if (con != null) {
				con.close();
			}
		} catch (Exception e) {

		}
	}

	public static String searchQuery(String queryTerm){
		try 
		{
			String searchQuery = "SELECT word1,word2,sum(count) from LMI_1000_l200 where UPPER(word1) = UPPER(\""+queryTerm+"\") group by UPPER(word1), UPPER(word2)";
			
			System.out.println(searchQuery);
			st.executeQuery(searchQuery);
			//int count = 0;
			String output = "";
			rs = st.getResultSet();
			while (rs.next()) {
				String origWord = rs.getString(1);
				String simWord = rs.getString(2);
				int score = rs.getInt(3);
				//System.out.println(origWord + "," + simWord + "," + score);
				//count ++;
				output = output + origWord + "," + simWord + "," + score + "\n";
			}
			//output = queryId+","+count + "\n" + output;
			
			return output;

		} catch (SQLException ex) {
			System.out.println("Exception - " + ex.getMessage());
			close();
			return null;
		}
		finally {
		}		
	}
	
	public static int getQueryCount(String queryTerm){
		try 
		{
			int score = 0;
			String searchQuery = "SELECT count(*) from (SELECT count(*) from LMI_1000_l200 where UPPER(word1) = UPPER(\""+queryTerm+"\") group by UPPER(word1), UPPER(word2)) as g";
			st.executeQuery(searchQuery);
			rs = st.getResultSet();
			while (rs.next()) {
				score = rs.getInt(1);
			}
			
			return score;

		} catch (SQLException ex) {
			System.out.println("Exception - " + ex.getMessage());
			close();
			return 0;
		}
		finally {
		}		
	}

	public static void main(String args[])
	{
		createConnectionAndStatement();
		String queryOutput = searchQuery("british");
		System.out.println(queryOutput);
		//System.out.println(getQueryCount("british"));
		close();
	}
}
