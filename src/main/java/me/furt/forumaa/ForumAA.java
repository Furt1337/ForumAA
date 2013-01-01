package me.furt.forumaa;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ForumAA extends JavaPlugin {

	private SQLQuery sqlDB = new SQLQuery(this);
	private final FAAPlayerListener faPl = new FAAPlayerListener(this);
	public static Logger log = Logger.getLogger("Minecraft");
	public static Server server;
	private String forumURL;

	public void onDisable() {
		logInfo("Disabled");
	}

	public void onEnable() {
		checkFiles();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.faPl, this);
		getCommand("account").setExecutor(new AccountCommand(this));
		String errorMsg = null;
		server = getServer();
		this.sqlDB.url = getConfig().getString("Database.URL");
		this.sqlDB.port = getConfig().getString("Database.Port");
		this.sqlDB.username = getConfig().getString("Database.Username");
		this.sqlDB.password = getConfig().getString("Database.Password");
		this.sqlDB.tablePref = getConfig().getString("Database.Table_Prefix");
		this.sqlDB.database = getConfig().getString("Database.Database");
		this.sqlDB.customField = getConfig().getString(
				"Optional.Custom_Field_ID");
		this.forumURL = getConfig().getString("Forum.URL");

		if (getConfig().getString("Forum.Type").equalsIgnoreCase("phpbb")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else if (getConfig().getString("Forum.Type").equalsIgnoreCase("mybb")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else if (getConfig().getString("Forum.Type").equalsIgnoreCase("ipb")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else if (getConfig().getString("Forum.Type").equalsIgnoreCase("smf")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else if (getConfig().getString("Forum.Type").equalsIgnoreCase(
				"xenforo")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else {
			errorMsg = getConfig().getString("Forum.Type")
					+ " is not a valid forum type! Make sure config.yml is setup properly.";
			setEnabled(false);
		}

		if (errorMsg == null) {
			try {
				if (this.sqlDB.sqlCon() != null) {
					if (this.sqlDB.checkTables()) {
						if (!this.sqlDB.customField.isEmpty()) {
							if (this.sqlDB.checkCustomColumn()) {
								logInfo("Database connected. Custom Field OK.");
								return;
							}

							logInfo("Cannot find custom field. Make sure config.yml is setup properly.");
							setEnabled(false);
							return;
						}

						logInfo("Database connected.");
						return;
					}

					logInfo("Could not connect to Users table. Make sure config.yml is setup properly.");
					setEnabled(false);
					return;
				}
			} catch (SQLException e) {
				logError("Could not connect to database! Make sure config.yml is setup properly.");
				setEnabled(false);
			} catch (ClassNotFoundException e) {
				logError("Could not connect to database! Make sure config.yml is setup properly.");
				setEnabled(false);
			}
		} else {
			logError(errorMsg);
			setEnabled(false);
		}
	}

	public boolean checkAccount(Player player) {
		boolean check = false;
		try {
			if (sqlDB.checkExists(player.getName())) {
				check = true;
			}
		} catch (SQLException e) {
			return check;
		} catch (ClassNotFoundException e) {
			return check;
		}
		return check;
	}

	public void activateUser(Player player, String mode) {
		String name = player.getName();
		try {
			// Check if user exists
			if (sqlDB.checkExists(name)) {
				// Check if user is already activated
				if (!sqlDB.checkActivated(name)) {
					// Save the user
					if (sqlDB.forumType.equalsIgnoreCase("phpbb")) {
						sqlDB.savePhpbbUser(name);
					} else if (sqlDB.forumType.equalsIgnoreCase("mybb")) {
						sqlDB.saveMybbUser(name);
					} else if (sqlDB.forumType.equalsIgnoreCase("xenforo")) {
						sqlDB.saveXenforoUser(name);
					} else if (sqlDB.forumType.equalsIgnoreCase("ipb")) {
						sqlDB.saveIpbUser(name);
					} else if (sqlDB.forumType.equalsIgnoreCase("smf")) {
						sqlDB.saveSmfUser(name);
					}
				} else {
					if (!mode.equals("login")) {
						sendError(player, "Account already activated");
					}
				}
			} else {
				sendError(player, "No account found. Go to " + forumURL
						+ " to register");
			}
		} catch (SQLException e) {
			logError("SQL Error Occurred.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logError("Could not find SQL Class.");
			e.printStackTrace();
		}
	}

	public void checkFiles() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		getConfig().addDefault("Database.URL", "localhost");
		getConfig().addDefault("Database.Port", "3306");
		getConfig().addDefault("Database.Username", "root");
		getConfig().addDefault("Database.Password", "password");
		getConfig().addDefault("Database.Database", "phpBB");
		getConfig().addDefault("Database.Table_Prefix", "phpbb_");
		getConfig().addDefault("Forum.Type", "phpbb");
		getConfig().addDefault("Forum.URL", "http://forum.myserver.com");
		getConfig().addDefault("Optional.Custom_Field_ID", "");
		// getConfig().addDefault("Optional.Promote_User.enable", false);
		// getConfig().addDefault("Optional.Promote_User.Post_Count", "0");
		// getConfig().addDefault("Optional.Promote_User.rank", "Member");
		getConfig().addDefault("Optional.Login_Activation", "false");
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public void sendInfo(Player player, String message) {
		player.sendMessage(ChatColor.DARK_GREEN + "[ForumAA] " + message);
	}

	public void sendError(Player player, String message) {
		player.sendMessage(ChatColor.RED + "[ForumAA] " + message);
	}

	public void logInfo(String message) {
		ForumAA.log.info("[ForumAA] " + message);
	}

	public void logError(String message) {
		ForumAA.log.severe("[ForumAA] " + message);
	}
}
