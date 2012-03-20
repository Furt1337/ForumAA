package net.kdmdesign;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ForumAccPlayerListener implements Listener {

	public static ForumAcc plugin;
	public boolean autoActivate = false;

	public ForumAccPlayerListener(ForumAcc instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		if (autoActivate) {
			Messaging.sendInfo(player, "Forum Account Found. Activating...");
			plugin.activateUser(player, "login");
		}
	}

}
