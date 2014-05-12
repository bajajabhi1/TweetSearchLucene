package edu.columbia.watson.project3;

import java.net.URL;

import java.awt.List;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCInterface {
	Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    ResultSet rs1 = null;
    String url = "jdbc:mysql://localhost:3306/twitter_trigram";
    String user = "root";
    String password = "9999813800appu";
	public void connectDatabase(){
		try {
            con = DriverManager.getConnection(url, user, password);
            
            st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            
            String searchQuery  ;
            searchQuery= "SELECT * from LMI_1000_l200 where word1 = \"british\"";
            st.executeUpdate(searchQuery);
            

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Version.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

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
                Logger lgr = Logger.getLogger(Version.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
		
		
	}
}
