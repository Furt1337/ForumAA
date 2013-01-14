package me.furt.forumaa;

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
			final Player player = (Player) sender;
			if (args.length == 1 && (args[0].equalsIgnoreCase("activate"))) {
				plugin.getServer().getScheduler()
						.runTaskAsynchronously(plugin, new Runnable() {
							public void run() {
								if (plugin.activateUser(player))
									plugin.activateCommands(player.getName());
							}

						});
				return true;
			}
		}
		return false;
	}
}
