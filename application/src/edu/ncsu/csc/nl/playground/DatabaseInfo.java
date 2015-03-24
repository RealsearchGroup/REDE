package edu.ncsu.csc.nl.playground;

import java.sql.*;

public class DatabaseInfo {

	public static void main(String args[]) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:mysql://152.46.18.47:3306/itrust2?user=root&password=root");
			conn.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
	}
}
