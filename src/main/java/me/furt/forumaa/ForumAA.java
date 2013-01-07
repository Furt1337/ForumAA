package me.furt.forumaa;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ForumAA extends JavaPlugin {

	private SQLQuery sqlDB = new SQLQuery(this);
	private final FAAPlayerListener faPl = new FAAPlayerListener(this);
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
		
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		   this.getLogger().log(Level.WARNING, "PluginMetrics could not start.");
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

	public boolean activateUser(Player player) {
		String name = player.getName();
		boolean activated = false;
		try {
			// Check if user exists
			if (sqlDB.checkExists(name)) {
				// Check if user is already activated
				if (!sqlDB.checkActivated(name)) {
					// Save the user
					if (sqlDB.forumType.equalsIgnoreCase("phpbb")) {
						activated = sqlDB.savePhpbbUser(name);
					} else if (sqlDB.forumType.equalsIgnoreCase("mybb")) {
						activated = sqlDB.saveMybbUser(name);
					} else if (sqlDB.forumType.equalsIgnoreCase("xenforo")) {
						activated = sqlDB.saveXenforoUser(name);
					} else if (sqlDB.forumType.equalsIgnoreCase("ipb")) {
						activated = sqlDB.saveIpbUser(name);
					} else if (sqlDB.forumType.equalsIgnoreCase("smf")) {
						activated = sqlDB.saveSmfUser(name);
					}
				}
			} else {
				sendError(player, "No account found. Go to " + forumURL
						+ " to register");
			}
			return activated;
		} catch (SQLException e) {
			logError("SQL Error Occurred.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logError("Could not find SQL Class.");
			e.printStackTrace();
		}
		return activated;
	}

	public void checkFiles() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		List<String> commands = new ArrayList<String>();
		commands.add("say &4%p &9has successfully activated there forum account!");
		commands.add("say Just another test command!");
		getConfig().addDefault("Database.URL", "localhost");
		getConfig().addDefault("Database.Port", "3306");
		getConfig().addDefault("Database.Username", "root");
		getConfig().addDefault("Database.Password", "password");
		getConfig().addDefault("Database.Database", "phpBB");
		getConfig().addDefault("Database.Table_Prefix", "phpbb_");
		getConfig().addDefault("Forum.Type", "phpbb");
		getConfig().addDefault("Forum.URL", "http://forum.myserver.com");
		getConfig().addDefault("Optional.Custom_Field_ID", "");
		getConfig().addDefault("Optional.activation_commands", commands);
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
		this.getLogger().info(message);
	}

	public void logError(String message) {
		this.getLogger().warning(message);
	}
	
	public String colorizeText(String string) {
		string = string.replaceAll("&0", "§0");
		string = string.replaceAll("&1", "§1");
		string = string.replaceAll("&2", "§2");
		string = string.replaceAll("&3", "§3");
		string = string.replaceAll("&4", "§4");
		string = string.replaceAll("&5", "§5");
		string = string.replaceAll("&6", "§6");
		string = string.replaceAll("&7", "§7");
		string = string.replaceAll("&8", "§8");
		string = string.replaceAll("&9", "§9");
		string = string.replaceAll("&a", "§a");
		string = string.replaceAll("&b", "§b");
		string = string.replaceAll("&c", "§c");
		string = string.replaceAll("&d", "§d");
		string = string.replaceAll("&e", "§e");
		string = string.replaceAll("&f", "§f");
		return string;
	}

	public void activateCommands(String name) {
		List<String> commands = getConfig().getStringList("Optional.activation_commands");
		String[] msg = commands.toArray(new String[] {});
		for(String s: msg) {
			s = colorizeText(s);
			s = s.replaceAll("%p", name);
			getServer().dispatchCommand(getServer().getConsoleSender(), s);
		}
		
	}
}
