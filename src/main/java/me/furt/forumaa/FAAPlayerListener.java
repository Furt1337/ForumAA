package me.furt.forumaa;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FAAPlayerListener implements Listener {

	public static ForumAA plugin;

	public FAAPlayerListener(ForumAA instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		final Player player = event.getPlayer();
		if (plugin.getConfig().getBoolean("Optional.Login_Activation")) {
			plugin.getServer().getScheduler()
					.runTaskAsynchronously(plugin, new Runnable() {
						public void run() {
							if (plugin.checkAccount(player)) {
								if (!plugin.checkActivated(player)) {
									plugin.sendInfo(player,
											"Forum Account Found. Activating...");
									if (plugin.activateUser(player))
										plugin.activateCommands(player
												.getName());
								}
							}
						}
					});
		}

	}
}
