package me.furt;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ForumAA extends JavaPlugin {

	// Declare sqlQuery
	private SQLQuery sqlDB = new SQLQuery();

	// Initialise Listener
	private final FAPlayerListener faPl = new FAPlayerListener(this);

	public static Logger log = Logger.getLogger("Minecraft");
	public static Server server;
	File file = new File(getDataFolder(), "config.yml");
	private String forumURL;

	@Override
	public void onDisable() {
		// Disable Plugin Message
		Messaging.logInfo("Disabled");
	}

	public void onEnable() {
		checkFiles();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.faPl, this);
		String errorMsg = null;
		server = getServer();
		this.faPl.autoActivate = getConfig().getBoolean(
				"Behaviour.ActivateOnLogin");
		this.sqlDB.url = getConfig().getString("Database.URL");
		this.sqlDB.port = getConfig().getString("Database.Port");
		this.sqlDB.username = getConfig().getString("Database.Username");
		this.sqlDB.password = getConfig().getString("Database.Password");
		this.sqlDB.tablePref = getConfig().getString("Database.Table_Prefix");
		this.sqlDB.database = getConfig().getString("Database.Database");
		this.sqlDB.customField = getConfig().getString(
				"Optional.Custom_Username_FieldID");
		this.forumURL = getConfig().getString("Forum.URL");

		if (getConfig().getString("Forum.Type").equalsIgnoreCase("phpbb")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else if (getConfig().getString("Forum.Type")
				.equalsIgnoreCase("mybb")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else if (getConfig().getString("Forum.Type")
				.equalsIgnoreCase("ipb")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else if (getConfig().getString("Forum.Type")
				.equalsIgnoreCase("smf")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else if (getConfig().getString("Forum.Type")
				.equalsIgnoreCase("xenforo")) {
			this.sqlDB.forumType = getConfig().getString("Forum.Type");
		} else {
			errorMsg = "Not a valid forum type!";
		}

		if (errorMsg == null) {
			try {
				if (this.sqlDB.sqlCon() != null) {
					if (this.sqlDB.checkTables()) {
						if (!this.sqlDB.customField.isEmpty()) {
							if (this.sqlDB.checkCustomColumn()) {
								Messaging
										.logInfo("Database connected. Custom Field OK.");
								return;
							}

							Messaging
									.logInfo("Cannot find custom field. Please check config");
							setEnabled(false);
							return;
						}

						Messaging.logInfo("Database connected.");
						return;
					}

					Messaging
							.logInfo("Could not connect to Users table. Check config.");
					setEnabled(false);
					return;
				}

				Messaging.logError("Could not connect to database!");
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			Messaging.logError(errorMsg);
			setEnabled(false);
		}
	}

	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("account")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;

				if (args.length == 1 && (args[0].equalsIgnoreCase("activate"))) {
					activateUser(player, "command");
				} else {
					return false;
				}

			}
		} else {
			return false;
		}
		return true;
	}

	public void activateUser(Player player, String mode) {
		String name = player.getName();
		try {
			// Check if user exists
			if (sqlDB.checkExists(name)) {
				// Check if user is already activated
				if (!sqlDB.checkActivated(name)) {
					// Save the user
					if (sqlDB.forumType.equalsIgnoreCase("phpbb"))
						sqlDB.savePhpbbUser(name);
					else
						sqlDB.saveMybbUser(name);
				} else {
					if (!mode.equals("login")) {
						Messaging
								.sendError(player, "Account already activated");
					}
				}
			} else {
				Messaging.sendError(player, "No account found. Go to "
						+ forumURL + " to register");
			}
		} catch (SQLException e) {
			Messaging.logError("SQL Error Occurred.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Messaging.logError("Could not find SQL Class.");
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
		getConfig().addDefault("Optional.Custom_Username_FieldID", "");
		getConfig().addDefault("Behaviour.ActivateOnLogin", "false");
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
}
