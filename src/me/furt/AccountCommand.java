package me.furt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountCommand implements CommandExecutor {

	private ForumAA plugin;

	public AccountCommand(ForumAA instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 1 && (args[0].equalsIgnoreCase("activate"))) {
				plugin.activateUser(player, "command");
				return true;
			}
		}
		return false;
	}

}
