package me.furt;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLQuery {

	// Initialize database variables
	String url, username, password, tablePref, database, query, port,
			forumType, customField = null;
	private ForumAA plugin;

	public SQLQuery(ForumAA instance) {
		this.plugin = instance;
	}

	public boolean checkTables() throws SQLException, ClassNotFoundException {
		sqlClass();
		DatabaseMetaData dbm = sqlCon().getMetaData();
		ResultSet tables;
		if (forumType.equalsIgnoreCase("xenforo")) {
			tables = dbm.getTables(null, null, tablePref + "user", null);
		} else if (forumType.equalsIgnoreCase("xenforo")
				|| forumType.equalsIgnoreCase("ipb")) {
			tables = dbm.getTables(null, null, tablePref + "members", null);
		} else {
			tables = dbm.getTables(null, null, tablePref + "users", null);
		}
		if (tables.next()) {
			closeCon();
			return true;
		} else {
			closeCon();
			return false;
		}
	}

	public boolean checkCustomColumn() throws SQLException,
			ClassNotFoundException {
		sqlClass();
		sqlCon();

		if (forumType.equalsIgnoreCase("phpbb")) {
			query = "SELECT * FROM " + tablePref
					+ "profile_fields_data LIMIT 1";
		} else if (forumType.equalsIgnoreCase("mybb")) {
			query = "SELECT * FROM " + tablePref + "userfields LIMIT 1";
		} else if (forumType.equalsIgnoreCase("xenforo")) {
			query = "SELECT * FROM " + tablePref
					+ "user_field WHERE field_id ='" + customField + "'";
			ResultSet rs = SELECT(query);
			if (rs != null) {
				closeCon();
				return true;
			}
			closeCon();
			return false;
		} else if (forumType.equalsIgnoreCase("ipb")) {
			query = "SELECT * FROM " + tablePref + "pfields_data";
		} else if (forumType.equalsIgnoreCase("smf")) {
			query = "SELECT * FROM " + tablePref
					+ "custom_fields WHERE field_name ='" + customField + "'";
			ResultSet rs = SELECT(query);
			if (rs != null) {
				closeCon();
				return true;
			}
			closeCon();
			return false;
		}

		ResultSet rs = SELECT(query);
		ResultSetMetaData meta = rs.getMetaData();
		int numCol = meta.getColumnCount();

		for (int i = 1; i < numCol + 1; i++) {
			if (forumType.equalsIgnoreCase("phpbb")) {
				if (meta.getColumnName(i).equals("pf_" + customField)) {
					closeCon();
					return true;
				}
			} else if (forumType.equalsIgnoreCase("mybb")) {
				if (meta.getColumnName(i).equals("fid" + customField)) {
					closeCon();
					return true;
				}
			} else if (forumType.equalsIgnoreCase("ipb")) {
				if (meta.getColumnName(i).equals("field_" + customField)) {
					closeCon();
					return true;
				}
			}
		}
		closeCon();
		return false;
	}

	public void savePhpbbUser(String user) throws SQLException,
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
				} else {
					plugin.sendInfo(ForumAA.server.getPlayer(user),
							"Account activated but could not update Stats");
				}
			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Username could not be found");
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
			} else {
				plugin.sendInfo(ForumAA.server.getPlayer(user),
						"Account activated but could not update Stats");
			}
		}
		closeCon();
	}

	private void updateStats(int id, String username, String user_colour,
			int gID) throws ClassNotFoundException, SQLException {
		sqlClass();
		sqlCon();

		ResultSet rs;
		if (user_colour.equals("")) {
			query = "SELECT group_colour FROM " + tablePref
					+ "groups WHERE group_id='" + gID + "'";
			rs = SELECT(query);
			if (rs.next()) {
				user_colour = rs.getString("group_colour");
			}
		}
		query = "UPDATE " + tablePref + "config SET config_value = '" + id
				+ "' WHERE config_name = 'newest_user_id'";
		UPDATE(query);
		query = "UPDATE " + tablePref + "config SET config_value = '"
				+ username + "' WHERE config_name = 'newest_username'";
		UPDATE(query);
		query = "UPDATE " + tablePref + "config SET config_value = '"
				+ user_colour + "' WHERE config_name = 'newest_user_colour'";
		UPDATE(query);
		query = "UPDATE "
				+ tablePref
				+ "config SET config_value = config_value+1 WHERE config_name = 'num_users'";
		UPDATE(query);
		closeCon();
	}

	public void saveMybbUser(String user) throws SQLException,
			ClassNotFoundException {
		sqlClass();
		sqlCon();
		if (!customField.isEmpty()) {
			query = "SELECT ufid FROM " + tablePref + "userfields WHERE fid"
					+ customField + "='" + user + "'";
			ResultSet rs = SELECT(query);
			if (rs.next()) {
				query = "UPDATE " + tablePref
						+ "users SET usergroup='2' WHERE uid='"
						+ rs.getInt("ufid") + "'";
				UPDATE(query);

				query = "SELECT * FROM " + tablePref + "users WHERE uid="
						+ rs.getInt("ufid") + " AND usergroup='2'";
				rs = SELECT(query);
				closeCon();
				if (rs.next()) {
					plugin.sendInfo(ForumAA.server.getPlayer(user),
							"Account activated for " + rs.getString("username"));

				} else {
					plugin.sendError(ForumAA.server.getPlayer(user),
							"Your account has not been activated");
				}

			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Couldn't find your username");
			}
		} else {
			query = "UPDATE " + tablePref
					+ "users SET usergroup='2' WHERE username='" + user + "'";
			UPDATE(query);
			closeCon();

			query = "SELECT * FROM " + tablePref + "users WHERE username='"
					+ user + "' AND usergroup='2'";
			ResultSet rs = SELECT(query);
			closeCon();
			if (rs.next()) {
				plugin.sendInfo(ForumAA.server.getPlayer(user),
						"Account activated for " + rs.getString("username"));

			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Your account has not been activated");
			}
		}
		closeCon();
	}

	public void saveXenforoUser(String user) throws SQLException,
			ClassNotFoundException {
		sqlClass();
		sqlCon();
		if (!customField.isEmpty()) {
			query = "SELECT user_id FROM " + tablePref
					+ "user_field_value WHERE field_value ='" + user + "'";
			ResultSet rs = SELECT(query);
			if (rs.next()) {
				query = "UPDATE " + tablePref
						+ "user SET user_state='valid' WHERE user_id='"
						+ rs.getInt("user_id") + "'";
				UPDATE(query);
				/**
				 * 
				 */
				query = "SELECT * FROM " + tablePref + "user WHERE user_id="
						+ rs.getInt("user_id") + " AND user_state='valid'";
				rs = SELECT(query);
				closeCon();
				if (rs.next()) {
					plugin.sendInfo(ForumAA.server.getPlayer(user),
							"Account activated for " + rs.getString("username"));

				} else {
					plugin.sendError(ForumAA.server.getPlayer(user),
							"Your account has not been activated");
				}

			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Couldn't find your username");
			}
		} else {
			query = "UPDATE " + tablePref
					+ "user SET user_state='valid' WHERE username='" + user
					+ "'";
			UPDATE(query);
			closeCon();

			query = "SELECT * FROM " + tablePref + "user WHERE username='"
					+ user + "' AND user_state='valid'";
			ResultSet rs = SELECT(query);
			closeCon();
			if (rs.next()) {
				plugin.sendInfo(ForumAA.server.getPlayer(user),
						"Account activated for " + rs.getString("username"));

			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Your account has not been activated");
			}
		}
		closeCon();
	}

	public void saveIpbUser(String user) throws ClassNotFoundException,
			SQLException {
		sqlClass();
		sqlCon();
		if (!customField.isEmpty()) {
			query = "SELECT member_id FROM " + tablePref
					+ "pfields_content WHERE field_" + customField + " ='"
					+ user + "'";
			ResultSet rs = SELECT(query);
			if (rs.next()) {
				query = "UPDATE " + tablePref
						+ "members SET member_group_id ='3' WHERE member_id='"
						+ rs.getInt("member_id") + "'";
				UPDATE(query);
				/**
				 * 
				 */
				query = "SELECT * FROM " + tablePref
						+ "members WHERE member_group_id='3' AND member_id="
						+ rs.getInt("member_id");
				rs = SELECT(query);
				closeCon();
				if (rs.next()) {
					plugin.sendInfo(
							ForumAA.server.getPlayer(user),
							"Account activated for "
									+ rs.getString("members_display_name"));
				} else {
					plugin.sendError(ForumAA.server.getPlayer(user),
							"Your account has not been activated");
				}

			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Couldn't find your username");
			}
		} else {
			query = "UPDATE "
					+ tablePref
					+ "members SET member_group_id ='3' WHERE members_display_name='"
					+ user + "'";
			UPDATE(query);
			/**
			 * 
			 */
			query = "SELECT * FROM " + tablePref
					+ "members WHERE members_display_name='" + user
					+ "' AND member_group_id='3'";
			ResultSet rs = SELECT(query);
			closeCon();
			if (rs.next()) {
				plugin.sendInfo(
						ForumAA.server.getPlayer(user),
						"Account activated for "
								+ rs.getString("members_display_name"));

			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Your account has not been activated");
			}
		}
		closeCon();
	}

	public void saveSmfUser(String user) throws ClassNotFoundException,
			SQLException {
		sqlClass();
		sqlCon();
		if (!customField.isEmpty()) {
			query = "SELECT id_member FROM " + tablePref
					+ "themes WHERE variable='" + smfFieldValue()
					+ "' AND value='" + user + "'";
			ResultSet rs = SELECT(query);
			if (rs.next()) {
				query = "UPDATE " + tablePref
						+ "members SET is_activated='1' WHERE id_member='"
						+ rs.getInt("id_member") + "'";
				UPDATE(query);
				query = "SELECT * FROM " + tablePref
						+ "members WHERE id_member=" + rs.getInt("id_member")
						+ " AND is_activated='1'";
				rs = SELECT(query);
				closeCon();
				if (rs.next()) {
					plugin.sendInfo(ForumAA.server.getPlayer(user),
							"Account activated for " + rs.getString("username"));

				} else {
					plugin.sendError(ForumAA.server.getPlayer(user),
							"Your account has not been activated");
				}

			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Couldn't find your username");
			}
		} else {
			query = "UPDATE " + tablePref
					+ "members SET is_activated='1' WHERE member_name='" + user
					+ "'";
			UPDATE(query);
			closeCon();

			query = "SELECT * FROM " + tablePref + "member WHERE member_name='"
					+ user + "' AND is_activated='1'";
			ResultSet rs = SELECT(query);
			closeCon();
			if (rs.next()) {
				plugin.sendInfo(ForumAA.server.getPlayer(user),
						"Account activated for " + rs.getString("username"));

			} else {
				plugin.sendError(ForumAA.server.getPlayer(user),
						"Your account has not been activated");
			}
		}
		closeCon();
	}

	private String smfFieldValue() throws ClassNotFoundException, SQLException {
		sqlClass();
		sqlCon();
		query = "SELECT * FROM " + tablePref
				+ "custom_fields WHERE field_name ='" + customField + "'";
		ResultSet rs = SELECT(query);
		closeCon();
		return rs.getString("col_name");
	}

	public boolean checkExists(String userC) throws SQLException,
			ClassNotFoundException {
		sqlClass();
		sqlCon();
		if (forumType.equalsIgnoreCase("phpbb")) {
			if (!customField.isEmpty()) {
				query = "SELECT * FROM " + tablePref
						+ "profile_fields_data WHERE pf_" + customField + "='"
						+ userC + "'";
			}
			query = "SELECT * FROM " + tablePref + "users WHERE username='" + userC
					+ "'";
		} else if (forumType.equalsIgnoreCase("mybb")) {
			if (!customField.isEmpty()) {
				query = "SELECT * FROM " + tablePref + "userfields WHERE fid"
						+ customField + "='" + userC + "'";
			}
			query = "SELECT * FROM " + tablePref + "users WHERE username='" + userC
					+ "'";
		} else if (forumType.equalsIgnoreCase("xenforo")) {
			if (!customField.isEmpty()) {
				query = "SELECT * FROM " + tablePref
						+ "user_field WHERE field_id ='" + customField
						+ "' AND field_value ='" + userC + "'";
			}
			query = "SELECT * FROM " + tablePref + "user WHERE username='"
					+ userC + "'";
		} else if (forumType.equalsIgnoreCase("ipb")) {
			if (!customField.isEmpty()) {
				query = "SELECT * FROM " + tablePref
						+ "pfields_content WHERE field_" + customField + " ='"
						+ userC + "'";
			}
			query = "SELECT * FROM " + tablePref
					+ "members WHERE members_display_name='" + userC + "'";
		} else if (forumType.equalsIgnoreCase("smf")) {
			if (!customField.isEmpty()) {
				query = "SELECT id_member FROM " + tablePref
						+ "themes WHERE variable='" + smfFieldValue()
						+ "' AND value='" + userC + "'";
			}
			query = "SELECT * FROM " + tablePref
					+ "members WHERE member_name='" + userC + "'";
		}

		ResultSet rs = SELECT(query);
		closeCon();
		if (rs.next()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkActivated(String userC) throws ClassNotFoundException,
			SQLException {
		sqlClass();
		sqlCon();
		if (forumType.equalsIgnoreCase("phpbb")) {
			if (!customField.isEmpty()) {
				query = "SELECT user_id FROM " + tablePref
						+ "profile_fields_data WHERE pf_" + customField + "='"
						+ userC + "'";

				ResultSet rs = SELECT(query);

				if (rs.next()) {
					query = "SELECT * FROM " + tablePref
							+ "users WHERE user_id='" + rs.getInt("user_id")
							+ "' AND user_type = '0'";

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
						+ userC + "' AND user_type = '0'";
				ResultSet rs = SELECT(query);

				if (rs.next()) {
					closeCon();
					return true;
				} else {
					closeCon();
					return false;
				}
			}
		} else if (forumType.equalsIgnoreCase("mybb")) {
			query = "SELECT * FROM " + tablePref + "users WHERE username='"
					+ userC + "' AND usergroup=2";

			if (!customField.isEmpty()) {
				query = "SELECT ufid FROM " + tablePref
						+ "userfields WHERE fid" + customField + "='" + userC
						+ "'";
				ResultSet rs = SELECT(query);
				if (rs.next()) {
					query = "SELECT * FROM " + tablePref + "users WHERE uid='"
							+ rs.getInt("ufid") + "' AND usergroup = '2'";
				} else {
					return false;
				}
			}

			ResultSet rs = SELECT(query);
			closeCon();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} else if (forumType.equalsIgnoreCase("xenforo")) {
			if (!customField.isEmpty()) {
				query = "SELECT * FROM " + tablePref
						+ "user_field_value WHERE field_id='" + customField
						+ "' AND field_value='" + userC + "'";

				ResultSet rs = SELECT(query);

				if (rs.next()) {
					query = "SELECT * FROM " + tablePref
							+ "user WHERE user_id='" + rs.getInt("user_id")
							+ "' AND user_state='valid'";

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

				query = "SELECT * FROM " + tablePref + "user WHERE username='"
						+ userC + "' AND user_state='valid'";
				ResultSet rs = SELECT(query);

				if (rs.next()) {
					closeCon();
					return true;
				} else {
					closeCon();
					return false;
				}
			}
		} else if (forumType.equalsIgnoreCase("ipb")) {
			query = "SELECT * FROM " + tablePref
					+ "members WHERE members_display_name='" + userC
					+ "' AND member_group_id=3";

			if (!customField.isEmpty()) {
				query = "SELECT member_id FROM " + tablePref
						+ "pfields_content WHERE field_" + customField + "='"
						+ userC + "'";
				ResultSet rs = SELECT(query);
				if (rs.next()) {
					query = "SELECT * FROM " + tablePref
							+ "members WHERE member_id='"
							+ rs.getInt("member_id")
							+ "' AND member_group_id = '3'";
				} else {
					return false;
				}
			}

			ResultSet rs = SELECT(query);
			closeCon();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} else if (forumType.equalsIgnoreCase("smf")) {
			if (!customField.isEmpty()) {
				query = "SELECT id_member FROM " + tablePref
						+ "themes WHERE variable='" + smfFieldValue()
						+ "' AND value='" + userC + "'";

				ResultSet rs = SELECT(query);

				if (rs.next()) {
					query = "SELECT * FROM " + tablePref
							+ "members WHERE id_member='"
							+ rs.getInt("id_member") + "' AND is_activated='1'";

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

				query = "SELECT * FROM " + tablePref
						+ "members WHERE member_name='" + userC
						+ "' AND is_activated='1'";
				ResultSet rs = SELECT(query);

				if (rs.next()) {
					closeCon();
					return true;
				} else {
					closeCon();
					return false;
				}
			}
		} else {
			return false;
		}
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
