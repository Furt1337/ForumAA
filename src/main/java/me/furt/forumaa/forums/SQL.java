package me.furt.forumaa.forums;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.furt.forumaa.ForumAA;

public class SQL {
	// Initialize database variables
	public String url, username, password, tablePref, database, query, port,
			forumType, customField = null;
	public ForumAA plugin;

	public SQL(ForumAA instance) {
		this.plugin = instance;
	}

	// Get SQL Class
	public Class<?> sqlClass() throws ClassNotFoundException {
		return Class.forName("com.mysql.jdbc.Driver");
	}

	// Get Database connection
	public Connection sqlCon() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://" + url + ":" + port
				+ "/" + database, username, password);
	}

	// Creater statement
	public Statement createS() throws SQLException {
		Statement stmt = sqlCon().createStatement();
		return stmt;
	}

	// Get ResulSet for SELECT statment
	public ResultSet SELECT(String getQuery) throws SQLException {
		return createS().executeQuery(getQuery);
	}

	// Execute INSERT statement
	public void INSERT(String getQuery) throws SQLException {
		Statement stmt = sqlCon().createStatement();
		stmt.execute(getQuery);
	}

	// Execute DELETE statement
	public void DELETE(String getQuery) throws SQLException {
		Statement stmt = sqlCon().createStatement();
		stmt.execute(getQuery);
	}

	public void UPDATE(String getQuery) throws SQLException {
		Statement stmt = sqlCon().createStatement();
		stmt.execute(getQuery);
	}

	// Close MySQL Connection
	public void closeCon() throws SQLException {
		sqlCon().close();
	}

}
