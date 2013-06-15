package me.furt.forumaa;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import me.furt.forumaa.forums.ForumHandler;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class ForumAA extends JavaPlugin {
	public ForumHandler handler;
	public static Server server;

	public void onEnable() {
		checkFiles();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new FAAPlayerListener(this), this);
		getCommand("account").setExecutor(new AccountCommand(this));
		server = getServer();
		Class<? extends ForumHandler> fh = null;
		try {
			fh = Class.forName(getConfig().getString("Forum.Type")).asSubclass(
					ForumHandler.class);
		} catch (ClassNotFoundException e1) {
			logError(getConfig().getString("Forum.Type")
					+ " is not a valid forum type! Make sure config.yml is setup properly.");
			setEnabled(false);
		}
		try {
			handler = fh.newInstance();
		} catch (InstantiationException e1) {
			logError(getConfig().getString("Forum.Type")
					+ " is not a valid forum type! Make sure config.yml is setup properly.");
			setEnabled(false);
		} catch (IllegalAccessException e) {
			logError(getConfig().getString("Forum.Type")
					+ " is not a valid forum type! Make sure config.yml is setup properly.");
			setEnabled(false);
		}
		handler.url = getConfig().getString("Database.URL");
		handler.port = getConfig().getString("Database.Port");
		handler.username = getConfig().getString("Database.Username");
		handler.password = getConfig().getString("Database.Password");
		handler.tablePref = getConfig().getString("Database.Table_Prefix");
		handler.database = getConfig().getString("Database.Database");
		handler.customField = getConfig().getString("Optional.Custom_Field_ID");

		getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
			public void run() {
				setEnabled(checkSql());
			}
		});

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			logInfo("PluginMetrics could not start.");
		}
	}

	public void onDisable() {
		logInfo("Disabled");
	}

	public void activateCommands(String name) {
		List<String> commands = getConfig().getStringList(
				"Optional.activation_commands");
		String[] msg = commands.toArray(new String[] {});
		for (String s : msg) {
			s = colorizeText(s);
			s = s.replaceAll("%p", name);
			getServer().dispatchCommand(getServer().getConsoleSender(), s);
		}
	}

	public boolean activateUser(Player p) {
		return false;

	}

	public boolean checkAccount(Player player) {
		boolean check = false;
		try {
			if (handler.checkExists(player.getName())) {
				check = true;
			}
		} catch (SQLException e) {
			return check;
		} catch (ClassNotFoundException e) {
			return check;
		}
		return check;

	}

	public boolean checkActivated(Player player) {
		try {
			return handler.checkActivated(player.getName());
		} catch (ClassNotFoundException c) {
			return false;
		} catch (SQLException e) {
			return false;
		}

	}

	public void checkFiles() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		List<String> commands = new ArrayList<String>();
		commands.add("say &4%p &9has successfully activated their forum account!");
		commands.add("say Just another test command!");
		getConfig().addDefault("Database.URL", "localhost");
		getConfig().addDefault("Database.Port", "3306");
		getConfig().addDefault("Database.Username", "root");
		getConfig().addDefault("Database.Password", "password");
		getConfig().addDefault("Database.Database", "smf");
		getConfig().addDefault("Database.Table_Prefix", "smf_");
		getConfig().addDefault("Forum.Type", "smf");
		getConfig().addDefault("Forum.URL", "http://forum.myserver.com");
		getConfig().addDefault("Optional.Custom_Field_ID", "");
		getConfig().addDefault("Optional.activation_commands", commands);
		getConfig().addDefault("Optional.Login_Activation", false);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public boolean checkSql() {
		try {
			if (handler.sqlCon() != null) {
				if (handler.checkTables()) {
					if (!handler.customField.isEmpty()) {
						if (handler.checkCustomField()) {
							logInfo("Database connected. Custom Field OK.");
							return true;
						}

						logInfo("Cannot find custom field. Make sure config.yml is setup properly.");
						return false;
					}

					logInfo("Database connected.");
					return true;
				}

				logInfo("Could not connect to Users table. Make sure config.yml is setup properly.");
				return false;
			} else {
				return false;
			}

		} catch (SQLException e) {
			logError("Could not connect to database! Make sure config.yml is setup properly.");
			return false;
		} catch (ClassNotFoundException e) {
			logError("Could not connect to database! Make sure config.yml is setup properly.");
			return false;
		}

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

}
