import java.net.URL;

import java.io.*;
import java.net.MalformedURLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.PrintWriter;

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
	private static String DB_END_POINT = "160.39.178.13";
	private final static String DB_USER_NAME = "user";
	private final static String DB_PWD = "9999813800appu";
	private final static String DB_NAME = "twitter_trigram";
	private final static int DB_PORT = 3306;
	
	public static void createConnectionAndStatement()
{
try{
// This will load the MySQL driver, each DB has its own driver
Class.forName("com.mysql.jdbc.Driver").newInstance();
// Setup the connection with the DB
con = DriverManager.getConnection("jdbc:mysql://"+DB_END_POINT+":"+DB_PORT+"/"+DB_NAME,DB_USER_NAME,DB_PWD);

// Statements allow to issue SQL queries to the database
st = con.createStatement();
} catch (Exception e) {
e.printStackTrace();
close();
}
}

	// You need to close the resultSet
private static void close() {
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

	public static void connectDatabase(){
		try {
            createConnectionAndStatement();
            
            String searchQuery  ;
            searchQuery= "SELECT * from LMI_1000_l200 where word1 = \"british\"";
            st.executeQuery(searchQuery);
            

        } catch (SQLException ex) {
            System.out.println("Exception - " + ex.getMessage());

        } finally {
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

            } catch (SQLException ex) {
                System.out.println("Exception - " + ex.getMessage());
            }
        }		
	}
	
	public static void main(String args[])
	{
		connectDatabase();
	}
}
