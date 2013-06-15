package me.furt.forumaa.forums;

import java.sql.SQLException;

import me.furt.forumaa.ForumAA;

public abstract class ForumHandler extends SQL {

	public ForumHandler(ForumAA instance) {
		super(instance);
	}

	public abstract boolean checkTables() throws SQLException, ClassNotFoundException;

	public abstract boolean checkCustomField() throws SQLException,
	ClassNotFoundException;

	public abstract boolean saveUser(String user) throws SQLException, ClassNotFoundException;

	public abstract void updateStats(int id, String username, String color,
			int gID) throws SQLException, ClassNotFoundException;

	public abstract void fieldValue() throws SQLException, ClassNotFoundException;

	public abstract boolean checkExists(String user) throws SQLException, ClassNotFoundException;

	public abstract boolean checkActivated(String user) throws SQLException, ClassNotFoundException;

}
