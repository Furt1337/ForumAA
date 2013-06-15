package me.furt.forumaa.forums;

import java.sql.SQLException;

import me.furt.forumaa.ForumAA;

public class Mybb extends ForumHandler {

	public Mybb(ForumAA instance) {
		super(instance);
	}

	@Override
	public boolean checkTables() throws SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkCustomField() throws SQLException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveUser(String user) throws SQLException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateStats(int id, String username, String color, int gID)
			throws SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fieldValue() throws SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkExists(String user) throws SQLException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkActivated(String user) throws SQLException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

}
