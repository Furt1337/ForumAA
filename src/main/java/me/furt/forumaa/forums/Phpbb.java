package me.furt.forumaa.forums;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import me.furt.forumaa.ForumAA;

public class Phpbb extends ForumHandler {

	public Phpbb(ForumAA instance) {
		super(instance);
	}

	@Override
	public boolean checkTables() throws SQLException, ClassNotFoundException {
		sqlClass();
		DatabaseMetaData dbm = sqlCon().getMetaData();
		ResultSet tables;
		tables = dbm.getTables(null, null, tablePref + "users", null);
		if (tables.next()) {
			closeCon();
			return true;
		} else {
			closeCon();
			return false;
		}
	}

	@Override
	public boolean checkCustomField() throws SQLException,
			ClassNotFoundException {
		sqlClass();
		sqlCon();
		query = "SELECT * FROM " + tablePref + "profile_fields_data LIMIT 1";
		ResultSet rs = SELECT(query);
		ResultSetMetaData meta = rs.getMetaData();
		int numCol = meta.getColumnCount();
		for (int i = 1; i < numCol + 1; i++) {
			if (meta.getColumnName(i).equals("pf_" + customField)) {
				closeCon();
				return true;
			}
		}
		closeCon();
		return false;
	}

	@Override
	public boolean saveUser(String user) throws SQLException,
			ClassNotFoundException {
		sqlClass();
		sqlCon();
		if (!customField.isEmpty()) {
			query = "SELECT user_id FROM " + tablePref
					+ "profile_fields_data WHERE pf_" + customField + "='"
					+ user + "'";
			ResultSet rs = SELECT(query);
			if (rs.next()) {
				query = "UPDATE "
						+ tablePref
						+ "users SET user_type='0', user_actkey='', user_inactive_time='0' WHERE user_id="
						+ rs.getInt("user_id");
				UPDATE(query);
				query = "SELECT * FROM " + tablePref + "users WHERE user_id='"
						+ rs.getInt("user_id") + "'";

				rs = SELECT(query);
				if (rs.next()) {
					plugin.sendInfo(ForumAA.server.getPlayer(user),
							"Activated account for " + rs.getString("username"));
					updateStats(rs.getInt("user_id"), rs.getString("username"),
							rs.getString("user_colour"), rs.getInt("group_id"));
					closeCon();
					return true;
				} else {
					plugin.sendInfo(ForumAA.server.getPlayer(user),
							"Account activated but could not update Stats");
					return true;
				}
			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Username could not be found");
				return false;
			}
		} else {
			// Update main users table
			query = "UPDATE "
					+ tablePref
					+ "users SET user_type='0', user_actkey='', user_inactive_time='0' WHERE username_clean='"
					+ user.toLowerCase() + "'";
			UPDATE(query);

			query = "SELECT * FROM "
					+ tablePref
					+ "users WHERE user_type='0' ORDER BY user_regdate DESC LIMIT 1";

			ResultSet rs = SELECT(query);

			if (rs.next()) {
				plugin.sendInfo(ForumAA.server.getPlayer(user),
						"Activated account for " + rs.getString("username"));
				updateStats(rs.getInt("user_id"), rs.getString("username"),
						rs.getString("user_colour"), rs.getInt("group_id"));
				closeCon();
				return true;
			} else {
				plugin.sendInfo(ForumAA.server.getPlayer(user),
						"Account activated but could not update Stats");
				closeCon();
				return true;
			}
		}

	}

	@Override
	public void updateStats(int id, String username, String color, int gID)
			throws SQLException, ClassNotFoundException {
		sqlClass();
		sqlCon();

		ResultSet rs;
		if (color.equals("")) {
			query = "SELECT group_colour FROM " + tablePref
					+ "groups WHERE group_id='" + gID + "'";
			rs = SELECT(query);
			if (rs.next()) {
				color = rs.getString("group_colour");
			}
		}
		query = "UPDATE " + tablePref + "config SET config_value = '" + id
				+ "' WHERE config_name = 'newest_user_id'";
		UPDATE(query);
		query = "UPDATE " + tablePref + "config SET config_value = '"
				+ username + "' WHERE config_name = 'newest_username'";
		UPDATE(query);
		query = "UPDATE " + tablePref + "config SET config_value = '" + color
				+ "' WHERE config_name = 'newest_user_colour'";
		UPDATE(query);
		query = "UPDATE "
				+ tablePref
				+ "config SET config_value = config_value+1 WHERE config_name = 'num_users'";
		UPDATE(query);
		closeCon();
	}

	@Override
	public void fieldValue() throws SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean checkExists(String user) throws SQLException,
			ClassNotFoundException {
		sqlClass();
		sqlCon();
		if (!customField.isEmpty()) {
			query = "SELECT * FROM " + tablePref
					+ "profile_fields_data WHERE pf_" + customField + "='"
					+ user + "'";
		}
		query = "SELECT * FROM " + tablePref + "users WHERE username='" + user
				+ "'";
		ResultSet rs = SELECT(query);
		closeCon();
		if (rs.next()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean checkActivated(String user) throws SQLException,
			ClassNotFoundException {
		sqlClass();
		sqlCon();
		if (!customField.isEmpty()) {
			query = "SELECT user_id FROM " + tablePref
					+ "profile_fields_data WHERE pf_" + customField + "='"
					+ user + "'";

			ResultSet rs = SELECT(query);

			if (rs.next()) {
				query = "SELECT * FROM " + tablePref + "users WHERE user_id='"
						+ rs.getInt("user_id") + "' AND user_type = '0'";

				rs = SELECT(query);

				if (rs.next()) {
					closeCon();
					return true;
				} else {
					closeCon();
					return false;
				}
			} else {
				return false;
			}
		} else {

			query = "SELECT * FROM " + tablePref + "users WHERE username='"
					+ user + "' AND user_type = '0'";
			ResultSet rs = SELECT(query);

			if (rs.next()) {
				closeCon();
				return true;
			} else {
				closeCon();
				return false;
			}
		}
	}

}
